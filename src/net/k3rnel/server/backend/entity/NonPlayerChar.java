package net.k3rnel.server.backend.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.k3rnel.server.battle.DataService;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.impl.NpcBattleField;
import net.k3rnel.server.network.TcpProtocolHandler;
import net.k3rnel.server.network.message.NpcSpeechMessage;
import net.k3rnel.server.network.message.SpriteSelectMessage;
import net.k3rnel.server.network.message.shop.ShopStockMessage;

/**
 * Represents a Non Playable Character
 * @author shadowkanji
 *
 */
public class NonPlayerChar extends Char {
	/*
	 * Trainers can have an more than 6 possible Monsters.
	 * When a battle is started with this NPC, it'll check the min party size.
	 * If you have a party bigger than min party,
	 * it'll generate a party of random size between minParty and your party size + 1.
	 * (Unless your party size is 6)
	 */
	private HashMap<String, Integer> m_possibleMonsters;
	private int m_minPartySize = 1;
	private boolean m_isBox = false;
	private boolean m_isHeal = false;
	private int m_isShop = 0;
	private int m_badge = -1;
	private ArrayList<Integer> m_speech;
	private Shop m_shop = null;
	private long m_lastBattle = 0;
	
	/**
	 * Constructor
	 */
	public NonPlayerChar() {}
	
	/**
	 * Returns a string of this npcs speech ids
	 * @param p
	 */
	private String getSpeech() {
		String result = "";
		for(int i = 0; i < m_speech.size(); i++) {
			result = result + m_speech.get(i) + ",";
		}
		return result;
	}
	
	/**
	 * Returns true if this NPC can battle
	 * @return
	 */
	public boolean canBattle() {
		return m_lastBattle == 0;
	}
	
	/**
	 * Sets the time this NPC last battled
	 * @param l
	 */
	public void setLastBattleTime(long l) {
		/* Only set this if they are not gym leaders */
		if(!isGymLeader())
			m_lastBattle = l;
	}
	
	/**
	 * Returns the time this NPC last battled
	 * NOTE: Is valued 0 if the NPC is able to battle
	 * @return
	 */
	public long getLastBattleTime() {
		return m_lastBattle;
	}
	
	/**
	 * Challenges a player (NOTE: Should only be called from NpcBattleLauncher)
	 * @param p
	 */
	public void challengePlayer(PlayerChar p) {
		String speech = this.getSpeech();
		if(!speech.equalsIgnoreCase("")) {
			TcpProtocolHandler.writeMessage(p.getTcpSession(), new NpcSpeechMessage(speech));
		}
	}
	
	/**
	 * Talks to a player
	 * @param p
	 */
	public void talkToPlayer(PlayerChar p) {
		if(isTrainer()) {
			if(canBattle()) {
				String speech = this.getSpeech();
				if(!speech.equalsIgnoreCase("")) {
					TcpProtocolHandler.writeMessage(p.getTcpSession(), new NpcSpeechMessage(speech));
				}
				setLastBattleTime(System.currentTimeMillis());
				p.setBattling(true);
				p.setBattleField(new NpcBattleField(DataService.getBattleMechanics(), p, this));
				return;
			} else {
				p.setTalking(false);
				return;
			}
		} else {
			/* If this NPC wasn't a trainer, handle other possibilities */
			String speech = this.getSpeech();
			if(!speech.equalsIgnoreCase("")) {
				if(!p.isShopping()) {
					//Dont send if player is shopping!
					TcpProtocolHandler.writeMessage(p.getTcpSession(), new NpcSpeechMessage(speech));
				}
			}
			/* If this NPC is a sprite selection npc */
			if(m_name.equalsIgnoreCase("Spriter")) {
				p.setSpriting(true);
				TcpProtocolHandler.writeMessage(p.getTcpSession(), new SpriteSelectMessage());
				return;
			}
			/* Box access */
			if(m_isBox) {
				//Send the data for the player's first box, they may change this later
				p.setTalking(false);
				p.setBoxing(true);
				p.sendBoxInfo(0);
			}
			/* Healer */
			if(m_isHeal) {
				p.healMonsters();
				p.setLastHeal(p.getX(), p.getY(), p.getMapX(), p.getMapY());
			}
			/* Shop access */
			if(m_isShop>0) { //0 is not a shop, over 0 means some kind of shop. 
				//Send shop packet to display shop window clientside
				if(!p.isShopping()){ //Dont display if user's shopping
					TcpProtocolHandler.writeMessage(p.getTcpSession(), new ShopStockMessage(m_shop.getStockData()));
					p.setShopping(true);
					p.setShop(m_shop);
				}
			}
		}
	}
	
	/**
	 * Returns true if this npc can see the player
	 * @param p
	 * @return
	 */
	public boolean canSee(PlayerChar p) {
		if(!p.isBattling() && !isGymLeader() && canBattle()) {
			Random r = new Random();
			switch(this.getFacing()) {
			case Up:
				if(p.getY() >= this.getY() - (32 * (r.nextInt(4) + 1)))
					return true;
				break;
			case Down:
				if(p.getY() <= this.getY() + (32 * (r.nextInt(4) + 1)))
					return true;
				break;
			case Left:
				if(p.getX() >= this.getX() - (32 * (r.nextInt(4) + 1)))
					return true;
				break;
			case Right:
				if(p.getX() <= this.getX() + (32 * (r.nextInt(4) + 1)))
					return true;
				break;
			}
		}
		return false;
	}
	
	/**
	 * Adds speech to this npc
	 * @param id
	 */
	public void addSpeech(int id) {
		if(m_speech == null)
			m_speech = new ArrayList<Integer>();
		m_speech.add(id);
	}
	
	/**
	 * Returns true if the npc is a gym leader
	 * @return
	 */
	public boolean isGymLeader() {
		return m_badge > -1;
	}
	
	/**
	 * Returns true if an NPC is a trainer
	 * @return
	 */
	public boolean isTrainer() {
		return m_possibleMonsters != null && m_minPartySize > 0 && m_possibleMonsters.size() > 0;
	}
	
	/**
	 * Return true if this npc heals your pokemon
	 * @return
	 */
	public boolean isHealer() {
		return m_isHeal;
	}
	
	/**
	 * Sets if this npc is a healer or not
	 * @param b
	 */
	public void setHealer(boolean b) {
		m_isHeal = b;
	}
	
	/**
	 * Returns true if this npc is a shop keeper
	 * @return
	 */
	public boolean isShopKeeper() {
		if(m_isShop>0)
			return true;
		else
			return false;
	}
	
	/**
	 * Returns true if this npc allows box access
	 * @return
	 */
	public boolean isBox() {
		return m_isBox;
	}
	
	/**
	 * Sets if this npc allows box access
	 * @param b
	 */
	public void setBox(boolean b) {
		m_isBox = b;
	}
	
	/**
	 * Sets if this npc is a shop keeper
	 * @param b
	 */
	public void setShopKeeper(int b) {
		m_isShop = b;
		if(b>0) {
			try{
			m_shop = new Shop(b);
			m_shop.start();
			} catch (Exception e){e.printStackTrace();}
		}
	}
	
	/**
	 * Sets the possible Monsters this trainer can have
	 * @param mons
	 */
	public void setPossibleMonsters(HashMap<String, Integer> mons) {
		m_possibleMonsters = mons;
	}
	
	/**
	 * Sets the badge this npc gives, if any
	 * @param i
	 */
	public void setBadge(int i) {
		m_badge = i;
	}
	
	/**
	 * Returns the number of the badge this npc gives. -1 if no badge.
	 * @return
	 */
	public int getBadge() {
		return m_badge;
	}
	
	/**
	 * Sets the minimum sized party this npc should have
	 * @param size
	 */
	public void setPartySize(int size) {
		m_minPartySize = (size > 6 ? 6 : size);
	}
	
	/**
	 * Returns a dynamically generated Monster party based on how well trained a player is
	 * @param p
	 * @return
	 */
	public Monster [] getParty(PlayerChar p) {
		Monster [] party = new Monster[6];
		Monster mon;
		int level;
		String name;
		Random r = DataService.getBattleMechanics().getRandom();
		if(isGymLeader()) {
			if(p.getBadgeCount() > 7) {
				/* If a player has 8 badges, level 80s all round */
				for(int i = 0; i < 6; i++) {
					name = (String) m_possibleMonsters.keySet().toArray()[r.nextInt(m_possibleMonsters.keySet().size())];
					level = 80;
					mon = Monster.getRandomMonster(name, level);
					party[i] = mon;
				}
			} else {
				/* If a player hasn't got 8 badges, give them normal levels */
				for(int i = 0; i < m_minPartySize; i++) {
					name = (String) m_possibleMonsters.keySet().toArray()[r.nextInt(m_possibleMonsters.keySet().size())];
					level = m_possibleMonsters.get(name);
					mon = Monster.getRandomMonster(name, level);
					party[i] = mon;
				}
			}
		} else {
			int playerPartySize = p.getPartyCount();
			if(m_minPartySize < playerPartySize && m_possibleMonsters.size() >= playerPartySize + 1) {
				/*
				 * The player has more Monsters, generate a random party
				 */
				/*
				 * First, get a random party size that is greater than m_minPartySize
				 * and less than or equal to the amount of monsters in the player's party + 1
				 */
				int pSize = r.nextInt(playerPartySize + 1 > 6 ? 6 : playerPartySize + 1);
				while(pSize < m_minPartySize) {
					pSize = r.nextInt(playerPartySize + 1 > 6 ? 6 : playerPartySize + 1);
				}
				/*
				 * Now generate the random Monster
				 */
				for(int i = 0; i <= pSize; i++) {
					//Select a random Monster
					name = (String) m_possibleMonsters.keySet().toArray()[r.nextInt(m_possibleMonsters.keySet().size())];
					level = m_possibleMonsters.get(name);
					/* Level scaling */
					/*while(level < p.getHighestLevel() - 3) {
						level = r.nextInt(p.getHighestLevel() + 5);
					}*/
					mon = Monster.getRandomMonster(name, level);
					party[i] = mon;
				}
			} else {
				/*
				 * Generate a party of size m_minPartySize
				 */
				for(int i = 0; i < m_minPartySize; i++) {
					//Select a random Monster from this list of possible Monsters
					name = (String) m_possibleMonsters.keySet().toArray()[r.nextInt(m_possibleMonsters.keySet().size())];
					level = m_possibleMonsters.get(name);
					//Ensure levels are the similiar
					/*while(level < p.getHighestLevel() - 3) {
						level = r.nextInt(p.getHighestLevel() + 5);
					}*/
					mon = Monster.getRandomMonster(name, level);
					party[i] = mon;
				}
			}
		}
		return party;
	}
}
