package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Health change message
 * @author shadowkanji
 *
 */
public class HealthChangeMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param id
	 * @param healthChange
	 */
	public HealthChangeMessage(int id, int healthChange) {
		m_message = "bh" + id + "," + healthChange;
	}
}
