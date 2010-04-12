package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Battle: Enemy data
 * @author shadowkanji
 *
 */
public class EnemyDataMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param index
	 * @param p
	 */
	public EnemyDataMessage(int index, Monster p) {
		m_message = "bP" + index + "," + p.getName() + "," + p.getLevel() + ","
			+ p.getGender() + "," + p.getHealth() + ","
			+ p.getHealth() + "," + p.getSpeciesNumber()
			+ "," + p.isShiny();
	}
}
