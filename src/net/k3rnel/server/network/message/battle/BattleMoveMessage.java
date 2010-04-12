package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A message for when a Monster is using a move
 * @author shadowkanji
 *
 */
public class BattleMoveMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param speciesName
	 * @param moveName
	 */
	public BattleMoveMessage(String speciesName, String moveName) {
		m_message = "bM" + speciesName + "," + moveName;
	}
}
