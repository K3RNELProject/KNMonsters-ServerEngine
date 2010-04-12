package net.k3rnel.server.network.message;

/**
 * A message to display the sprite selector
 * @author shadowkanji
 *
 */
public class SpriteSelectMessage extends K3RNELMessage {
	/**
	 * Constructor
	 */
	public SpriteSelectMessage() {
		m_message = "SS";
	}
}
