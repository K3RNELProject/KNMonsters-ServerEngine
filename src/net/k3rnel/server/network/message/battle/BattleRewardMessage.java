package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A reward from a battle
 * @author shadowkanji
 *
 */
public class BattleRewardMessage extends K3RNELMessage {
	public enum BattleRewardType { MONEY, ITEM };
	
	/**
	 * Constructor
	 * @param b
	 * @param i
	 */
	public BattleRewardMessage(BattleRewardType b, int i) {
		switch(b) {
		case MONEY:
			m_message = "b$" + i;
			break;
		case ITEM:
			m_message = "bI" + i;
			break;
		}
	}
}
