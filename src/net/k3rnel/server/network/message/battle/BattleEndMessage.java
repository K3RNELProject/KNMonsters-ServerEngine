package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Informs client a battle is over and if they won/lost
 * @author shadowkanji
 *
 */
public class BattleEndMessage extends K3RNELMessage {
	public enum BattleEnd { WON, LOST, CAUGHT }
	/**
	 * Constructor
	 * @param won
	 */
	public BattleEndMessage(BattleEnd b) {
		switch(b) {
		case WON:
			m_message = "b@w";
			break;
		case LOST:
			m_message = "b@l";
			break;
		case CAUGHT:
			m_message = "b@p";
			break;
		}
	}
}
