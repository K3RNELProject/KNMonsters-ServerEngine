package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Level up during battle
 * @author shadowkanji
 *
 */
public class BattleLevelChangeMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param pokeName
	 * @param level
	 */
	public BattleLevelChangeMessage(String pokeName, int level) {
		m_message = "bl" + pokeName + "," + level;
	}
}
