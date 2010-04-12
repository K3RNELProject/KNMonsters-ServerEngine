package net.k3rnel.server.network.message;

/**
 * NPC chat message
 * @author shadowkanji
 */
public class TradeNpcSpeechMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param m
	 */
	public TradeNpcSpeechMessage(String m) {
		m_message = "Ct" + m;
	}
}
