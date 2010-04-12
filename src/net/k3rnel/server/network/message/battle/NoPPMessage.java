package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Informs client no pp is left
 * @author shadowkanji
 *
 */
public class NoPPMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param moveName
	 */
	public NoPPMessage(String moveName) {
		m_message = "bp" + moveName;
	}
}
