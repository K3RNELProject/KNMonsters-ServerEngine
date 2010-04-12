package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A message when running from a battle
 * @author shadowkanji
 *
 */
public class RunMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param success
	 */
	public RunMessage(boolean success) {
		m_message = "br" + (success ? "1" : "2");
	}
}
