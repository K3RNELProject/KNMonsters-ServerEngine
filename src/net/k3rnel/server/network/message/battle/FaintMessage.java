package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A message for when a monster faints
 * @author shadowkanji
 *
 */
public class FaintMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param monsName
	 */
	public FaintMessage(String monsName) {
		m_message = "bF" + monsName;
	}

}
