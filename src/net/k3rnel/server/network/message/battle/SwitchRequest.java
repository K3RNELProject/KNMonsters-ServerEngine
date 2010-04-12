package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A request to switch Monsters
 * @author shadowkanji
 *
 */
public class SwitchRequest extends K3RNELMessage {
	/**
	 * Constructor
	 */
	public SwitchRequest() {
		m_message = "bs";
	}
}
