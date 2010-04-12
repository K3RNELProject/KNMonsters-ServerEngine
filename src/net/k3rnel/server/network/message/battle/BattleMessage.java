package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A battle message which doesn't fit into other categories
 * @author shadowkanji
 *
 */
public class BattleMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param message
	 */
	public BattleMessage(String message) {
		m_message = "b!" + message;
	}
}
