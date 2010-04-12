/*
 * ConfuseEffect.java
 *
 * Created on December 23, 2006, 12:12 PM
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
import net.k3rnel.server.battle.mechanics.BattleMechanics;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;

/**
 *
 * @author Colin
 */
public class ConfuseEffect extends StatusEffect {

    private int m_turns = 0;
    
    public String getName() {
        return "Confusion";
    }
    
    public boolean tick(Monster p) {
        return false;
    }
    
    public int getTier() {
        // Not applicable.
        return 1;
    }
    
    public boolean switchOut(Monster p) {
        return true;
    }
    
    public boolean apply(Monster p) {
        if (p.hasSubstitute()) {
            return false;
        }
        if (p.hasAbility("Own Tempo")) {
            return false;
        }
        if (p.hasAbility("Tangled Feet")) {
            p.getMultiplier(Monster.S_EVASION).increaseMultiplier();
        }
        m_turns = p.getField().getRandom().nextInt(4) + 2;
        return true;
    }
    
    public void unapply(Monster p) {
        if (p.hasAbility("Tangled Feet")) {
            p.getMultiplier(Monster.S_EVASION).decreaseMultiplier();
        }
    }
    
    public String getDescription() {
        return " became confused!";
    }
    
    /**
     * Confusion has a 50% chance of immobolising the afflicted monster.
     */
    public boolean immobilises(Monster mons) {
        if (mons.hasEffect(SleepEffect.class)) {
            return false;
        }
        
        if (--m_turns <= 0) {
            mons.removeStatus(this);
            mons.getField().showMessage(mons.getName()
                + " snapped out of confusion!");
            return false;
        }
        
        BattleField field = mons.getField();
        
        field.showMessage(mons.getName() + " is confused!");
        if (field.getRandom().nextDouble() <= 0.5) {
            return false;
        }
        
        field.showMessage("It hurt itself in its confusion!");
        mons.useMove(new MonsterMove(MonsterType.T_TYPELESS, 40, 1.0, 1) {
                public int use(BattleMechanics mech, Monster source, Monster target) {
                    int damage = mech.calculateDamage(this, source, target);
                    target.changeHealth(-damage, true);
                    return damage;
                }
                public boolean attemptHit(BattleMechanics mech,
                        Monster source, Monster target) {
                    return true;
                }
                public boolean canCriticalHit() {
                    return false;
                }
            }, mons);
        return true;
    }
    
    public void informDuplicateEffect(Monster p) {
        p.getField().showMessage(p.getName() + " is already confused!");
    }
}
