
package net.k3rnel.server.battle.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import net.k3rnel.server.backend.ItemProcessor.CatchNet;
import net.k3rnel.server.backend.entity.PlayerChar;
import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.BattleTurn;
import net.k3rnel.server.battle.DataService;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.MonsterEvolution;
import net.k3rnel.server.battle.MonsterSpecies;
import net.k3rnel.server.battle.MonsterEvolution.EvolutionTypes;
import net.k3rnel.server.battle.mechanics.BattleMechanics;
import net.k3rnel.server.battle.mechanics.MoveQueueException;
import net.k3rnel.server.battle.mechanics.statuses.StatusEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.FieldEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.HailEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.RainEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.SandstormEffect;
import net.k3rnel.server.feature.TimeService;
import net.k3rnel.server.network.TcpProtocolHandler;
import net.k3rnel.server.network.message.battle.BattleEndMessage;
import net.k3rnel.server.network.message.battle.BattleExpMessage;
import net.k3rnel.server.network.message.battle.BattleInitMessage;
import net.k3rnel.server.network.message.battle.BattleLevelChangeMessage;
import net.k3rnel.server.network.message.battle.BattleMessage;
import net.k3rnel.server.network.message.battle.BattleMoveMessage;
import net.k3rnel.server.network.message.battle.BattleMoveRequest;
import net.k3rnel.server.network.message.battle.BattleRewardMessage;
import net.k3rnel.server.network.message.battle.EnemyDataMessage;
import net.k3rnel.server.network.message.battle.FaintMessage;
import net.k3rnel.server.network.message.battle.HealthChangeMessage;
import net.k3rnel.server.network.message.battle.NoPPMessage;
import net.k3rnel.server.network.message.battle.RunMessage;
import net.k3rnel.server.network.message.battle.StatusChangeMessage;
import net.k3rnel.server.network.message.battle.SwitchMessage;
import net.k3rnel.server.network.message.battle.SwitchRequest;
import net.k3rnel.server.network.message.battle.BattleEndMessage.BattleEnd;
import net.k3rnel.server.network.message.battle.BattleRewardMessage.BattleRewardType;

/**
 * A battlefield for wild Monster battles
 * @author shadowkanji
 */
public class WildBattleField extends BattleField {
	private final PlayerChar   m_player;
	private Monster            m_wildMonster;
	private final BattleTurn[] m_turn                 = new BattleTurn[2];
	private int                m_runCount;
	Set<Monster>               m_participatingPokemon = new LinkedHashSet<Monster>();
	private boolean            m_finished             = false;

	/**
	 * Constructor
	 * 
	 * @param m
	 * @param p
	 * @param wild
	 */
	public WildBattleField(BattleMechanics m, PlayerChar p, Monster wild) {
		super(m, new Monster[][] { p.getParty(), new Monster[] { wild } });
		/* Send information to client */
		p.setBattling(true);
		p.setBattleId(0);
		TcpProtocolHandler.writeMessage(p.getTcpSession(), new BattleInitMessage(
				true, 1));
		TcpProtocolHandler.writeMessage(p.getTcpSession(), new EnemyDataMessage(0,
				wild));

		/* Store variables */
		m_player = p;
		m_wildMonster = wild;
		m_participatingPokemon.add(p.getParty()[0]);

		/* Call methods */
		// applyWeather();
		requestMoves();
	}

	/**
	 * Applies weather effect based on world/map weather
	 */
	@Override
	public void applyWeather() {
		if (m_player.getMap().isWeatherForced()) {
			switch (m_player.getMap().getWeather()) {
			case NORMAL:
				return;
			case RAIN:
				this.applyEffect(new RainEffect());
				return;
			case HAIL:
				this.applyEffect(new HailEffect());
				return;
			case SANDSTORM:
				this.applyEffect(new SandstormEffect());
				return;
			default:
				return;
			}
		} else {
			FieldEffect f = TimeService.getWeatherEffect();
			if (f != null) {
				this.applyEffect(f);
			}
		}
	}

	@Override
	public BattleTurn[] getQueuedTurns() {
		return m_turn;
	}

	@Override
	public String getTrainerName(int idx) {
		if (idx == 0) {
			return m_player.getName();
		} else {
			return m_wildMonster.getSpeciesName();
		}
	}

	@Override
	public void informMonsterFainted(int trainer, int idx) {
		/*
		 * If the monster is the player's make sure it don't get exp
		 */
		if (trainer == 0 && m_participatingPokemon.contains(getParty(trainer)[idx])) {
			m_participatingPokemon.remove(getParty(trainer)[idx]);
		}
		if (m_player != null)
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new FaintMessage(getParty(trainer)[idx].getSpeciesName()));
	}

	@Override
	public void informMonsterHealthChanged(Monster poke, int change) {
		if (m_player != null) {
			if (getActiveMonster()[0] == poke) {
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new HealthChangeMessage(0, change));
			} else if (getActiveMonster()[1] == poke) {
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new HealthChangeMessage(1, change));
			} else {
				int index = getMonsterPartyIndex(0, poke);
				if (index > -1) {
					m_player.getTcpSession().write(
							"Ph" + String.valueOf(index) + poke.getHealth());
				}
			}
		}
	}

	@Override
	public void informStatusApplied(Monster poke, StatusEffect eff) {
		if (m_finished) return;
		if (m_player != null) {
			if (poke != m_wildMonster) TcpProtocolHandler.writeMessage(m_player
					.getTcpSession(), new StatusChangeMessage(0, poke.getSpeciesName(), eff
							.getName(), false));
			else if (poke == m_wildMonster)
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new StatusChangeMessage(1, poke.getSpeciesName(), eff.getName(),
								false));
		}
	}

	@Override
	public void informStatusRemoved(Monster poke, StatusEffect eff) {
		if (m_finished) return;
		if (m_player != null) {
			if (poke != m_wildMonster && !poke.isFainted()) TcpProtocolHandler.writeMessage(m_player
					.getTcpSession(), new StatusChangeMessage(0, poke.getSpeciesName(), eff
							.getName(), true));
			else if (poke == m_wildMonster && !poke.isFainted())
				TcpProtocolHandler
				.writeMessage(m_player.getTcpSession(), new StatusChangeMessage(1,
						poke.getSpeciesName(), eff.getName(), true));
		}
	}

	@Override
	public void informSwitchInMonster(int trainer, Monster poke) {
		if (trainer == 0 && m_player != null) {
			if (!m_participatingPokemon.contains(poke))
				m_participatingPokemon.add(poke);
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new SwitchMessage(m_player.getName(), poke.getSpeciesName(), trainer,
							getMonsterPartyIndex(trainer, poke)));
		}
	}

	@Override
	public void informUseMove(Monster poke, String name) {
		if (m_player != null)
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleMoveMessage(poke.getSpeciesName(), name));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void informVictory(int winner) {
		m_finished = true;
		if (winner == 0) {
			calculateExp();
			m_player.removeTempStatusEffects();
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleEndMessage(BattleEnd.WON));
		} else {
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleEndMessage(BattleEnd.LOST));
			m_player.lostBattle();
		}
		m_player.setBattling(false);
		dispose();
		m_wildMonster = null;
		if (m_dispatch != null) {
			/*
			 * This very bad programming but shoddy does it and forces us to do it
			 */
			Thread t = m_dispatch;
			m_dispatch = null;
			t.stop();
		}
	}

	/**
	 * Queues a battle turn
	 */
	@Override
	public void queueMove(int trainer, BattleTurn move) throws MoveQueueException {
		/* Checks the move exists */
		if (move.isMoveTurn() && move.getId() != -1
				&& getActiveMonster()[trainer].getMove(move.getId()) == null) {
			requestMove(trainer);
			return;
		}
		/* Handle forced switches */
		if (m_isWaiting && m_replace != null && m_replace[trainer]) {
			if (!move.isMoveTurn()) {
				if (getActiveMonster()[trainer].compareTo(this.getParty(trainer)[move
				                                                                 .getId()]) != 0) {
					this.switchInMonster(trainer, move.getId());
					m_replace[trainer] = false;
					m_isWaiting = false;
					return;
				}
			}
			requestMonsterReplacement(trainer);
			return;
		}
		/* Ensure they haven't queued a move already */
		if (m_turn[trainer] == null) {
			/* Handle Monster being unhappy and ignoring you */
			if (trainer == 0 && !getActiveMonster()[0].isFainted()) {
				if (getActiveMonster()[0].getHappiness() <= 40) {
					/* Monster is unhappy, they'll do what they feel like */
					showMessage(getActiveMonster()[0].getSpeciesName() + " is unhappy!");
					int moveID = getMechanics().getRandom().nextInt(4);
					while (getActiveMonster()[0].getMove(moveID) == null)
						moveID = getMechanics().getRandom().nextInt(4);
					move = BattleTurn.getMoveTurn(moveID);
				} else if (getActiveMonster()[0].getHappiness() < 70) {
					/* Monster is partially unhappy, 50% chance they'll listen to you */
					if (getMechanics().getRandom().nextInt(2) == 1) {
						showMessage(getActiveMonster()[0].getSpeciesName() + " is unhappy!");
						int moveID = getMechanics().getRandom().nextInt(4);
						while (getActiveMonster()[0].getMove(moveID) == null)
							moveID = getMechanics().getRandom().nextInt(4);
						move = BattleTurn.getMoveTurn(moveID);
					}
				}
			}
			if (move.getId() == -1) {
				if (m_dispatch == null && (trainer == 0 && m_turn[1] != null)) {
					m_dispatch = new Thread(new Runnable() {
						public void run() {
							executeTurn(m_turn);
							m_dispatch = null;
						}
					});
					m_dispatch.start();
					return;
				}
			} else {
				if (this.getActiveMonster()[trainer].isFainted()) {
					if (!move.isMoveTurn()
							&& this.getParty(trainer)[move.getId()] != null
							&& this.getParty(trainer)[move.getId()].getHealth() > 0) {
						switchInMonster(trainer, move.getId());
						requestMoves();
						if (!m_participatingPokemon.contains(getActiveMonster()[0]))
							m_participatingPokemon.add(getActiveMonster()[0]);
						return;
					} else {
						if (trainer == 0 && getAliveCount(0) > 0) {
							if (getAliveCount(0) > 0) {
								if (m_participatingPokemon.contains(getActiveMonster()[0]))
									m_participatingPokemon.remove(getActiveMonster()[0]);
								requestMonsterReplacement(0);
								return;
							} else {
								/* Player lost the battle */
								this.informVictory(1);
								return;
							}
						}
					}
				} else {
					if (move.isMoveTurn()) {
						if (getActiveMonster()[trainer].mustStruggle()) m_turn[trainer] = BattleTurn
						.getMoveTurn(-1);
						else {
							if (this.getActiveMonster()[trainer].getPp(move.getId()) <= 0) {
								if (trainer == 0) {
									TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
											new NoPPMessage(this.getActiveMonster()[trainer]
											                                        .getMoveName(move.getId())));
									requestMove(0);
								} else {
									requestMove(1);
								}
								return;
							} else {
								m_turn[trainer] = move;
							}
						}
					} else {
						if (this.getActiveMonster()[trainer].isActive()
								&& this.getParty(trainer)[move.getId()] != null
								&& this.getParty(trainer)[move.getId()].getHealth() > 0) {
							m_turn[trainer] = move;
						} else {
							if (trainer == 0) {
								requestMove(0);
							}
							return;
						}
					}
				}
			}
		}
		if (trainer == 0 && m_turn[1] == null) {
			requestMove(1);
			return;
		}
		if (m_dispatch != null) return;
		if (m_turn[0] != null && m_turn[1] != null) {
			m_dispatch = new Thread(new Runnable() {
				public void run() {
					executeTurn(m_turn);
					for (int i = 0; i < m_participants; ++i) {
						m_turn[i] = null;
					}
					m_dispatch = null;
				}
			});
			m_dispatch.start();
		}
	}

	/**
	 * Refreshes Monster on battlefield
	 */
	@Override
	public void refreshActiveMonster() {
		m_player.getTcpSession().write(
				"bh0" + this.getActiveMonster()[0].getHealth());
		m_player.getTcpSession().write(
				"bh1" + this.getActiveMonster()[1].getHealth());
	}

	/**
	 * Requests a new Monster (called by moves that force poke switches)
	 */
	@Override
	public void requestAndWaitForSwitch(int party) {
		if (party == 0) {
			requestMonsterReplacement(party);
			if (!m_replace[party]) { return; }
			m_isWaiting = true;
			do {
				synchronized (m_dispatch) {
					try {
						m_dispatch.wait(1000);
					} catch (InterruptedException e) {

					}
				}
			} while ((m_replace != null) && m_replace[party]);
		}
	}

	/**
	 * Generates a wild Monster move
	 */
	protected void getWildPokemonMove() {
		if (getActiveMonster()[1] == null) return;
		try {
			int moveID = getMechanics().getRandom().nextInt(4);
			while (getActiveMonster()[1].getMove(moveID) == null) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				moveID = getMechanics().getRandom().nextInt(4);
				/*
				 * Stop infinite loops when player disconnects
				 */
				if (m_player.getTcpSession() == null
						|| m_player.getTcpSession().isClosing()
						|| !m_player.getTcpSession().isConnected()) break;
			}
			queueMove(1, BattleTurn.getMoveTurn(moveID));
		} catch (MoveQueueException x) {
			x.printStackTrace();
		}
	}

	/**
	 * Requests moves
	 */
	@Override
	protected void requestMoves() {
		clearQueue();
		if (this.getActiveMonster()[0].isActive()
				&& this.getActiveMonster()[1].isActive()) {
			getWildPokemonMove();
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleMoveRequest());
		}
	}

	/**
	 * Requests a monster replacement
	 */
	@Override
	protected void requestMonsterReplacement(int i) {
		if (i == 0) {
			/*
			 * 0 = our player in this case
			 */
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new SwitchRequest());
		}
	}

	@Override
	public void showMessage(String message) {
		if (m_finished) return;
		if (m_player != null)
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleMessage(message));
	}

	/**
	 * Returns true if the player can run from the battle
	 * 
	 * @return
	 */
	private boolean canRun() {
		// Formula from http://bulbapedia.bulbagarden.net/wiki/Escape
		float A = getActiveMonster()[0].getStat(Monster.S_SPEED);
		float B = getActiveMonster()[1].getStat(Monster.S_SPEED);
		int C = ++m_runCount;

		float F = (((A * 32) / (B / 4)) + 30) * C;

		if (F > 255) return true;

		if (getMechanics().getRandom().nextInt(255) <= F) return true;

		return false;
	}

	/**
	 * Attempts to run from this battle
	 */
	public void run() {
		if (canRun()) {
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), new RunMessage(
					true));
			m_player.setBattling(false);
			this.dispose();
		} else {
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), new RunMessage(
					false));
			if (m_turn[1] == null) this.getWildPokemonMove();
			try {
				this.queueMove(0, BattleTurn.getMoveTurn(-1));
			} catch (MoveQueueException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Throws a CatchingDevice. Returns true if monster was caught
	 * 
	 * @param p
	 * @return
	 */
	public boolean throwNet(CatchNet p) {
		/* Ensure user doesn't throw a CatchingDevice while battling */
		while (m_dispatch != null) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
		}
		switch (p) {
		case SMALLNET:
			showMessage(m_player.getName() + " threw a Small Net!");
			if (getMechanics().isCaught(
					m_wildMonster,
					m_wildMonster.getRareness(), 1.0, 1)) {
				m_wildMonster.calculateStats(false);
				m_player.catchMonster(m_wildMonster);
				showMessage("You successfuly caught " + m_wildMonster.getSpeciesName());
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new BattleEndMessage(BattleEnd.CAUGHT));
				m_player.setBattling(false);
				dispose();
				return true;
			} else
				showMessage("...but it failed!");
			break;
		case GREATNET:
			showMessage(m_player.getName() + " threw a Great Net!");
			if (getMechanics().isCaught(
					m_wildMonster,
					m_wildMonster.getRareness(), 1.5, 1)) {
				m_wildMonster.calculateStats(false);
				m_player.catchMonster(m_wildMonster);
				showMessage("You successfuly caught " + m_wildMonster.getSpeciesName());
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new BattleEndMessage(BattleEnd.CAUGHT));
				m_player.setBattling(false);
				dispose();
				return true;
			} else
				showMessage("...but it failed!");
			break;
		case ULTRANET:
			showMessage(m_player.getName() + " threw an Ultra Net!");
			if (getMechanics().isCaught(
					m_wildMonster,
					m_wildMonster.getRareness(), 2.0, 1)) {
				m_wildMonster.calculateStats(false);
				m_player.catchMonster(m_wildMonster);
				showMessage("You successfuly caught " + m_wildMonster.getSpeciesName());
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new BattleEndMessage(BattleEnd.CAUGHT));
				m_player.setBattling(false);
				dispose();
				return true;
			} else
				showMessage("...but it failed!");
			break;
		case MASTERNET:
			showMessage(m_player.getName() + " threw a Master Net!");
			if (getMechanics().isCaught(
					m_wildMonster,
					m_wildMonster.getRareness(), 255.0, 1)) {
				m_wildMonster.calculateStats(false);
				m_player.catchMonster(m_wildMonster);
				showMessage("You successfuly caught " + m_wildMonster.getSpeciesName());
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new BattleEndMessage(BattleEnd.CAUGHT));
				m_player.setBattling(false);
				dispose();
				return true;
			} else
				showMessage("...but it failed!");
			break;
		}
		return false;
	}

	/**
	 * Clears the moves queue
	 */
	@Override
	public void clearQueue() {
		m_turn[0] = null;
		m_turn[1] = null;
	}

	/**
	 * Requests a move from a specific player
	 */
	@Override
	protected void requestMove(int trainer) {
		if (trainer == 0) {
			/*
			 * If its the player, send a move request packet
			 */
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleMoveRequest());
		} else {
			/*
			 * If its the wild Monster, just get the moves
			 */
			getWildPokemonMove();
		}
	}

	/**
	 * Calculates exp gained for Monster at the end of battles
	 */
	private void calculateExp() {
		/*
		 * First calculate earnings
		 */
		int item = MonsterSpecies.getDefaultData().
			getMonsterByName(m_wildMonster.getSpeciesName()).getRandomItem();
		if (item > -1) {
			m_player.getBag().addItem(item, 1);
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleRewardMessage(BattleRewardType.ITEM, item));
		} else {
			int money = 5;
			m_player.setMoney(m_player.getMoney() + money);
			m_player.updateClientMoney();
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new BattleRewardMessage(BattleRewardType.MONEY, money));
		}

		if (m_participatingPokemon.size() > 0) {
			double exp = (DataService.getBattleMechanics().calculateExpGain(
					m_wildMonster, m_participatingPokemon.size()));
			if (exp == 0) exp = 1;

			/*
			 * Secondly, calculate EVs and exp
			 */
			int[] evs = m_wildMonster.getEffortPoints();

			/*
			 * Finally, add the EVs and exp to the participating Monster
			 */
			for (Monster p : m_participatingPokemon) {
				int index = m_player.getMonsterIndex(p);

				/* Add the evs */
				/* Ensure EVs don't go over limit, before or during addition */
				int evTotal = p.getEvTotal();
				if (evTotal < 510) {
					for (int i = 0; i < evs.length; i++) {
						/* Ensure we don't hit the EV limit */
						if (evTotal + evs[i] < 510) {
							if (p.getEv(i) < 255) {
								if (p.getEv(i) + evs[i] < 255) {
									/* Add the EV */
									evTotal += evs[i];
									p.setEv(i, p.getEv(i) + evs[i]);
								} else {
									/* Cap the EV at 255 */
									evTotal += (255 - p.getEv(i));
									p.setEv(i, 255);
								}
							}
						} else {
							/*
							 * We're going to hit the EV total limit Only add what's allowed
							 */
							evs[i] = 510 - evTotal;
							if (p.getEv(i) + evs[i] < 255) p.setEv(i, p.getEv(i) + evs[i]);
							else
								p.setEv(i, 255);
							i = evs.length;
						}
					}
				}

				/* Gain exp/level up and update client */
				p.setExp(p.getExp() + exp);
				//Calculate how much exp is left to next level
				int expTillLvl = (int)(DataService.getBattleMechanics().getExpForLevel(p, (p.getLevel() + 1)) - p.getExp());
				//Make sure that value isn't negative.
				if (expTillLvl < 0){
					expTillLvl = 0;
				}
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
						new BattleExpMessage(p.getSpeciesName(), exp, expTillLvl));

				String expGain = exp + "";
				expGain = expGain.substring(0, expGain.indexOf('.'));
				m_player.getTcpSession().write("Pe" + index + expGain);

				double levelExp = DataService.getBattleMechanics().getExpForLevel(p,
						p.getLevel() + 1)
						- p.getExp();
				if (levelExp <= 0) {
					MonsterSpecies pokeData = MonsterSpecies.getDefaultData().getMonsterByName(
							p.getSpeciesName());
					boolean evolve = false;
					/* Handle evolution */
					for (int i = 0; i < pokeData.getEvolutions().length; i++) {
						MonsterEvolution evolution = pokeData.getEvolutions()[i];
						if (evolution.getType() == EvolutionTypes.Level) {
							if (evolution.getLevel() <= p.getLevel() + 1) {
								p.setEvolution(evolution);
								m_player.getTcpSession().write("PE" + index);
								evolve = true;
								i = pokeData.getEvolutions().length;
							}
						} else if (evolution.getType() == EvolutionTypes.HappinessDay) {
							if (p.getHappiness() > 220 && !TimeService.isNight()) {
								p.setEvolution(evolution);
								m_player.getTcpSession().write("PE" + index);
								evolve = true;
								i = pokeData.getEvolutions().length;
							}
						} else if (evolution.getType() == EvolutionTypes.HappinessNight) {
							if (p.getHappiness() > 220 && TimeService.isNight()) {
								p.setEvolution(evolution);
								m_player.getTcpSession().write("PE" + index);
								evolve = true;
								i = pokeData.getEvolutions().length;
							}
						} else if (evolution.getType() == EvolutionTypes.Happiness) {
							if (p.getHappiness() > 220) {
								p.setEvolution(evolution);
								m_player.getTcpSession().write("PE" + index);
								evolve = true;
								i = pokeData.getEvolutions().length;
							}
						}
					}
					/* If the Monster is evolving, don't move learn just yet */
					if (evolve) continue;

					/* This Monster just levelled up! */
					p.setHappiness(p.getHappiness() + 2);
					p.calculateStats(false);

					int level = DataService.getBattleMechanics().calculateLevel(p);
					m_player.addTrainingExp(level * 5);
					int oldLevel = p.getLevel();
					String move = "";

					/* Move learning */
					p.getMovesLearning().clear();
					for (int i = oldLevel + 1; i <= level; i++) {
						if (pokeData.getLevelMoves().get(i) != null) {
							move = pokeData.getLevelMoves().get(i);
							p.getMovesLearning().add(move);
							m_player.getTcpSession().write("Pm" + index + move);
						}
					}

					/* Save the level and update the client */
					p.setLevel(level);
					m_player.getTcpSession().write("Pl" + index + "," + level);
					TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
							new BattleLevelChangeMessage(p.getSpeciesName(), level));
					m_player.updateClientMonsterStats(index);
				}
			}
		}
	}

	@Override
	public void forceExecuteTurn() {
		if (m_turn[0] == null) {
			m_turn[0] = BattleTurn.getMoveTurn(-1);
		}
		if (m_turn[1] == null) {
			m_turn[1] = BattleTurn.getMoveTurn(-1);
		}
		executeTurn(m_turn);
	}
}
