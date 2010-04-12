/*
 * ToxicEffect.java
 *
 * Created on January 13, 2007, 1:53 PM
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

package net.k3rnel.server.battle.mechanics.statuses;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.MonsterType;

/**
 *
 * @author Colin
 */
public class ToxicEffect extends PoisonEffect {
    
    private int m_turns = 1;
    
    /** Creates a new instance of ToxicEffect */
    public ToxicEffect() {
        m_lock = SPECIAL_EFFECT_LOCK;
    }
    
    /**
     * Poison stays through switching out.
     */
    public boolean switchOut(Monster p) {
        m_turns = 1;
        return false;
    }
    
    /**
     * Toxic removes 1/16, 2/16, 3/16, etc. max health each round
     * up to a maximum of 8/16.
     */
    public boolean tick(Monster p) {
        int damage;
        if (p.hasAbility("Poison Heal")) {
            damage = p.getStat(Monster.S_HP) / 8;
            p.getField().showMessage(p.getName() + "'s Poison Heal restored health!");
            p.changeHealth(damage);
        } else {
            damage = p.getStat(Monster.S_HP) * m_turns / 16;
            if (m_turns < 8) {
                ++m_turns;
            }
            if (damage <= 0) damage = 1;
            p.getField().showMessage(p.getName() + " is hurt by poison!");
            p.changeHealth(-damage, true);
        }
        return false;
    }
    
    /**
     * Toxic is in the fourth tier.
     */
    public int getTier() {
        return 3;
    }
    
    public boolean apply(Monster p) {
        if (p.hasAbility("Immunity")) {
            return false;
        }
        if (p.isType(MonsterType.T_POISON) || p.isType(MonsterType.T_STEEL)) {
            return false;
        }
        m_turns = 1;
        return true;
    }
    
    public void unapply(Monster p) {
    }
    
    public String getDescription() {
        return " was badly poisoned!";
    }
    
    /**
     * Poison does not immobilise.
     */
    public boolean immobilises(Monster poke) {
        return false;
    }
    
}
