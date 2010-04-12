/*
 * ParalysisEffect.java
 *
 * Created on December 23, 2006, 12:11 PM
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

package net.k3rnel.server.battle.mechanics.statuses;
import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.Monster;

/**
 *
 * @author Colin
 */
public class ParalysisEffect extends StatusEffect {
    
    /** Creates a new instance of ConfuseEffect */
    public ParalysisEffect() {
        m_lock = SPECIAL_EFFECT_LOCK;
    }
    
    public String getName() {
        return "Paralysis";
    }
    
    /**
     * Paralysis stays through switching out.
     */
    public boolean switchOut(Monster p) {
        return false;
    }
    
    public boolean tick(Monster p) {
        return false;
    }
    
    public int getTier() {
        return 2;
    }
    
    public boolean apply(Monster p) {
        if (p.hasAbility("Limber")) {
            return false;
        }

        if (!p.hasAbility("Quick Feet")) {
            p.getMultiplier(Monster.S_SPEED).divideBy(4.0);
        }
        return true;
    }
    
    public void unapply(Monster p) {
        if (!p.hasAbility("Quick Feet")) {
            p.getMultiplier(Monster.S_SPEED).multiplyBy(4.0);
        }
    }
    
    public String getDescription() {
        return " is paralysed! It may be unable to move!";
    }
    
    /**
     * Paralysis has a 25% chance of immobolising the afflicted monster.
     */
    public boolean immobilises(Monster poke) {
        BattleField field = poke.getField();
        if (field.getRandom().nextDouble() <= 0.25) {
            field.showMessage(poke.getName() + " is paralysed! It can't move!");
            return true;
        }
        return false;
    }
    
}
