package net.k3rnel.server.network.message;

/**
 * NPC chat message
 * @author shadowkanji
 */
public class NpcSpeechMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param m
	 */
	public NpcSpeechMessage(String m) {
		m_message = "Cn" + m;
	}
}
