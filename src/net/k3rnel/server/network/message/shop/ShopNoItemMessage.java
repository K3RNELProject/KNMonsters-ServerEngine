package net.k3rnel.server.network.message.shop;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * The player doesn't have the item he's trying to sell
 * @author ZombieBear
 *
 */
public class ShopNoItemMessage extends K3RNELMessage{
	/**
	 * Constructor
	 */
	public ShopNoItemMessage(String item) {
		m_message = "Sd" + item;
	}
}
