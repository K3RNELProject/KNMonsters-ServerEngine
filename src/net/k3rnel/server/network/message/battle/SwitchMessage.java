package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Switch in Monster
 * @author shadowkanji
 *
 */
public class SwitchMessage extends K3RNELMessage {
	/**
	 * Constructor
	 * @param playerName
	 * @param pokemonSpecies
	 * @param trainer
	 * @param partyIndex
	 */
	public SwitchMessage(String playerName, String monsterSpecies, int trainer, int partyIndex) {
		m_message = "bS" + playerName + "," + monsterSpecies
			+ "," + trainer + "," + partyIndex;
	}
}
