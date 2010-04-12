package net.k3rnel.server.network.message.shop;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * For when player has no bag space
 * @author shadowkanji
 *
 */
public class ShopNoSpaceMessage extends K3RNELMessage {
	/**
	 * Constructor
	 */
	public ShopNoSpaceMessage() {
		m_message = "Sf";
	}
}
