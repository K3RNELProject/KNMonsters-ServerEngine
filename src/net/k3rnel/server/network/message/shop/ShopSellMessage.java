package net.k3rnel.server.network.message.shop;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A packet for when an item is sold
 * @author ZombieBear
 *
 */
public class ShopSellMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param itemId
	 */
	public ShopSellMessage(int itemId) {
		m_message = "Ss" + itemId;
	}
}

