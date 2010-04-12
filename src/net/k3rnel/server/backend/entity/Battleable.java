package net.k3rnel.server.backend.entity;

import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.Monster;

import org.simpleframework.xml.Root;

/**
 * Provides an interface for all game objects that can be battled
 * @author shadowkanji
 *
 */
@Root
public interface Battleable {
	public boolean isBattling();

	public String getName();
	public Monster[] getParty();
	public int getBattleId();
	
	public Battleable getOpponent();
	public BattleField getBattleField();
	public void setBattleField(BattleField b);
	public void setParty(Monster[] team);
	public void setBattleId(int battleID);
}
