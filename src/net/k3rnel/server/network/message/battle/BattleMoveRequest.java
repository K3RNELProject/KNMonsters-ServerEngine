package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A request to select a battle move
 * @author shadowkanji
 *
 */
public class BattleMoveRequest extends K3RNELMessage {
	/**
	 * Constructor
	 */
	public BattleMoveRequest() {
		m_message = "bm";
	}
}
