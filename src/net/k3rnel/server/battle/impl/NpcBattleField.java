package net.k3rnel.server.battle.impl;

import net.k3rnel.server.backend.entity.NonPlayerChar;
import net.k3rnel.server.backend.entity.PlayerChar;
import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.BattleTurn;
import net.k3rnel.server.battle.Monster;
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
import net.k3rnel.server.network.message.battle.BattleInitMessage;
import net.k3rnel.server.network.message.battle.BattleMessage;
import net.k3rnel.server.network.message.battle.BattleMoveMessage;
import net.k3rnel.server.network.message.battle.BattleMoveRequest;
import net.k3rnel.server.network.message.battle.BattleRewardMessage;
import net.k3rnel.server.network.message.battle.EnemyDataMessage;
import net.k3rnel.server.network.message.battle.FaintMessage;
import net.k3rnel.server.network.message.battle.HealthChangeMessage;
import net.k3rnel.server.network.message.battle.NoPPMessage;
import net.k3rnel.server.network.message.battle.StatusChangeMessage;
import net.k3rnel.server.network.message.battle.SwitchMessage;
import net.k3rnel.server.network.message.battle.SwitchRequest;
import net.k3rnel.server.network.message.battle.BattleEndMessage.BattleEnd;
import net.k3rnel.server.network.message.battle.BattleRewardMessage.BattleRewardType;

/**
 * A battlefield for NPC battles
 * @author shadowkanji
 *
 */
public class NpcBattleField extends BattleField {
	private PlayerChar m_player;
	private NonPlayerChar m_npc;
	private BattleTurn[] m_turn = new BattleTurn[2];
	private boolean m_finished = false;

	/**
	 * Constructor
	 * @param mech
	 * @param p
	 * @param n
	 */
	public NpcBattleField(BattleMechanics mech, PlayerChar p, NonPlayerChar n) {
		super(mech, new Monster[][] { p.getParty(), n.getParty(p) });
		/* Store the player and npc */
		m_player = p;
		m_npc = n;

		/* Start the battle */
		TcpProtocolHandler.writeMessage(p.getTcpSession(), 
				new BattleInitMessage(false, getAliveCount(1)));
		/* Send enemy's Monster data */
		sendMonsterData(p);
		/* Set the player's battle id */
		m_player.setBattleId(0);
		/* Send enemy name */
		m_player.getTcpSession().write("bn" + m_npc.getName());
		/* Apply weather and request moves */
		applyWeather();
		requestMoves();
	}
	
	/**
	 * Sends monster data to the client
	 * @param receiver
	 */
	private void sendMonsterData(PlayerChar receiver) {
		for (int i = 0; i < this.getParty(1).length; i++) {
			if (this.getParty(1)[i] != null) {
				TcpProtocolHandler.writeMessage(receiver.getTcpSession(), 
						new EnemyDataMessage(i, getParty(1)[i]));
			}
		}
	}

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
	public void clearQueue() {
		m_turn[0] = null;
		m_turn[1] = null;
	}

	@Override
	public BattleTurn[] getQueuedTurns() {
		return m_turn;
	}

	@Override
	public String getTrainerName(int idx) {
		if(idx == 0)
			return m_player.getName();
		else
			return m_npc.getName();
	}

	@Override
	public void informMonsterFainted(int trainer, int idx) {
		if (m_player != null)
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
					new FaintMessage(getParty(trainer)[idx].getSpeciesName()));
	}

	@Override
	public void informMonsterHealthChanged(Monster mons, int change) {
		if (m_player != null) {
			if (getActiveMonster()[0] == mons) {
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new HealthChangeMessage(0 , change));
			} else if(getActiveMonster()[1] == mons) {
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new HealthChangeMessage(1 , change));
			} else {
				int index = getMonsterPartyIndex(0, mons);
				if(index > -1) {
					m_player.getTcpSession().write("Ph" + String.valueOf(index) + mons.getHealth());
					return;
				}
				//TODO: Add support for NPC monster healing for monster in catching nets
			}
		}
	}

	@Override
	public void informStatusApplied(Monster mons, StatusEffect eff) {
		if(m_finished)
			return;
		if (m_player != null) {
			if (getActiveMonster()[0].compareTo(mons) == 0)
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new StatusChangeMessage(0, 
								mons.getSpeciesName(), 
								eff.getName(), false));
			else if(mons.compareTo(getActiveMonster()[1]) == 0)
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new StatusChangeMessage(1, 
								mons.getSpeciesName(), 
								eff.getName(), false));
		}
	}

	@Override
	public void informStatusRemoved(Monster mons, StatusEffect eff) {
		if(m_finished)
			return;
		if (m_player != null) {
			if (getActiveMonster()[0].compareTo(mons) == 0 &&
					!getActiveMonster()[0].isFainted())
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new StatusChangeMessage(0, 
								mons.getSpeciesName(), 
								eff.getName(), true));
			else if(mons.compareTo(getActiveMonster()[1]) == 0 &&
					!getActiveMonster()[1].isFainted())
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new StatusChangeMessage(1, 
								mons.getSpeciesName(), 
								eff.getName(), true));
		}
	}

	@Override
	public void informSwitchInMonster(int trainer, Monster mons) {
		if(m_player != null) {
			if (trainer == 0) {
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new SwitchMessage(m_player.getName(),
								mons.getSpeciesName(),
								trainer,
								getMonsterPartyIndex(trainer, mons)));
			} else {
				TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
						new SwitchMessage(m_npc.getName(),
								mons.getSpeciesName(),
								trainer,
								getMonsterPartyIndex(trainer, mons)));
			}
		}
	}

	@Override
	public void informUseMove(Monster mons, String name) {
		if (m_player != null)
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
					new BattleMoveMessage(mons.getSpeciesName(), name));
	}

	@Override
	public void informVictory(int winner) {
		m_finished = true;
		int money = getParty(1)[0].getLevel() * (getMechanics().getRandom().nextInt(4) + 1);
		if (winner == 0) {
			/* Reward the player */

			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
					new BattleRewardMessage(BattleRewardType.MONEY,
					money));
			m_player.setMoney(m_player.getMoney() + money);
			/* End the battle */
			m_player.removeTempStatusEffects();
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
					new BattleEndMessage(BattleEnd.WON));
			/* Now add Trainer EXP */
			int trainerExp = 0;
			for(int i = 0; i < getParty(1).length; i++) {
				if(getParty(1)[i] != null)
					trainerExp += getParty(1)[i].getLevel() / 2;
			}
			/* If the player got a badge, triple the EXP gained */
			if(m_npc.isGymLeader() && !m_player.hasBadge(m_npc.getBadge()))
				trainerExp *= 2;
			if(trainerExp > 0)
				m_player.addTrainingExp(trainerExp);
			/* Give the player the badge if it's a gym leader */
			if(m_npc.isGymLeader()) {
				m_player.addBadge(m_npc.getBadge());
			}
		} else {
			if(m_player.getMoney() - money >= 0) {
				m_player.setMoney(m_player.getMoney() - money);
			} else {
				m_player.setMoney(0);
			}
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
					new BattleEndMessage(BattleEnd.LOST));
			m_player.lostBattle();
		}
		m_player.updateClientMoney();
		m_player.setBattling(false);
		m_player.setTalking(false);
		dispose();
		if (m_dispatch != null) {
			/*
			 * This very bad programming but shoddy does it and forces us to do
			 * it
			 */
			/*Thread t = m_dispatch;
			m_dispatch = null;
			t.stop(); let the thread manually return.*/
		}
	}

	@Override
	public void queueMove(int trainer, BattleTurn move)
			throws MoveQueueException {
		/* Check if move exists */
		if(move.isMoveTurn() && move.getId() != -1 &&
				getActiveMonster()[trainer].getMove(move.getId()) == null) {
			requestMove(trainer);
			return;
		}
		/* Handle forced switches */
		if(m_isWaiting && m_replace != null && m_replace[trainer]) {
			if(!move.isMoveTurn()) {
				if(getActiveMonster()[trainer].compareTo(this.getParty(trainer)[move.getId()]) != 0) {
					this.switchInMonster(trainer, move.getId());
					m_replace[trainer] = false;
					m_isWaiting = false;
					return;
				}
			}
			requestMonsterReplacement(trainer);
			return;
		}
		/* Queue the move */
		if(m_turn[trainer] == null) {
			/* Handle Monster being unhappy and ignoring you */
			if(trainer == 0 && !getActiveMonster()[0].isFainted()) {
				if(getActiveMonster()[0].getHappiness() <= 40) {
					/* Monster is unhappy, they'll do what they feel like */
					showMessage(getActiveMonster()[0].getSpeciesName() + " is unhappy!");
					int moveID = getMechanics().getRandom().nextInt(4);
					while (getActiveMonster()[0].getMove(moveID) == null)
						moveID = getMechanics().getRandom().nextInt(4);
					move = BattleTurn.getMoveTurn(moveID);
				} else if(getActiveMonster()[0].getHappiness() < 70) {
					/* Monster is partially unhappy, 50% chance they'll listen to you */
					if(getMechanics().getRandom().nextInt(2) == 1) {
						showMessage(getActiveMonster()[0].getSpeciesName() + " is unhappy!");
						int moveID = getMechanics().getRandom().nextInt(4);
						while (getActiveMonster()[0].getMove(moveID) == null)
							moveID = getMechanics().getRandom().nextInt(4);
						move = BattleTurn.getMoveTurn(moveID);
					}
				}
			}
			if (move.getId() == -1) {
				if (m_dispatch == null
						&& ((trainer == 0 && m_turn[1] != null) ||
								(trainer == 1 && m_turn[0] != null))) {
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
				// Handle a fainted monster
				if (this.getActiveMonster()[trainer].isFainted()) {
					if (!move.isMoveTurn() && this.getParty(trainer)[move.getId()] != null
							&& this.getParty(trainer)[move.getId()].getHealth() > 0) {
						switchInMonster(trainer, move.getId());
						requestMoves();
						return;
					} else {
						// The player still has monsters left
						if (getAliveCount(trainer) > 0) {
							requestMonsterReplacement(trainer);
							return;
						} else {
							// the player has no monsters left. Announce winner
							if (trainer == 0)
								this.informVictory(1);
							else
								this.informVictory(0);
							return;
						}
					}
				} else {
					// The turn was used to attack!
					if (move.isMoveTurn()) {
						// Handles Struggle
						if (getActiveMonster()[trainer].mustStruggle())
							m_turn[trainer] = BattleTurn.getMoveTurn(-1);
						else {
							// The move has no more PP
							if (this.getActiveMonster()[trainer].getPp(move
									.getId()) <= 0) {
								if (trainer == 0) {
									TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
											new NoPPMessage(this.getActiveMonster()[trainer]
												.getMoveName(move.getId())));
									requestMove(0);
								} else {
									/* Get another move from the npc */
									requestMove(1);
								}
								return;
							} else {
								// Assign the move to the turn
								m_turn[trainer] = move;
							}
						}
					} else {
						if (this.getActiveMonster()[trainer].isActive() && 
								this.getParty(trainer)[move.getId()] != null &&
								this.getParty(trainer)[move.getId()].getHealth() > 0) {
							m_turn[trainer] = move;
						} else {
							requestMove(trainer);
							return;
						}
					}
				}
			}
		}
		/* Ensures the npc selected a move */
		if(trainer == 0 && m_turn[0] != null && m_turn[1] == null) {
			requestMove(1);
			return;
		}
		if (m_dispatch != null)
			return;
		// Both turns are ready to be performed 
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

	@Override
	public void refreshActiveMonster() {
		m_player.getTcpSession().write(
				"bh0" + this.getActiveMonster()[0].getHealth());
		m_player.getTcpSession().write(
				"bh1" + this.getActiveMonster()[1].getHealth());
	}

	@Override
	public void requestAndWaitForSwitch(int party) {
		requestMonsterReplacement(party);
		if (party == 0) {
			/* Request a switch from the player */
			if (!m_replace[party]) {
				return;
			}
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

	@Override
	protected void requestMove(int trainer) {
		if(trainer == 0) {
			/* Request move from player */
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
					new BattleMoveRequest());
		} else {
			/* Request move from npc */
			try {
				if(getActiveMonster()[1].hasTypeWeakness(getActiveMonster()[0])
						&& this.getAliveCount(1) >= 3) {
					/* The npc should switch out a different Monster */
					/* 50:50 chance they will switch */
					if(this.getMechanics().getRandom().nextInt(3) == 0) {
						int index = 0;
						while(this.getParty(1)[index] == null ||
								this.getParty(1)[index].isFainted() ||
								this.getParty(1)[index].compareTo(getActiveMonster()[1]) == 0) {
							try {
								Thread.sleep(100);
							} catch (Exception e) {}
							index = getMechanics().getRandom().nextInt(6);
						}
						this.queueMove(1, BattleTurn.getSwitchTurn(index));
						return;
					}
				}
				/* If they did not switch, select a move */
				int moveID = getMechanics().getRandom().nextInt(4);
				while (getActiveMonster()[1].getMove(moveID) == null)
					moveID = getMechanics().getRandom().nextInt(4);
				queueMove(1, BattleTurn.getMoveTurn(moveID));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void requestMoves() {
		clearQueue();
		requestMove(1);
		requestMove(0);
	}

	@Override
	protected void requestMonsterReplacement(int i) {
		if(i == 0) {
			/* Request Monster replacement from player */
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
					new SwitchRequest());
		} else {
			/* Request Monster replacement from npc */
			if(getAliveCount(1) == 0) {
				informVictory(0);
			} else {
				try {
					int index = 0;

					while(this.getParty(1)[index] == null ||
							this.getParty(1)[index].isFainted()) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {}
						index = getMechanics().getRandom().nextInt(6);
					}
					this.switchInMonster(1, BattleTurn.getSwitchTurn(index).getId());
	                requestMoves();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void showMessage(String message) {
		if(m_finished)
			return;
		if(m_player != null)
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(), 
				new BattleMessage(message));
	}

	@Override
	public void forceExecuteTurn() {
		if(m_turn[0] == null) {
			m_turn[0] = BattleTurn.getMoveTurn(-1);
		}
		if(m_turn[1] == null) {
			m_turn[1] = BattleTurn.getMoveTurn(-1);
		}
		executeTurn(m_turn);
	}

}
