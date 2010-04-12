package net.k3rnel.server.network.message.shop;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A packet for when an item is bought
 * @author shadowkanji
 *
 */
public class ShopBuyMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param itemId
	 */
	public ShopBuyMessage(int itemId) {
		m_message = "Sb" + itemId;
	}
}
