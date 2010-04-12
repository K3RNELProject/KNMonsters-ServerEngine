/*
 * MonsterMove.java
 *
 * Created on December 15, 2006, 3:42 PM
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

package net.k3rnel.server.battle.mechanics.moves;
import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.BattleTurn;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.BattleMechanics;
import net.k3rnel.server.battle.mechanics.MonsterType;

/**
 * This class represents a move that a pokemon can use on its turn.
 * @author Colin
 */
public class MonsterMove implements Cloneable {
    
    protected MonsterType m_type;
    protected int m_power;
    protected double m_accuracy;
    protected int m_pp;
    protected MoveListEntry m_entry;
    
    /**
     * Initialise a typical attacking move.
     */
    public MonsterMove(MonsterType type, int power, double accuracy, int pp) {
        m_type = type;
        m_power = power;
        m_accuracy = accuracy;
        m_pp = pp;
    }
    
    /**
     * Set this move's entry in the move list.
     */
    /*package*/ void setMoveListEntry(MoveListEntry e) {
        m_entry = e;
    }
    
    /**
     * Return this move's entry in the move list.
     */
    public MoveListEntry getMoveListEntry() {
        return m_entry;
    }
    
    /**
     * Return whether this move should use special attack and defence.
     */
    public boolean isSpecial(BattleMechanics mech) {
        return mech.isMoveSpecial(this);
    }
    
    /**
     * Clone this move.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            /* unreachable */
            return null;
        }
    }
    
    /**
     * Attempt to hit an enemy.
     */
    public boolean attemptHit(BattleMechanics mech, Monster user, Monster target) {
        return mech.attemptHit(this, user, target);
    }
    
    /**
     * Cause a monster to use this move on another monster.
     */
    public int use(BattleMechanics mech, Monster user, Monster target) {
        int damage = mech.calculateDamage(this, user, target);
        target.changeHealth(-damage);
        return damage;
    }
    
    /**
     * Get PP.
     */
    public int getPp() {
        return m_pp;
    }
    
    /**
     * Return whether this move deals damage.
     */
    public boolean isDamaging() {
        return isAttack();
    }
    
    /**
     * Get accuracy.
     */
    public double getAccuracy() {
        return m_accuracy;
    }
    
    /**
     * Set the accuracy of this move.
     */
    public void setAccuracy(double accuracy) {
        if (accuracy > 1.0) {
            m_accuracy = 1.0;
        } else if (accuracy < 0.0) {
            m_accuracy = 0.0;
        } else {
            m_accuracy = accuracy;
        }
    }
    
    /**
     * Get the type of this move.
     */
    public MonsterType getType() {
        return m_type;
    }
    
    /**
     * Some moves can be used even if a status effect (e.g. sleep) would
     * normally prevent it. If this move can be used a such, the class
     * of the status effect is returned by this method. Otherwise, the method
     * returns null.
     */
    @SuppressWarnings("unchecked")
	public Class getStatusException() {
        return null;
    }
    
    /**
     * Get the effectiveness of this move against a denfending monster.
     */
    public double getEffectiveness(Monster user, Monster defender) {
        return getEffectiveness(m_type, user, defender);
    }
    
    /**
     * Get the effectiveness of one type of move against an arbitrary monster.
     */
    public static double getEffectiveness(MonsterType type, Monster user, Monster defender) {
        MonsterType[] defTypes = defender.getTypes();
        double multiplier = 1.0;
        for (int i = 0; i < defTypes.length; ++i) {
            double expected = type.getMultiplier(defTypes[i]);
            double factor;
            MonsterType def = defTypes[i];
            if (user != null) {
                factor = user.getEffectiveness(type, def, false);
                if (factor == expected) {
                    factor = defender.getEffectiveness(type, def, true);
                }
            } else {
                BattleField field = defender.getField();
                factor = field.getEffectiveness(type, def, false);
                if (factor == expected) {
                    factor = field.getEffectiveness(type, def, true);
                }
            }
            multiplier *= factor;
        }
        return multiplier;
    }
    
    /**
     * This method is called when a monster who has this move is switched into
     * the field.
     */
    public void switchIn(Monster p) {
        
    }
    
    /**
     * Return whether this move can strike critical.
     */
    public boolean canCriticalHit() {
        return true;
    }
    
    /**
     * Set the type of this move.
     */
    public void setType(MonsterType type) {
        m_type = type;
    }
    
    /**
     * Get the power of this move.
     */
    public int getPower() {
        return m_power;
    }
    
    /**
     * Set the power of this move.
     */
    public void setPower(int power) {
        m_power = power;
    }
    
    /**
     * Get the priority of this move. Priority determines when this move will
     * be used during the turn.
     */
    public int getPriority() {
        return 0;
    }
    
    /**
     * Determine whether this move has a high chance of striking a critical
     * hit.
     */
    public boolean hasHighCriticalHitRate() {
        return false;
    }
    
    /**
     * Returns whether this move is an attack. This method is shoddy and should 
     * be overridden by any exceptions.
     */
    public boolean isAttack() {
        return m_power != 0;
    }
    
    /**
     * This function is called at the beginning on a turn on which this move
     * is about to be used.
     * @param turn the moves about to be used on this turn
     * @param index the position of the source in the turn array
     * @param source the monster who is using the move
     */
    public void beginTurn(BattleTurn[] turn, int index, Monster source) {
        
    }
    
    /**
     * Return whether this move is buggy.
     */
    public boolean isBuggy() {
        return false;
    }
}
