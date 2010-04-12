package net.k3rnel.server.backend.entity;

import net.k3rnel.server.battle.Monster;

/**
 * Represents a Monster box.
 * @author shadowkanji
 *
 */
public class MonsterBox {
	private Monster [] m_monsters;
	
	/**
	 * Sets the monster in this box
	 * @param mons
	 */
	public void setMonsters(Monster [] mons) {
		m_monsters = mons;
	}
	
	/**
	 * Returns all monsters
	 * @return
	 */
	public Monster [] getMonsters() {
		return m_monsters;
	}
	
	/**
	 * Returns a specific monster
	 * @param i
	 * @return
	 */
	public Monster getMonster(int i) {
		return m_monsters[i];
	}
	
	/**
	 * Sets a specific monster
	 * @param index
	 * @param p
	 */
	public void setMonster(int index, Monster m) {
		m_monsters[index] = m;
	}
}
