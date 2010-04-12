/*
 * PoisonEffect.java
 *
 * Created on December 23, 2006, 12:17 PM
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
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.MonsterType;

/**
 *
 * @author Colin
 */
public class PoisonEffect extends StatusEffect {
    
    /** Creates a new instance of PoisonEffect */
    public PoisonEffect() {
        m_lock = SPECIAL_EFFECT_LOCK;
    }
    
    public String getName() {
        return "Poison";
    }
    
    /**
     * Poison stays through switching out.
     */
    public boolean switchOut(Monster p) {
        return false;
    }
    
    /**
     * Poison removes 1/8 max health each round.
     */
    public boolean tick(Monster p) {
        int damage;
        if (p.hasAbility("Poison Heal")) {
            damage = p.getStat(Monster.S_HP) / 8;
            p.getField().showMessage(p.getName() + "'s Poison Heal restored health!");
            p.changeHealth(damage, true);
        } else {
            damage = p.getStat(Monster.S_HP) / 8;
            if (damage <= 0) damage = 1;
            p.getField().showMessage(p.getName() + " is hurt by poison!");
            p.changeHealth(-damage, true);
        }
        return false;
    }
    
    /**
     * Poison is in the fourth tier.
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
        return true;
    }
    
    public void unapply(Monster p) {
    }
    
    public String getDescription() {
        return " was poisoned!";
    }
    
    /**
     * Poison does not immobilise.
     */
    public boolean immobilises(Monster poke) {
        return false;
    }
    
}
