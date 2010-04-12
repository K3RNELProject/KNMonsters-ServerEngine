/*
 * BattleField.java
 *
 * Created on December 19, 2006, 4:05 PM
 *
 * This file is a part of Shoddy Battle.
 * Copyright (C) 2006  Colin Fitzpatrick
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, visit the Free Software Foundation, Inc.
 * online at http://gnu.org.
 */

package net.k3rnel.server.battle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.k3rnel.server.backend.entity.PlayerChar;
import net.k3rnel.server.battle.mechanics.BattleMechanics;
import net.k3rnel.server.battle.mechanics.ModData;
import net.k3rnel.server.battle.mechanics.MoveQueueException;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.ValidationException;
import net.k3rnel.server.battle.mechanics.clauses.Clause;
import net.k3rnel.server.battle.mechanics.moves.MoveList;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;
import net.k3rnel.server.battle.mechanics.moves.MoveList.SpeedSwapEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatusEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.FieldEffect;

/**
 *
 * @author Colin
 */
public abstract class BattleField {

	/*
	 * The number of people who can actually participate. This could be four
	 * later, but for now it will always be two.
	 */
	protected int m_participants = 2;
	/*
	 * Stores if we are waiting for a switch
	 */
	protected boolean m_isWaiting = false;
	/*
	 * Store lists of spectators and effects
	 */
	private ArrayList<PlayerChar> m_spectators = new ArrayList<PlayerChar>();
	protected ArrayList<FieldEffect> m_effects = new ArrayList<FieldEffect>();
	/*
	 * The Monsters in this battlefield
	 */
	protected Monster[][] m_monsters;
	protected int[] m_active = { 0, 0 };
	private BattleMechanics m_mechanics;
	private boolean m_narration = true;
	/*
	 * Needed for request and wait for switch
	 * Tells battle threadlets if the player forced to switch, has switched
	 */
	protected boolean [] m_replace;
	/*
	 * The dispatch thread
	 */
	protected Thread m_dispatch = null;

	// Cache of Struggle.
	private static final MoveListEntry m_struggle = MoveList.getDefaultData().getMove("Struggle");

	public static MoveListEntry getStruggle() {
		return m_struggle;
	}

	/**
	 * Forces move executions
	 */
	public abstract void forceExecuteTurn();

	/**
	 * Adds a spectator to the battle
	 * @param p
	 */
	public void addSpectator(PlayerChar p) {
		m_spectators.add(p);
	}

	/**
	 * Removes a spectator from the battle
	 * @param p
	 */
	public void removeSpectator(PlayerChar p) {
		m_spectators.remove(p);
	}

	/**
	 * Return whether narration is enabled.
	 */
	public boolean isNarrationEnabled() {
		return m_narration;
	}

	/**
	 * Set whether to narrate the battle.
	 */
	public void setNarrationEnabled(boolean enabled) {
		m_narration = enabled;
	}

	/**
	 * Get the effectiveness of a move against a given monster on this field.
	 */
	public double getEffectiveness(MonsterType move, MonsterType monster, boolean enemy) {
		return Monster.getEffectiveness(m_effects, move, monster, enemy);
	}

	/**
	 * Detaches the battlefield from all monsters
	 */
	private void detachField() {
		for (int i = 0; i < m_monsters.length; ++i) {
			detachField(i);
		}
	}

	/**
	 * Detaches battlefield from a specific party
	 * @param i
	 */
	private void detachField(int i) {
		Monster[] team = m_monsters[i];
		for (int j = 0; j < team.length; ++j) {
			if(team[j] != null)
				team[j].detachField();
		}
	}

	/**
	 * Dispose of this object by breaking all links to other objects, making it
	 * easy to the garbage collector to find and free them.
	 */
	public void dispose() {
		detachField();
		m_spectators = null;
		m_effects = null;
		m_monsters = null;
		m_active = null;
		m_mechanics = null;
	}

	/** Creates a new instance of BattleField */
	public BattleField(BattleMechanics mech, Monster[][] monsters) {
		m_mechanics = mech;
		m_monsters = monsters;
		/*
		 * Set m_hasSwitched to 4 to allow for 2 v 2 battles in future
		 */
		m_replace = new boolean[4];
		for(int i = 0; i < m_replace.length; i++)
			m_replace[i] = false;
		attachField();
		setMonsters(monsters);
	}

	/**
	 * Applies weather to the battlefield.
	 * Must be implemented by children classes.
	 */
	public abstract void applyWeather();

	/**
	 * Allows children to construct without pokemon.
	 * @param mech
	 */
	protected BattleField(BattleMechanics mech) {
		m_mechanics = mech;
	}

	/**
	 * Get the mechanics used on this battle field.
	 */
	public BattleMechanics getMechanics() {
		return m_mechanics;
	}

	/**
	 * Return the instance of Random used on this BattleField.
	 */
	public Random getRandom() {
		return m_mechanics.getRandom();
	}

	protected void setMonsters(Monster[][] monsters) {
		Monster[] active = getActiveMonster();
		sortBySpeed(active);
		for (int i = 0; i < active.length; ++i) {
			Monster mon = active[i];
			if(mon != null) {
				applyEffects(mon);
				mon.switchIn();
			}
		}
	}

	/**
	 * Get a party.
	 */
	 public Monster[] getParty(int idx) throws IllegalArgumentException {
		 if ((idx < 0) || (idx >= m_participants)) {
			 throw new IllegalArgumentException("0 <= idx < participants");
		 }
		 return m_monsters[idx];
	 }

	 /**
	  * Attach this field to all of its monster.
	  */
	 protected void attachField() {
		 for (int i = 0; i < m_monsters.length; ++i) {
			 attachField(i);
		 }
	 }

	 public void attachField(int i) {
		 Monster[] team = m_monsters[i];
		 for (int j = 0; j < team.length; ++j) {
			 if(team[j] != null)
				 team[j].attachToField(this, i, j);
		 }
	 }

	 /**
	  * Get the active monster.
	  */
	 public Monster[] getActiveMonster() {
		 if ((m_monsters == null)
				 || (m_monsters[0] == null)
				 || (m_monsters[1] == null))
			 return null;
		 return new Monster[] {
				 m_monsters[0][m_active[0]],
				 m_monsters[1][m_active[1]]
		 };
	 }

	 /**
	  * Apply a new FieldEffect to this BattleField.
	  * Note that the actual effect passed in is not used -- it is cloned via
	  * eff.getFieldCopy(), not eff.clone(), the latter of which should return
	  * the same object.
	  */
	 public boolean applyEffect(FieldEffect eff) {
		 if(eff != null) {
			 Iterator<FieldEffect> i = m_effects.iterator();
			 while (i.hasNext()) {
				 FieldEffect j = (FieldEffect)i.next();
				 if (j.isRemovable()) continue;
				 if (eff.equals(j)) return false;

				 if (eff.isExclusiveWith(j)) {
					 // FieldEffects overwrite each other rather than failing if
					 // another in their class is present.
					 removeEffect(j);
					 // We know that no other statuses can possibly be in this
					 // category, so it is safe to skip the rest of this loop.
					 break;
				 }
			 }

			 FieldEffect applied = eff.getFieldCopy();
			 if (!applied.applyToField(this)) return false;

			 m_effects.add(applied);

			 // Apply to each monster in the field.
			 Monster[] active = getActiveMonster();
			 if (active != null) {
				 for (int j = 0; j < active.length; ++j) {
					 active[j].addStatus(null, applied);
				 }
			 }
			 return true;
		 }
		 return false;
	 }

	 /**
	  * Return whether a client's team is valid.
	  */
	 public void validateTeam(Monster[] team, int idx) throws ValidationException {
		 final int length = team.length;
		 if ((length < 1) || (length > 6)) {
			 throw new ValidationException("The team is an invalid size.");
		 }
		 ModData data = ModData.getDefaultData();
		 for (int i = 0; i < length; ++i) {
			 team[i].validate(data);
		 }

		 // Check any clauses.
		 Collections.sort(m_effects, new Comparator<Object>() {
			 public int compare(Object o1, Object o2) {
				 StatusEffect e1 = (StatusEffect)o1;
				 StatusEffect e2 = (StatusEffect)o2;
				 return e1.getTier() - e2.getTier();
			 }
		 });
		 Iterator<FieldEffect> i = m_effects.iterator();
		 while (i.hasNext()) {
			 StatusEffect eff = (StatusEffect)i.next();
			 if (eff instanceof Clause) {
				 Clause clause = (Clause)eff;
				 if (!clause.isTeamValid(this, team, idx)) {
					 throw new ValidationException("The team violates "
							 + clause.getClauseName() + ".");
				 }
			 }
		 }
	 }

	 /**
	  * Synchronise FieldEffects.
	  */
	 public void synchroniseFieldEffects() {
		 if(m_effects == null)
			 return;
		 Iterator<FieldEffect> i = m_effects.iterator();
		 while (i.hasNext()) {
			 StatusEffect eff = (StatusEffect)i.next();
			 if (eff.isRemovable()) {
				 i.remove();
			 }
		 }
	 }

	 /**
	  * Remove a FieldEffect from this field.
	  */
	 public void removeEffect(FieldEffect eff) {
		 Monster[] active = getActiveMonster();
		 for (int i = 0; i < active.length; ++i) {
			 eff.unapply(active[i]);
		 }
		 eff.unapplyToField(this);
		 eff.disable();
	 }

	 /**
	  * Returns the first instance of an effect of a certain class that is
	  * applied to the BattleField.
	  */
	 @SuppressWarnings("unchecked")
	 public FieldEffect getEffectByType(Class type) {
		 ArrayList list = getEffectsByType(type);
		 if (list.size() == 0) {
			 return null;
		 }
		 return (FieldEffect)list.get(0);
	 }

	 /**
	  * Returns a list of the effects of a certain class that are applied to
	  * this BattleField.
	  */
	 @SuppressWarnings("unchecked")
	 public ArrayList<FieldEffect> getEffectsByType(Class type) {
		 ArrayList<FieldEffect> ret = new ArrayList<FieldEffect>();
		 Iterator<FieldEffect> i = m_effects.iterator();
		 while (i.hasNext()) {
			 FieldEffect effect = (FieldEffect)i.next();
			 if ((effect == null) || (!effect.isActive())) {
				 continue;
			 }
			 if (type.isAssignableFrom(effect.getClass())) {
				 ret.add(effect);
			 }
		 }
		 return ret;
	 }

	 /**
	  * Obtain a replacement monster for the team identified by the parameter.
	  */
	 protected abstract void requestMonsterReplacement(int i);

	 /**
	  * Narrate the battle.
	  */
	 public abstract void showMessage(String message);

	 /**
	  * Refresh all active monsters. The exact meaning of this is for the
	  * implementation to decide.
	  */
	 public abstract void refreshActiveMonster();

	 /**
	  * Get the index of a trainer from one of his monsters.
	  * @param mon a Monster who with 100% certainty belongs to one of the clients
	  */
	 public int getMonsterTrainer(Monster mon) {
		 Monster[] team = m_monsters[0];
		 for (int i = 0; i < team.length; ++i) {
			 if (team[i] == mon) {
				 return 0;
			 }
		 }
		 return 1;
	 }

	 /**
	  * Gets the party index of a monster
	  * @param mon
	  * @return
	  */
	 public int getMonsterPartyIndex(int trainer, Monster mon) {
		 /* Check player 1 */
		 for (int i = 0; i < m_monsters[trainer].length; i++){
			 if (m_monsters[trainer][i] != null && m_monsters[trainer][i].compareTo(mon) == 0) {
				 return i;
			 }
		 }
		 /* This Monster came out of nowhere it seems */
		 return -1;
	 }

	 /**
	  * Get the name of a trainer by number.
	  */
	 public abstract String getTrainerName(int idx);

	 /**
	  * Request moves for the next turn from both players
	  */
	 protected abstract void requestMoves();

	 /**
	  * Requests a move from a specific player
	  * @param trainer
	  */
	 protected abstract void requestMove(int trainer);

	 /**
	  * Inform that a monster's health was changed.
	  */
	 public abstract void informMonsterHealthChanged(Monster mon, int change);

	 /**
	  * Inform that a status was applied to a monster.
	  */
	 public abstract void informStatusApplied(Monster mon, StatusEffect eff);

	 /**
	  * Inform that a status effect was removed from a monster.
	  */
	 public abstract void informStatusRemoved(Monster mon, StatusEffect eff);

	 /**
	  * Inform that a monster was switched in.
	  */
	 public abstract void informSwitchInMonster(int trainer, Monster mon);

	 /**
	  * Inform that a monster fainted.
	  */
	 public abstract void informMonsterFainted(int trainer, int idx);

	 /**
	  * Inform that a monster used a move.
	  *
	  * @param mon the monster who used the move
	  * @param name the name of the move that was used
	  */
	 public abstract void informUseMove(Monster mon, String name);

	 /**
	  * Apply field effects to a monster.
	  */
	 private void applyEffects(Monster mon) {
		 Iterator<FieldEffect> i = m_effects.iterator();
		 while (i.hasNext()) {
			 FieldEffect eff = (FieldEffect)i.next();
			 if (!eff.isRemovable()) {
				 mon.addStatus(null, eff);
			 }
		 }
	 }

	 /**
	  * Switch in a monster and apply FieldEffects to it.
	  */
	 public void switchInMonster(int trainer, int idx) {
		 m_monsters[trainer][m_active[trainer]].switchOut();
		 m_active[trainer] = idx;
		 Monster poke = m_monsters[trainer][idx];
		 informSwitchInMonster(trainer, poke);

		 applyEffects(poke);
		 poke.switchIn();
	 }

	 /**
	  * Inform that a player has won.
	  */
	 public abstract void informVictory(int winner);

	 /**
	  * Returns the queued battle turns
	  * @return
	  */
	 public abstract BattleTurn[] getQueuedTurns();

	 /**
	  * Queue a move.
	  */
	 public abstract void queueMove(int trainer, BattleTurn move)
	 throws MoveQueueException;

	 /**
	  * Wait for a player to switch monster.
	  */
	 public abstract void requestAndWaitForSwitch(int party);

	 /**
	  * Get the opponent of the Monster passed in.
	  */
	 public Monster getOpponent(Monster p) {
		 int idx = getMonsterTrainer(p);
		 int opponent = (idx == 0) ? 1 : 0;
		 return m_monsters[opponent][m_active[opponent]];
	 }

	 /**
	  * Replace a fainted monster.
	  */
	 public void replaceFaintedMonster(int party, int monster, boolean search) {
		 if ((monster < 0) || (monster > 5)) {
			 return;
		 }
		 switchInMonster(party, monster);
		 if (!search)
			 return;
		 for (int i = 0; i < 2; ++i) {
			 if (m_monsters[i][m_active[i]].isFainted()) {
				 requestMonsterReplacement(i);
				 return;
			 }
		 }
		 requestMoves();
	 }

	 /**
	  * Execute a turn.
	  */
	 private void executeTurn(BattleTurn turn, int source, int target) {
		 Monster psource = m_monsters[source][m_active[source]];        
		 if (psource.isFainted()) {
			 return;
		 }

		 if (!turn.isMoveTurn()) {
			 switchInMonster(source, turn.getId());
			 return;
		 }

		 psource.executeTurn(turn);

		 int move = turn.getId();

		 MoveListEntry entry = psource.getMove(move);
		 if (entry == null) return;
		 MonsterMove theMove = entry.getMove();

		 if (psource.isImmobilised(theMove.getStatusException())) {
			 return;
		 }

		 Monster ptarget = m_monsters[target][m_active[target]];
		 if (theMove.isAttack() && ptarget.isFainted()) {
			 informUseMove(psource, entry.getName());
			 showMessage("But there was no target!");
			 return;
		 }
		 psource.useMove(move, ptarget);
	 }

	 /**
	  * Determine the order in which monster attack, etc.
	  */
	 @SuppressWarnings("unchecked")
	 private void sortBySpeed(Monster[] active) {
		 // Sort pokemon by speed.
		 ArrayList<Monster> list = new ArrayList<Monster>(Arrays.asList(active));
		 Collections.sort(list, new Comparator() {
			 public int compare(Object o1, Object o2) {
				 return MonsterWrapper.compareSpeed((Monster)o1, (Monster)o2);
			 }
		 });
	 }

	 /**
	  * Tick status effects at the end of a turn.
	  */
	 @SuppressWarnings("unchecked")
	 private void tickStatuses(Monster[] active) {
		 sortBySpeed(active);

		 for (int i = 0; i < active.length; ++i) {
			 active[i].beginStatusTicks();
		 }

		 // For each tier.
		 final int tiers = StatusEffect.getTierCount();
		 for (int i = 0; i < tiers; ++i) {
			 // For each monster.
			 for (int j = 0; j < active.length; ++j) {
				 Monster mon = active[j];
				 if (mon.isFainted()) continue;

				 List v = mon.getStatusesByTier(i);
				 Iterator k = v.iterator();
				 while (k.hasNext()) {
					 ((StatusEffect)k.next()).tick(mon);
				 }
			 }
		 }
	 }

	 /**
	  * Return the number of party members in a given party who are alive.
	  */
	 public int getAliveCount(int idx) {
		 if ((idx < 0) || (idx >= m_participants)) {
			 throw new IllegalArgumentException("0 <= idx < participants");
		 }
		 int alive = 0;
		 Monster[] mon = m_monsters[idx];
		 for (int i = 0; i < mon.length; ++i) {
			 if (mon[i] != null && !mon[i].isFainted()) {
				 alive++;
			 }
		 }
		 return alive;
	 }

	 /**
	  * Check if one party has won the battle and inform victory if so.
	  */
	 public void checkBattleEnd(int i) {
		 if (getAliveCount(i) != 0)
			 return;
		 int opponent = ((i == 0) ? 1 : 0);
		 if (getAliveCount(opponent) == 0) {
			 // It's a draw!
			 opponent = -1;
		 }
		 informVictory(opponent);
	 }

	 /**
	  * A wrapper for a monster and a turn. Can be compared on the basis of
	  * move priority, or, failing that, speed.
	  */
	 @SuppressWarnings("unchecked")
	 protected static class MonsterWrapper implements Comparable {
		 private Monster m_mon;
		 private BattleTurn m_turn;
		 private int m_idx;

		 /**
		  * Initialise a MonsterWrapper with a Monster and a BattleTurn.
		  */
		 private MonsterWrapper(Monster p, BattleTurn turn, int idx) {
			 m_mon = p;
			 m_turn = turn;
			 m_idx = idx;
		 }

		 /**
		  * Compare based on speed.
		  */
		 public static int compareSpeed(Monster p1, Monster p2) {
			 if(p1 == null)
				 return 1;
			 if(p2 == null)
				 return -1;
			 final int s1 = p1.getStat(Monster.S_SPEED);
			 final int s2 = p2.getStat(Monster.S_SPEED);
			 int comp = 0;
			 if (s1 > s2) comp = -1;
			 else if (s2 > s1) comp = 1;

			 // Note: shoddy.
			 if (comp != 0) {
				 if (p1.getField().getEffectByType(SpeedSwapEffect.class) != null) {
					 return -comp;
				 }
				 return comp;
			 }

			 // Since the speeds are equal, pick a random monster.
			 return (p1.getField().getRandom().nextBoolean() ? -1 : 1);
		 }

		 /**
		  * Compare this object to another MonsterWrapper.
		  */
		 public int compareTo(Object obj) {
			 MonsterWrapper comp = (MonsterWrapper)obj;
			 if ((comp == null) || (comp.m_turn == null))
				 return -1;
			 if (m_turn == null)
				 return 1;
			 if (m_turn.isMoveTurn() && comp.m_turn.isMoveTurn()) {
				 int p1 = 0, p2 = 0;
				 MonsterMove m1 = m_turn.getMove(m_mon);
				 if (m1 != null) {
					 p1 = m1.getPriority();
				 }
				 MonsterMove m2 = comp.m_turn.getMove(comp.m_mon);
				 if (m2 != null) {
					 p2 = m2.getPriority();
				 }
				 if (p1 > p2) return -1;
				 if (p2 > p1) return 1;
				 return compareSpeed(m_mon, comp.m_mon);
			 }
			 return (!m_turn.isMoveTurn() ? -1 : 1);
		 }

		 /**
		  * Sort monster and moves in descending order. Reorders the elements of
		  * the arrays passed in and also returns an array of the indices of the
		  * monster as rearranged.
		  */
		 public static int[] sortMoves(Monster[] active, BattleTurn[] move) {
			 final MonsterWrapper[] wrap = new MonsterWrapper[active.length];
			 for (int i = 0; i < wrap.length; ++i) {
				 wrap[i] = new MonsterWrapper(active[i], move[i], i);
			 }
			 Collections.sort(Arrays.asList(wrap));
			 final int[] order = new int[wrap.length];
			 for (int i = 0; i < wrap.length; ++i) {
				 MonsterWrapper item = wrap[i];
				 active[i] = item.m_mon;
				 move[i] = item.m_turn;
				 order[i] = item.m_idx;
			 }
			 return order;
		 }
	 }

	 /**
	  * Execute a turn.
	  */
	 public void executeTurn(BattleTurn[] move) {       
		 Monster[] active = getActiveMonster();
		 int[] order = MonsterWrapper.sortMoves(active, move);

		 for (int i = 0; i < active.length; ++i) {
			 BattleTurn turn = move[i];
			 if (turn == null)
				 continue;
			 if (turn.isMoveTurn()) {
				 MonsterMove monsterMove = turn.getMove(active[i]);
				 if (monsterMove != null) {
					 monsterMove.beginTurn(move, i, active[i]);
				 }
			 }
		 }

		 for (int i = 0; i < active.length; ++i) {
			 int other = (order[i] == 0) ? 1 : 0;
			 BattleTurn turn = move[i];
			 if (turn != null) {
				 executeTurn(turn, order[i], other);
			 }
		 }

		 // Refresh the active array in case a trainer switched.
		 active = getActiveMonster();

		 tickStatuses(active);

		 boolean request = true;
		 for (int i = 0; i < active.length; ++i) {
			 // Synchronise statuses.
			 active[i].synchroniseStatuses();

			 if (!active[i].isFainted()) {
				 continue;
			 }

			 requestMonsterReplacement(i);
			 request = false;
		 }

		 // Synchronise FieldEffects.
		 synchroniseFieldEffects();

		 //showMessage("---");

		 if (request) {
			 requestMoves();
		 }
	 }

	 public abstract void clearQueue();
}
