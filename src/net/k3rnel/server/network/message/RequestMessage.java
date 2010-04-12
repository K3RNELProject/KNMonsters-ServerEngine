package net.k3rnel.server.network.message;

import net.k3rnel.server.backend.entity.PlayerChar.RequestType;

/**
 * A request packet
 * @author shadowkanji
 *
 */
public class RequestMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param t
	 * @param playerName
	 */
	public RequestMessage(RequestType t, String playerName) {
		switch(t) {
		case BATTLE:
			m_message = "rb" + playerName;
			break;
		case TRADE:
			m_message = "rt" + playerName;
			break;
		}
	}
}
