/*
 * FieldEffect.java
 *
 * Created on January 6, 2007, 5:17 PM
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
 */

package net.k3rnel.server.battle.mechanics.statuses.field;
import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.statuses.StatusEffect;

/**
 * This class represents a StatusEffect that is applied to a whole battle
 * field, rather than a single monster.
 * @author Colin
 */
public abstract class FieldEffect extends StatusEffect {
    
    /**
     * Whether tick() has been called since the last call to beginTick().
     */
    private boolean m_ticked = true;
    
    /**
     * Generally a FieldEffect is said to be healed when a monster is switched
     * out because it will be reapplied when the monster returns. This is the
     * case with all current monster field effects (i.e. weather) in the sense
     * that a monster who switches out during a weather effect and returns when
     * the weather has cleared up is not still affected by that weather. If
     * this function returned false, a monster switching in in clear times who
     * had left when weather was present would still be affected by the weather.
     */
    public boolean switchOut(Monster poke) {
        return true;
    }
    
    /**
     * Returns this object, rather than making a clone. This is important so
     * that all monster getting a clone of a FieldEffect are actually getting
     * the same effect.
     */
    public final Object clone() {
        return this;
    }
    
    /**
     * Allow for functional cloning when applying this effect to a field.
     */
    public FieldEffect getFieldCopy() {
        return (FieldEffect)super.clone();
    }
    
    /**
     * Tick the effect on its associated field.
     * Subclasses should override tickPokemon() or tickField() rather than
     * this method.
     */
    public final boolean tick(Monster poke) {
        if (!m_ticked) {
            m_ticked = true;
            if (tickField(poke.getField())) {
                return true;
            }
        }
        return tickMonster(poke);
    }
    
    /**
     * Tick the effect on a monster.
     */
    protected boolean tickMonster(Monster poke) {
        return false;
    }
    
    /**
     * Tick the effect on the whole field.
     * @return whether the effect was removed
     */
    protected abstract boolean tickField(BattleField field);
    
    /**
     * Apply this effect to a field.
     */
    public abstract boolean applyToField(BattleField field);
    
    /**
     * Remove this effect from a field.
     */
    public void unapplyToField(BattleField field) {
        
    }
    
    /**
     * Prepare this effect for ticking.
     */
    public void beginTick() {
        m_ticked = false;
    }
}
