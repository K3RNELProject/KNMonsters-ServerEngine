package net.k3rnel.server.backend.entity;

import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.network.TcpProtocolHandler;
import net.k3rnel.server.network.message.TradeNpcSpeechMessage;

/**
 * Represents an NPC that wants to trade Monsters
 * @author shadowkanji
 *
 */
public class TradeChar extends NonPlayerChar implements Tradeable {
	private Trade m_trade = null;
	private boolean m_tradeAccepted = false;
	private PlayerChar m_player;
	/*
	 * Requested Monster data
	 */
	private String m_requestedSpecies = "";
	/*
	 * Offered Monster data
	 */
	private Monster [] m_party;
	
	/**
	 * Constructor
	 */
	public TradeChar() {
		setBadge(-1);
		setHealer(false);
		setPartySize(0);
	}
	
	/**
	 * Sets the Monster the NPC wants
	 * @param species
	 * @param level
	 * @param nature
	 */
	public void setRequestedMonster(String species, int level, String nature) {
		m_requestedSpecies = species;
		//TODO: Add support for levels and natures
	}
	
	/**
	 * Sets the Monster the NPC offers
	 * @param species
	 * @param level
	 */
	public void setOfferedSpecies(String species, int level) {
		m_party = new Monster[1];
		m_party[0] = Monster.getRandomMonster(species, level);
	}
	
	@Override
	public void talkToPlayer(PlayerChar p) {
		m_player = p;
		if(m_trade == null) {
			/* Can trade */
			m_trade = new Trade(this, p);
			p.setTrade(m_trade);
			m_trade.setOffer(this, 0, 0);
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new TradeNpcSpeechMessage("I'm looking for a " + m_requestedSpecies +
							". Want to trade one for my " + m_party[0].getName() + "?"));
		} else {
			/* Can't trade */
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new TradeNpcSpeechMessage("I can't trade with you right now"));
		}
	}

	public boolean acceptedTradeOffer() {
		return m_tradeAccepted;
	}

	public void cancelTrade() {
		m_trade.endTrade();
	}

	public void cancelTradeOffer() {}

	public void finishTrading() {
		m_trade = null;
		m_tradeAccepted = false;
		TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
				new TradeNpcSpeechMessage("Thanks! It's just what I was looking for!"));
	}

	public int getMoney() {
		return 999999;
	}

	public Monster[] getParty() {
		return m_party;
	}

	public Trade getTrade() {
		return m_trade;
	}

	public boolean isTrading() {
		return m_trade != null;
	}

	public void receiveTradeOffer(TradeOffer[] o) {
		if(o[0].getInformation().equalsIgnoreCase(m_requestedSpecies)) {
			//This is the Monster the TradeChar wanted
			setTradeAccepted(true);
		} else {
			//This is the wrong Monster
			TcpProtocolHandler.writeMessage(m_player.getTcpSession(),
					new TradeNpcSpeechMessage("This is not what I'm looking for!\n" +
					"Come back when you find the right Pokemon!"));
		}
	}

	public void receiveTradeOfferCancelation() {}

	public void setMoney(int money) {}

	public void setTrade(Trade t) {
		m_trade = t;
	}

	public void setTradeAccepted(boolean b) {
		m_tradeAccepted = b;
		if(b) {
			m_trade.checkForExecution();
		}
	}

	public String getIpAddress() {
		return "";
	}
}
