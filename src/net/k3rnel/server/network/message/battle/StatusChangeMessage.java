package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Status change message (e.g. poison, sleep, etc.)
 * @author shadowkanji
 *
 */
public class StatusChangeMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param side
	 * @param monsName
	 * @param effect
	 * @param removed
	 */
	public StatusChangeMessage(int side, String monsName, String effect, boolean removed) {
		if(removed) {
			m_message = "bE" + side + "" + monsName + "," + effect;
		} else {
			m_message = "be" + side + "" + monsName + "," + effect;
		}
	}
}
