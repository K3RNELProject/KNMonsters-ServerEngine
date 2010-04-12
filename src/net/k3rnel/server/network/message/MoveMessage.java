package net.k3rnel.server.network.message;

import net.k3rnel.server.backend.entity.Char;

/**
 * A character movement message
 * @author shadowkanji
 *
 */
public class MoveMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param d
	 * @param id
	 * @param mY 
	 * @param mX 
	 */
	public MoveMessage(Char c, boolean directionChange) {
		if(directionChange) {
			switch(c.getFacing()) {
			case Up:
				m_message = "cU" + c.getId();
				break;
			case Down:
				m_message = "cD" + c.getId();
				break;
			case Left:
				m_message = "cL" + c.getId();
				break;
			case Right:
				m_message = "cR" + c.getId();
				break;
			}
		} else {
			m_message = "M" + c.getId() + "," + c.getX() + "," + c.getY();
		}
	}

}
