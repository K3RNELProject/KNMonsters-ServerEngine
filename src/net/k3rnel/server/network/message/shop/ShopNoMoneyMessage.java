package net.k3rnel.server.network.message.shop;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * For when the client has no money
 * @author shadowkanji
 *
 */
public class ShopNoMoneyMessage extends K3RNELMessage {
	/**
	 * Constructor
	 */
	public ShopNoMoneyMessage() {
		m_message = "Sn";
	}
}
