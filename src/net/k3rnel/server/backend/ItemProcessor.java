
package net.k3rnel.server.backend;

import java.util.Random;

import net.k3rnel.server.GameServer;
import net.k3rnel.server.backend.entity.PlayerChar;
import net.k3rnel.server.backend.item.Item;
import net.k3rnel.server.backend.item.Item.ItemAttribute;
import net.k3rnel.server.battle.BattleTurn;
import net.k3rnel.server.battle.DataService;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.MonsterEvolution;
import net.k3rnel.server.battle.MonsterSpecies;
import net.k3rnel.server.battle.MonsterEvolution.EvolutionTypes;
import net.k3rnel.server.battle.impl.WildBattleField;
import net.k3rnel.server.battle.mechanics.statuses.BurnEffect;
import net.k3rnel.server.battle.mechanics.statuses.ConfuseEffect;
import net.k3rnel.server.battle.mechanics.statuses.FreezeEffect;
import net.k3rnel.server.battle.mechanics.statuses.ParalysisEffect;
import net.k3rnel.server.battle.mechanics.statuses.PoisonEffect;
import net.k3rnel.server.battle.mechanics.statuses.SleepEffect;


/**
 * Processes an item using a thread
 * 
 * @author shadowkanji
 */
public class ItemProcessor implements Runnable {
	/* An enum which handles Pokeball types */
	public enum CatchNet {
		SMALLNET, GREATNET, ULTRANET, MASTERNET
	};

	private final PlayerChar m_player;
	private final String[]   m_details;

	/**
	 * Constructor
	 * 
	 * @param p
	 * @param details
	 */
	public ItemProcessor(PlayerChar p, String[] details) {
		m_player = p;
		m_details = details;
	}

	/**
	 * Executes the item usage
	 */
	public void run() {
		String[] data = new String[m_details.length - 1];
		for (int i = 1; i < m_details.length; i++)
			data[i - 1] = m_details[i];
		if (useItem(m_player, Integer.parseInt(m_details[0]), data) &&
				!GameServer.getServiceManager().getItemDatabase().getItem(Integer.parseInt(m_details[0])).getName().contains("Rod")) {
			m_player.getBag().removeItem(Integer.parseInt(m_details[0]), 1);
			m_player.getTcpSession().write("Ir" + m_details[0] + "," + 1);
		}
	}

	/**
	 * Uses an item in the player's bag. Returns true if it was used.
	 * 
	 * @param p
	 * @param itemId
	 * @param data
	 *          - extra data received from client
	 * @return
	 */
	public boolean useItem(PlayerChar p, int itemId, String[] data) {
		/* Check that the bag contains the item */
		if (p.getBag().containsItem(itemId) < 0) return false;
		/* We have the item, so let us use it */
		Item i = GameServer.getServiceManager().getItemDatabase().getItem(itemId);
		/* Monster object we might need */
		Monster mon = null;
		try {
			/* Check if the item is a rod */
			if (i.getName().equalsIgnoreCase("OLD ROD")) {
				if(!p.isBattling() && !p.isFishing()) {
					p.fish(0);
					return true;
				}
			} else if(i.getName().equalsIgnoreCase("GOOD ROD")) {
				if(!p.isBattling() && !p.isFishing()) {
					if(p.getFishingLevel() >= 15) {
						p.fish(15);
					} else {
						// Notify client that you need a fishing level of 15 or higher for this rod
						p.getTcpSession().write("FF15");
					}
					return true;
				}
			} else if(i.getName().equalsIgnoreCase("GREAT ROD")) {
				if(!p.isBattling() && !p.isFishing()) {
					if(p.getFishingLevel() >= 50) {
						p.fish(35);
					} else {
						// Notify client that you need a fishing level of 50 or higher for this rod
						p.getTcpSession().write("FF50");
					}
					return true;
				}
			} else if(i.getName().equalsIgnoreCase("ULTRA ROD")) {
				if(!p.isBattling() && !p.isFishing()) {
					if(p.getFishingLevel() >= 70) {
						p.fish(50);
					} else {
						// Notify client that you need a fishing level of 70 or higher for this rod
						p.getTcpSession().write("FF70");
					}
					return true;
				}
			}
			/* Check if the item is a repel or escape rope */
			else if (i.getName().equalsIgnoreCase("REPEL")) {
				p.setRepel(100);
				return true;
			} else if (i.getName().equalsIgnoreCase("SUPER REPEL")) {
				p.setRepel(200);
				return true;
			} else if (i.getName().equalsIgnoreCase("MAX REPEL")) {
				p.setRepel(250);
				return true;
			} else if (i.getName().equalsIgnoreCase("ESCAPE ROPE")) {
				if (p.isBattling()) return false;
				/* Warp the player to their last heal point */
				p.setX(p.getHealX());
				p.setY(p.getHealY());
				p.setMap(GameServer.getServiceManager().getMovementService()
						.getMapMatrix()
						.getMapByGamePosition(p.getHealMapX(), p.getHealMapY()), null);
				return true;
			}
			/* Else, determine what do to with the item */
			if (i.getAttributes().contains(ItemAttribute.MOVESLOT)) {
				/* TMs & HMs */
				try {
					/* Can't use a TM/HM during battle */
					if (p.isBattling()) return false;
					/* Player is not in battle, learn the move */
					mon = p.getParty()[Integer.parseInt(data[0])];
					if (mon == null) return false;
					String moveName = i.getName().substring(5);
					/* Ensure the Monster can learn this move */
					if (DataService.getMoveSetData().getMoveSet(mon.getSpeciesNumber())
							.canLearn(moveName)) {
						mon.getMovesLearning().add(moveName);
						m_player.getTcpSession().write("Pm" + data[0] + moveName);
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else if (i.getAttributes().contains(ItemAttribute.MONSTER)) {
				/* Status healers, hold items, etc. */
				if (i.getCategory().equalsIgnoreCase("POTIONS")) {
					/*
					 * Potions
					 */
					int hpBoost = 0;
					mon = p.getParty()[Integer.parseInt(data[0])];
					String message = "";
					if (mon == null) return false;
					if(i.getId() == 1) { //Potion
                        hpBoost = 20;
                        mon.changeHealth(hpBoost);
                        message = "You used Potion on "+mon.getName()+"/nThe Potion restored 20 HP";
	                } else if(i.getId()==2) {//Super Potion
	                        hpBoost = 50;
	                        mon.changeHealth(hpBoost);
	                        message = "You used Super Potion on "+mon.getName()+"/nThe Super Potion restored 50 HP";
	                } else if(i.getId()==3) { //Hyper Potion
	                        hpBoost = 200;
	                        mon.changeHealth(hpBoost);
	                        message = "You used Hyper Potion on "+mon.getName()+"/nThe Hyper Potion restored 200 HP";
	                } else if(i.getId()==4) {//Max Potion
	                        mon.changeHealth(mon.getRawStat(0));
	                        message = "You used Max Potion on "+mon.getName()+"/nThe Max Potion restored All HP";
	                } else {
	                        return false;
	                }
					if (!p.isBattling()) {
						/* Update the client */
						p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
						p.getTcpSession().write("Ii" + message);
					} else {
						/* Player is in battle, take a hit from enemy */
						p.getBattleField().forceExecuteTurn();
					}
					return true;
				} else if (i.getCategory().equalsIgnoreCase("EVOLUTION")) {
					/* Evolution items can't be used in battle */
					if (p.isBattling()) return false;
					/* Get the pokemon's evolution data */
					mon = p.getParty()[Integer.parseInt(data[0])];
					/* Ensure mon exists */
					if (mon == null) return false;
					MonsterSpecies pokeData = MonsterSpecies.getDefaultData().getMonsterByName(
							mon.getSpeciesName());
					for (int j = 0; j < pokeData.getEvolutions().length; j++) {
						MonsterEvolution evolution = pokeData.getEvolutions()[j];
						/*
						 * Check if this pokemon evolves by item
						 */
						if (evolution.getType() == EvolutionTypes.Item) {
							/*
							 * Check if the item is an evolution stone If so, evolve the
							 * Pokemon
							 */
							if (i.getName().equalsIgnoreCase("FIRE STONE")
									&& evolution.getAttribute().equalsIgnoreCase("FIRESTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("WATER STONE")
									&& evolution.getAttribute().equalsIgnoreCase("WATERSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("THUNDERSTONE")
									&& evolution.getAttribute().equalsIgnoreCase("THUNDERSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("LEAF STONE")
									&& evolution.getAttribute().equalsIgnoreCase("LEAFSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("MOON STONE")
									&& evolution.getAttribute().equalsIgnoreCase("MOONSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("SUN STONE")
									&& evolution.getAttribute().equalsIgnoreCase("SUNSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("SHINY STONE")
									&& evolution.getAttribute().equalsIgnoreCase("SHINYSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("DUSK STONE")
									&& evolution.getAttribute().equalsIgnoreCase("DUSKSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("DAWN STONE")
									&& evolution.getAttribute().equalsIgnoreCase("DAWNSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							} else if (i.getName().equalsIgnoreCase("OVAL STONE")
									&& evolution.getAttribute().equalsIgnoreCase("OVALSTONE")) {
								mon.setEvolution(evolution);
								mon.evolutionResponse(true, p);
								return true;
							}
						}
					}
				} else if (i.getCategory().equalsIgnoreCase("MEDICINE")) {
					mon = p.getParty()[Integer.parseInt(data[0])];
					if (mon == null) return false;
					if(i.getId() == 16) { //Antidote
            			String message = "You used Antidote on "+mon.getName()+"/nThe Antidote restored "+mon.getName()+" status to normal";
            			mon.removeStatus(PoisonEffect.class);
            			if(p.isBattling())
            				p.getBattleField().forceExecuteTurn();
            			else
            				p.getTcpSession().write("Ii" + message);
            			return true;
                    } else if(i.getId() == 17) { //Parlyz Heal
                    	String message = "You used Parlyz Heal on "+mon.getName()+"/nThe Parlyz Heal restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(ParalysisEffect.class);
                    	if(p.isBattling())
                    		p.getBattleField().forceExecuteTurn();
                    	else
                    		p.getTcpSession().write("Ii" + message);
                    	return true;
                    } else if(i.getId() == 18) { //Awakening
                    	String message = "You used Awakening on "+mon.getName()+"/nThe Awakening restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(SleepEffect.class);
                    	if(p.isBattling())
                    		p.getBattleField().forceExecuteTurn();
                    	else
                    		p.getTcpSession().write("Ii" + message);
                    	return true;
                    } else if(i.getId() == 19) { //Burn Heal
                    	String message = "You used Burn Heal on "+mon.getName()+"/nThe Burn Heal restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(BurnEffect.class);
                    	if(p.isBattling())
                    		p.getBattleField().forceExecuteTurn();
                    	else
                    		p.getTcpSession().write("Ii" + message);
                    	return true;
                    } else if(i.getId() == 20) { //Ice Heal
                    	String message = "You used Ice Heal on "+mon.getName()+"/nThe Ice Heal restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(FreezeEffect.class);
                    	if(p.isBattling())
                    		p.getBattleField().forceExecuteTurn();
                    	else
                    		p.getTcpSession().write("Ii" + message);
                    	return true;
                    } else if(i.getId() == 21) { //Full Heal
                    	String message = "You used Full Heal on "+mon.getName()+"/nThe Full Heal restored "+mon.getName()+" status to normal";
                    	mon.removeStatusEffects(true);
                    	if(p.isBattling())
                    		p.getBattleField().forceExecuteTurn();
                    	else
                    		p.getTcpSession().write("Ii" + message);
                    	return true;
                    } else if (i.getName().equalsIgnoreCase("LAVA COOKIE")) {
						// just like a FULL HEAL
						mon.removeStatusEffects(true);
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}
						return true;
					} else if (i.getName().equalsIgnoreCase("OLD GATEAU")) {
						// just like a FULL HEAL
						mon.removeStatusEffects(true);
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}
						return true;
					}
				} else if (i.getCategory().equalsIgnoreCase("FOOD")) {
					mon = p.getParty()[Integer.parseInt(data[0])];
					Random rand = new Random();
					if (mon == null) return false;
					if(i.getId() == 200) { //Cheri Berry
                    	String message = mon.getName()+" ate the Cheri Berry/nThe Cheri Berry restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(ParalysisEffect.class);
                        if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                        	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 201) { //Chesto Berry
                    	String message = mon.getName()+" ate the Chesto Berry/nThe Chesto Berry restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(SleepEffect.class);
                        if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                        	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 202) { //Pecha Berry
                    	String message = mon.getName()+" ate the Pecha Berry/nThe Pecha Berry restored "+mon.getName()+" status to normal";
                        mon.removeStatus(PoisonEffect.class);
                        if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                        	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 203) { //Rawst Berry
                    	String message = mon.getName()+" ate the Rawst Berry/nThe Rawst Berry restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(BurnEffect.class);
                    	if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                        	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 204) { //Aspear Berry
                    	String message = mon.getName()+" ate the Aspear Berry/nThe Aspear Berry restored "+mon.getName()+" status to normal";
                        mon.removeStatus(FreezeEffect.class);
                        if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                        	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 205) { //Leppa Berry
                    	String message = "Leppa Berry had no effect"; // Move selection not completed, temp message TODO. Add support for this
                        int ppSlot = Integer.parseInt(data[1]);
                        if(mon.getPp(ppSlot) + 10 <= mon.getMaxPp(ppSlot))
                        	mon.setPp(ppSlot, mon.getPp(ppSlot) + 10);
                        else
                        	mon.setPp(ppSlot, mon.getMaxPp(ppSlot));
                        if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                        	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 206) { //Oran Berry
                    	String message = mon.getName()+" ate the Oran Berry/nThe Oran Berry restored 10HP";
                    	mon.changeHealth(10);
                        if(!p.isBattling()) {
                        	p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
                        	p.getTcpSession().write("Ii" + message);
                        }
                        else
                        	p.getBattleField().forceExecuteTurn();
                        return true;
                    } else if(i.getId() == 207) { //Persim Berry
                    	String message = mon.getName()+" ate the Persim Berry/nThe Persim Berry restored "+mon.getName()+" status to normal";
                    	mon.removeStatus(ConfuseEffect.class);
                        if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                        	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 208) { //Lum Berry
                    	String message = mon.getName()+" ate the Lum Berry/nThe Lum Berry restored "+mon.getName()+" status to normal";
                        mon.removeStatusEffects(true);
                        if(p.isBattling())
                        	p.getBattleField().forceExecuteTurn();
                        else
                           	p.getTcpSession().write("Ii" + message);
                        return true;
                    } else if(i.getId() == 209) { //Sitrus Berry
                    	String message = mon.getName()+" ate the Sitrus Berry/nThe Sitrus Berry restored 30HP";
                        mon.changeHealth(30);
                        if(!p.isBattling()) {
                          	p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
                        	p.getTcpSession().write("Ii" + message);
                        }
                        else
                        	p.getBattleField().forceExecuteTurn();
                        return true;
                    } else if(i.getId() == 210) { //Figy Berry
                    	String message = mon.getName()+" ate the Figy Berry/nThe Figy Berry restored" +mon.getRawStat(0) / 8+" HP to " +mon.getName()+"!";
                        mon.changeHealth(mon.getRawStat(0) / 8);
                        if(!p.isBattling()) {
                          	p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
                           	p.getTcpSession().write("Ii" + message);
                        }
                        else
                           	p.getBattleField().forceExecuteTurn();
                        return true;
                    } else if(i.getId() == 214) { //Wiki Berry
                    	String message = mon.getName()+" ate the Wiki Berry/nThe Wiki Berry restored" +mon.getRawStat(0) / 8+" HP to " +mon.getName()+"!";
                        mon.changeHealth(mon.getRawStat(0) / 8);
                        if(!p.isBattling()) {
                          	p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
                           	p.getTcpSession().write("Ii" + message);
                        }
                        else
                           	p.getBattleField().forceExecuteTurn();
                        return true;
                    } else if(i.getId() == 212) { //Mago Berry
                    	String message = mon.getName()+" ate the Mago Berry/nThe Mago Berry restored" +mon.getRawStat(0) / 8+" HP to " +mon.getName()+"!";
                        mon.changeHealth(mon.getRawStat(0) / 8);
                        if(!p.isBattling()) {
                          	p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
                           	p.getTcpSession().write("Ii" + message);
                        }
                        else
                           	p.getBattleField().forceExecuteTurn();
                        return true;
                    } else if(i.getId() == 213) { //Aguav Berry
                    	String message = mon.getName()+" ate the Aguav Berry/nThe Aguav Berry restored" +mon.getRawStat(0) / 8+" HP to " +mon.getName()+"!";
                        mon.changeHealth(mon.getRawStat(0) / 8);
                        if(!p.isBattling()) {
                          	p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
                           	p.getTcpSession().write("Ii" + message);
                        }
                        else
                           	p.getBattleField().forceExecuteTurn();
                        return true;
                    } else if(i.getId() == 214) { //Iapapa Berry
                    	String message = mon.getName()+" ate the Iapapa Berry/nThe Iapapa Berry restored" +mon.getRawStat(0) / 8+" HP to " +mon.getName()+"!";
                        mon.changeHealth(mon.getRawStat(0) / 8);
                        if(!p.isBattling()) {
                          	p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
                           	p.getTcpSession().write("Ii" + message);
                        }
                        else
                           	p.getBattleField().forceExecuteTurn();
                        return true;
                    }else if (i.getId() == 800) { //Voltorb Lollipop
						String message = mon.getName()+" ate the Voltorb Lollipop/nThe Lollipop restored 50 HP to " +mon.getName()+"!";
						mon.changeHealth(50);
						int random = rand.nextInt(10);
						if(random <3){
							mon.addStatus(new ParalysisEffect());
							message+="/n"+mon.getName()+" was Paralyzed from the Lollipop!";
						}
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}else{
							p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
							p.getTcpSession().write("Ii" + message);
						}
						return true;
					} else if (i.getId() == 801) { //Sweet Chills
						String message = mon.getName()+" ate the Sweet Chill/nThe Sweet Chill restored " +mon.getName()+"'s moves!";
						for(int ppSlot=0;ppSlot<4;ppSlot++){
							if (mon.getPp(ppSlot) + 5 <= mon.getMaxPp(ppSlot)) {
								mon.setPp(ppSlot, mon.getPp(ppSlot) + 5);
							} else {
								mon.setPp(ppSlot, mon.getMaxPp(ppSlot));
							}
						}
						int random = rand.nextInt(10);
						if(random <3){
							try{
							mon.addStatus(new FreezeEffect());
							message+="/n"+mon.getName()+" was frozen solid from the cold candy!";
							}catch(Exception e){}//Already under a status effect. 
						}
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}else
							p.getTcpSession().write("Ii" + message);
						return true;
					}else if (i.getId() == 802) { //Cinnamon Candy
						String message = mon.getName()+" ate the Cinnamon Candy./nThe Cinnamon Candy restored " +mon.getName()+"'s status to normal!";
						mon.removeStatusEffects(true);
						int random = rand.nextInt(10);
						if(random <3){
							mon.addStatus(new BurnEffect());
							message+="/n"+mon.getName()+" was burned from the candy!";
						}
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}else{
							p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
							p.getTcpSession().write("Ii"+message);
						}
						return true;
					} else if (i.getId() == 803) { //Candy Corn
						String message = mon.getName()+" ate the Candy Corn./n" +mon.getName()+" is happier!";
						int happiness = mon.getHappiness()+15;
						if(happiness<=300)
							mon.setHappiness(happiness);
						else
							mon.setHappiness(300);
						int random = rand.nextInt(10);
						if(random <3){
							mon.addStatus(new PoisonEffect());
							message+="/n"+mon.getName()+" got Poisoned from the rotten candy!";
						}
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}else
							p.getTcpSession().write("Ii"+message);
						return true;
					} else if (i.getId() == 804) { //Chocobar
						String message = mon.getName()+" ate the ChocoBar!/n" +mon.getName()+" is happier!";
						int happiness = mon.getHappiness()+10;
						if(happiness<=300)
							mon.setHappiness(happiness);
						else
							mon.setHappiness(300);
						int random = rand.nextInt(10);
						if(random <=3){
							mon.changeHealth(30);
							message+="/n"+mon.getName()+" recovered 30HP.";
						}
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}else
							p.getTcpSession().write("Ii"+message);
						return true;
					} else if (i.getId() == 805) { //Gummilax
						String message = mon.getName()+" ate the Gummilax./n" +mon.getName()+" is happier!";
						int happiness = mon.getHappiness()+rand.nextInt(30);
						if(happiness<=255)
							mon.setHappiness(happiness);
						else
							mon.setHappiness(255);
						int random = rand.nextInt(10);
						if(random <3){
							mon.addStatus(new ParalysisEffect());
							message+="/nThe gummi was too sweet for "+mon.getName()+"./n"+mon.getName()+" fell asleep!";
						}
						if (p.isBattling()) {
							p.getBattleField().forceExecuteTurn();
						}else
							p.getTcpSession().write("Ii"+message);
						return true;
					} else if (i.getId() == 806) { //Gengum
						String message = mon.getName()+" ate the Gengum.";
						int randHealth = rand.nextInt(100);
						randHealth-=20;
						if(mon.getHealth()+randHealth<0)
							mon.setHealth(1);
						else
							mon.changeHealth(randHealth);
						if(randHealth>0)
							message+="/n"+mon.getName()+" healed "+randHealth+"HP";
						else
							message+="/n"+mon.getName()+" lost "+-randHealth+"HP";
						if (p.isBattling()) {
							p.getBattleField().queueMove(0,BattleTurn.getMoveTurn(-1));
						}else{
							p.getTcpSession().write("Ph" + data[0] + mon.getHealth());
							p.getTcpSession().write("Ii"+message);
						}
						return true;
					}
				}
			} else if (i.getAttributes().contains(ItemAttribute.BATTLE)) {
				/* Catching Nets */
				if (i.getName().equalsIgnoreCase("SMALL NET")) {
					if (p.getBattleField() instanceof WildBattleField) {
						WildBattleField w = (WildBattleField) p.getBattleField();
						if (!w.throwNet(CatchNet.SMALLNET))
							w.queueMove(0, BattleTurn.getMoveTurn(-1));
						return true;
					}
				} else if (i.getName().equalsIgnoreCase("GREAT NET")) {
					if (p.getBattleField() instanceof WildBattleField) {
						WildBattleField w = (WildBattleField) p.getBattleField();
						if (!w.throwNet(CatchNet.GREATNET))
							w.queueMove(0, BattleTurn.getMoveTurn(-1));
						return true;
					}
				} else if (i.getName().equalsIgnoreCase("ULTRA NET")) {
					if (p.getBattleField() instanceof WildBattleField) {
						WildBattleField w = (WildBattleField) p.getBattleField();
						if (!w.throwNet(CatchNet.ULTRANET))
							w.queueMove(0, BattleTurn.getMoveTurn(-1));
						return true;
					}
				} else if (i.getName().equalsIgnoreCase("MASTER NET")) {
					if (p.getBattleField() instanceof WildBattleField) {
						WildBattleField w = (WildBattleField) p.getBattleField();
						if (!w.throwNet(CatchNet.MASTERNET))
							w.queueMove(0, BattleTurn.getMoveTurn(-1));
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
