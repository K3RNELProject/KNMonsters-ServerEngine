package net.k3rnel.server.network.message;

/**
 * A message from server to client
 * @author shadowkanji
 *
 */
public class K3RNELMessage {
	protected String m_message = "";
	
	/**
	 * Returns the message
	 * @return
	 */
	public String getMessage() {
		return m_message;
	}
}
