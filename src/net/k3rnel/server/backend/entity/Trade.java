package net.k3rnel.server.backend.entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import net.k3rnel.server.GameServer;
import net.k3rnel.server.backend.entity.TradeOffer.TradeType;
import net.k3rnel.server.battle.DataService;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.MonsterEvolution;
import net.k3rnel.server.battle.MonsterSpecies;
import net.k3rnel.server.battle.MonsterEvolution.EvolutionTypes;
import net.k3rnel.server.network.MySqlManager;

/**
 * A trade between two players
 * @author shadowkanji
 *
 */
public class Trade implements Runnable{
	/* Stores the offers */
	private HashMap<Tradeable, TradeOffer[]> m_offers;
	public boolean m_isExecuting = false;
	private ArrayList<String> m_queries = new ArrayList<String>();
	
	/**
	 * Constructor
	 * @param player1
	 * @param player2
	 */
	public Trade(Tradeable player1, Tradeable player2) {
		m_offers = new HashMap<Tradeable, TradeOffer[]>();
		m_offers.put(player1, null);
		m_offers.put(player2, null);
		/* Block players of same IP address from trading */		
		if(player1.getIpAddress().equalsIgnoreCase(player2.getIpAddress())) {		
			if(player1 instanceof PlayerChar) {		
				PlayerChar p = (PlayerChar) player1;		
				p.getTcpSession().write("!Trading cannot be done with that player");		
			}		
			endTrade();		
			return;		
		}
		if(player1 instanceof PlayerChar) {
			/* Tell the client to open the trade window */
			PlayerChar p = (PlayerChar) player1;
			Char c = (Char) player2;
			p.getTcpSession().write("Ts" + c.getName());
			/*
			 * Send the Monster data of player 2 to player 1
			 */
			Monster[] player2Party = player2.getParty();
			for(int i = 0; i < player2Party.length; i++) {
				if(player2Party[i] != null) {
					p.getTcpSession().write("Ti" + i + MonsterSpecies.getDefaultData().getMonsterByName(player2Party[i].getSpeciesName()).getSpeciesNumber() + "," +
							player2Party[i].getName() + "," +
							player2Party[i].getHealth() + "," +
							player2Party[i].getGender() + "," +
							(player2Party[i].isShiny() ? 1 : 0) + "," +
							player2Party[i].getStat(0) + "," +
							player2Party[i].getStat(1) + "," +
							player2Party[i].getStat(2) + "," +
							player2Party[i].getStat(3) + "," +
							player2Party[i].getStat(4) + "," +
							player2Party[i].getStat(5) + "," +
							player2Party[i].getTypes()[0] + "," +
							(player2Party[i].getTypes().length > 1 &&
									player2Party[i].getTypes()[1] != null ? player2Party[i].getTypes()[1] + "," : ",") +
									player2Party[i].getExp() + "," +
									player2Party[i].getLevel() + "," +
									player2Party[i].getAbilityName() + "," +
									player2Party[i].getNature().getName() + "," +
							(player2Party[i].getMoves()[0] != null ? player2Party[i].getMoves()[0].getName() : "") + "," +
							(player2Party[i].getMoves()[1] != null ? player2Party[i].getMoves()[1].getName() : "") + "," +
							(player2Party[i].getMoves()[2] != null ? player2Party[i].getMoves()[2].getName() : "") + "," +
							(player2Party[i].getMoves()[3] != null ? player2Party[i].getMoves()[3].getName() : ""));
				}
			}
		}
		if(player2 instanceof PlayerChar) {
			/* If player 2 is a PlayerChar, tell client to open trade window */
			PlayerChar p = (PlayerChar) player2;
			Char c = (Char) player1;
			p.getTcpSession().write("Ts" + c.getName());
			/*
			 * Send the Monster data of player 1 to player 2
			 */
			Monster[] player1Party = player1.getParty();
			for(int i = 0; i < player1Party.length; i++) {
				if(player1Party[i] != null) {
					p.getTcpSession().write("Ti" + i + MonsterSpecies.getDefaultData().getMonsterByName(player1Party[i].getSpeciesName()).getSpeciesNumber() + "," +
							player1Party[i].getName() + "," +
							player1Party[i].getHealth() + "," +
							player1Party[i].getGender() + "," +
							(player1Party[i].isShiny() ? 1 : 0) + "," +
							player1Party[i].getStat(0) + "," +
							player1Party[i].getStat(1) + "," +
							player1Party[i].getStat(2) + "," +
							player1Party[i].getStat(3) + "," +
							player1Party[i].getStat(4) + "," +
							player1Party[i].getStat(5) + "," +
							player1Party[i].getTypes()[0] + "," +
							(player1Party[i].getTypes().length > 1 &&
									player1Party[i].getTypes()[1] != null ? player1Party[i].getTypes()[1] + "," : ",") +
									player1Party[i].getExp() + "," +
									player1Party[i].getLevel() + "," +
									player1Party[i].getAbilityName() + "," +
									player1Party[i].getNature().getName() + "," +
							(player1Party[i].getMoves()[0] != null ? player1Party[i].getMoves()[0].getName() : "") + "," +
							(player1Party[i].getMoves()[1] != null ? player1Party[i].getMoves()[1].getName() : "") + "," +
							(player1Party[i].getMoves()[2] != null ? player1Party[i].getMoves()[2].getName() : "") + "," +
							(player1Party[i].getMoves()[3] != null ? player1Party[i].getMoves()[3].getName() : ""));
				}
			}
		}
	}
	
	/**
	 * Sets the offer from a player
	 * @param p
	 * @param o
	 */
	public void setOffer(Tradeable t, int monster, int money) {
		if(t instanceof PlayerChar) {
			PlayerChar p = (PlayerChar) t;
			if(p.getMoney() < money)
				return;
		}
		TradeOffer [] o = new TradeOffer[2];
		o[0] = new TradeOffer();
		o[0].setId(monster);
		o[0].setType(TradeType.MONSTER);
		o[0].setInformation(t.getParty()[monster].getSpeciesName());
		if(monster > -1 && monster < 6) {
			if(!DataService.canTrade(t.getParty()[monster].getSpeciesName())) {
				endTrade();
				return;
			}
		}
		
		o[1] = new TradeOffer();
		o[1].setQuantity(money);
		o[1].setType(TradeType.MONEY);
		
		m_offers.put(t, o);
		/* Send the offer to the other player */
		sendOfferInformation(t, o);
	}
	
	/**
	 * Cancels an offer from a player
	 * @param p
	 */
	public void cancelOffer(Tradeable t) {
		Iterator<Tradeable> it = m_offers.keySet().iterator();
		Tradeable otherPlayer = null;
		/* Find the other player */
		while(it.hasNext()) {
			Tradeable temp = it.next();
			if(temp != t) {
				otherPlayer = temp;
			}
		}
		/* Check the other player hasn't accepted a previous offer */
		if(!otherPlayer.acceptedTradeOffer()) {
			m_offers.put(t, null);
			otherPlayer.receiveTradeOfferCancelation();
		}
	}
	
	/**
	 * Sends offer information from one player to another
	 * @param Tradeable t
	 * @param TradeOffer [] o
	 */
	private void sendOfferInformation(Tradeable t, TradeOffer [] o) {
		Iterator<Tradeable> i = m_offers.keySet().iterator();
		while(i.hasNext()) {
			Tradeable temp = i.next();
			if(temp.getName().compareTo(t.getName()) != 0) {
				temp.receiveTradeOffer(o);
			}
		}
	}
	
	/**
	 * Checks if both player's agree to trade
	 */
	public void checkForExecution() {
		Iterator<Tradeable> i = m_offers.keySet().iterator();
		if(i.next().acceptedTradeOffer() && i.next().acceptedTradeOffer()) {
			try{
				executeTrade();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Executes the trade
	 */
	private void executeTrade() {
		/* Ensure two threads can't cause execute the trade */
		if(!m_isExecuting) {
			m_isExecuting = true;
			Monster [] temp = new Monster[2];
			
			Iterator<Tradeable> it = m_offers.keySet().iterator();
			Tradeable player1 = it.next();
			Tradeable player2 = it.next();
			TradeOffer [] o1 = m_offers.get(player1);
			TradeOffer [] o2 = m_offers.get(player2);
			
			/* Ensure each player has made an offer */
			if(o1 == null || o2 == null)
				return;
			
			/* Keep checking no player has left the trade */
			if(player1 != null && player2 != null) {
				
				/* Store a timestamp of the transaction */
				Date date = new Date();
				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
				
				/* Handle player 1's offers */
				for(int j = 0; j < o1.length; j++) {
					switch(o1[j].getType()) {
					case MONSTER:
						/* 
						 * An id greater than 5 or less an 0 is sent 
						 * if no MONSTER is being traded 
						 */
						if(o1[j].getId() >= 0 && o1[j].getId() <= 5) {
							/* Store the Monster temporarily */
							temp[0] = player1.getParty()[o1[j].getId()];
							if(player1 instanceof PlayerChar && player2 instanceof PlayerChar) {
								player1.getParty()[o1[j].getId()] = null;
								m_queries.add("INSERT into kn_history (member,action,with,timestamp,details) VALUES ('"+((PlayerChar) player1).getId()+"','1','"+((PlayerChar)player2).getId()+"','"+timestamp+"','"+temp[0].getDatabaseID()+"')");
							}
							
						}
						break;
					case MONEY:
						/* Ensure there was money offered */
						if(o1[j].getQuantity() > 0) {
							player1.setMoney(player1.getMoney() - o1[j].getQuantity());
							player2.setMoney(player2.getMoney() + o1[j].getQuantity());
							if(player1 instanceof PlayerChar && player2 instanceof PlayerChar) {
								m_queries.add("INSERT into kn_history (member,action,with,timestamp,details) VALUES ('"+((PlayerChar) player1).getId()+"','0','"+((PlayerChar)player2).getId()+"','"+timestamp+"','"+o1[j].getQuantity()+"')");
							}
						}
						break;
					case ITEM:
						break;
					}
				}
				
				/* Handle player 2's offers */
				for(int j = 0; j < o2.length; j++) {
					switch(o2[j].getType()) {
					case MONSTER:
						/* 
						 * An id greater than 5 or less an 0 is sent 
						 * if no monster is being traded 
						 */
						if(o2[j].getId() >= 0 && o2[j].getId() <= 5) {
							/* Store the Monster temporarily */
							temp[1] = player2.getParty()[o2[j].getId()];
							if(player1 instanceof PlayerChar && player2 instanceof PlayerChar) {
								player2.getParty()[o1[j].getId()] = null;
								m_queries.add("INSERT into kn_history (member,action,with,timestamp,details) VALUES ('"+((PlayerChar) player2).getId()+"','1','"+((PlayerChar)player1).getId()+"','"+timestamp+"','"+temp[1].getDatabaseID()+"')");
							}
						}
						break;
					case MONEY:
						/* Ensure there was money offered */
						if(o2[j].getQuantity() > 0) {
							player2.setMoney(player2.getMoney() - o2[j].getQuantity());
							player1.setMoney(player1.getMoney() + o2[j].getQuantity());
							if(player1 instanceof PlayerChar && player2 instanceof PlayerChar) {
								m_queries.add("INSERT into kn_history (member,action,with,timestamp,details) VALUES ('"+((PlayerChar) player2).getId()+"','0','"+((PlayerChar)player1).getId()+"','"+timestamp+"','"+o2[j].getQuantity()+"')");
							}
						}
						break;
					case ITEM:
						break;
					}
				}
				
				/* Execute the Monster swap */
				if(temp[1] != null) {
					if(player1 instanceof PlayerChar) {
						PlayerChar p = (PlayerChar) player1;
						p.addMonster(temp[1]);
					}
				}
				if(temp[0] != null) {
					if(player2 instanceof PlayerChar) {
						PlayerChar p = (PlayerChar) player2;
						p.addMonster(temp[0]);
					}
				}
				
				/*  Evolution checks */
				for (Monster curMonster : temp) { // do both monsters
					PlayerChar p;
					if(curMonster == temp[0]){
						p = (PlayerChar) player2;
					} else {
						p = (PlayerChar) player1;
					}
					
					int index = p.getMonsterIndex(curMonster);
					
					MonsterSpecies monData = MonsterSpecies.getDefaultData().getMonsterByName(
							curMonster.getSpeciesName());
					
					for (MonsterEvolution currentEvolution : monData.getEvolutions()) {
						System.out.println(curMonster.getName() + " can evolve via " + currentEvolution.getType());
						
						if (currentEvolution.getType().equals(EvolutionTypes.Trade)){
							curMonster.setEvolution(currentEvolution);
							p.getTcpSession().write("PE" + index);
							break;
						} else if (currentEvolution.getType() == EvolutionTypes.TradeItem){
							// TODO: trade item evolving
							// need to check to see if they have the right item
							break;
						}
					}
				}
				
				/* Update the money */
				if(player1 instanceof PlayerChar) {
					PlayerChar p = (PlayerChar) player1;
					p.updateClientMoney();
				}
				if(player2 instanceof PlayerChar) {
					PlayerChar p = (PlayerChar) player2;
					p.updateClientMoney();
				}
				
				/* Store transactions on DB */
				new Thread(this).start();
				
				/* End the trade */
				m_isExecuting = false;
				endTrade();
			}
		}
	}
	
	/**
	 * Returns true if the trade was ended
	 */
	public boolean endTrade() {
		if(!m_isExecuting) {
			Iterator<Tradeable> i = m_offers.keySet().iterator();
			while(i.hasNext()) {
				Tradeable t = i.next();
				if(t != null)
					t.finishTrading();
			}
			m_offers.clear();
			m_offers = null;
			return true;
		}
		return false;
	}

	public void run() {
		/*Record Trade on History Table*/
		MySqlManager m_database = new MySqlManager();
		if(m_database.connect(GameServer.getDatabaseHost(), GameServer.getDatabaseUsername(), GameServer.getDatabasePassword())){
			m_database.selectDatabase(GameServer.getDatabaseName());
			while(!m_queries.isEmpty()){
				m_database.query(m_queries.get(0));
				m_queries.remove(0);
			}
		}
		m_database.close();
	}
}
