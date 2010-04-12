
package net.k3rnel.server.network.message.battle;

import net.k3rnel.server.network.message.K3RNELMessage;

/**
 * Exp gain during battle
 * 
 * @author shadowkanji
 */
public class BattleExpMessage extends K3RNELMessage {
  /**
   * Constructor
   * 
   * @param pokeName
   * @param exp
   */
  public BattleExpMessage(String pokeName, double exp, double rem) {
    m_message = "b." + pokeName + "," + exp + "," + rem;
  }
}
