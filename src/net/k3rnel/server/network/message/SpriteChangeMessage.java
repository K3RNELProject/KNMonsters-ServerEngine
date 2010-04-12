package net.k3rnel.server.network.message;

/**
 * A sprite change packet
 * @author shadowkanji
 *
 */
public class SpriteChangeMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param playerId
	 * @param sprite
	 */
	public SpriteChangeMessage(int playerId, int sprite) {
		m_message = "cS" + playerId + "," + sprite;
	}
}
