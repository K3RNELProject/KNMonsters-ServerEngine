package net.k3rnel.server.network.message.shop;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * A message containing a shop's stock
 * @author shadowkanji
 *
 */
public class ShopStockMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param stock
	 */
	public ShopStockMessage(String stock) {
		m_message = "Sl" + stock;
	}
}
