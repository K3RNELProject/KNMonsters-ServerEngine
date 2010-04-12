package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Informs the client a battle has started
 * @author shadowkanji
 *
 */
public class BattleInitMessage extends K3RNELMessage{
	/**
	 * Constructor
	 * @param isWildBattle
	 * @param enemyPartySize
	 */
	public BattleInitMessage(boolean isWildBattle, int enemyPartySize) {
		m_message = "bi" + (isWildBattle ? "1" : "0") + enemyPartySize;
	}
}
