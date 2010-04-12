/*
 * FreezeEffect.java
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
import net.k3rnel.server.battle.BattleTurn;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;
import net.k3rnel.server.battle.mechanics.statuses.field.SunEffect;

/**
 * TODO: Account for fire moves thawing out monster.
 * @author Colin
 */
public class FreezeEffect extends StatusEffect {

    public FreezeEffect() {
        m_lock = SPECIAL_EFFECT_LOCK;
    }
    
    public String getName() {
        return "Freeze";
    }
    
    public boolean tick(Monster p) {
        return false;
    }
    
    public int getTier() {
        // Not applicable.
        return -1;
    }
    
    /**
     * A frozen monster is not cured by being switched out.
     */
    public boolean switchOut(Monster p) {
        return false;
    }
    
    /**
     * Ice monster cannot be frozen.
     */
    public boolean apply(Monster p) {
        if (p.hasAbility("Magma Armor")) {
            return false;
        }
        if (p.isType(MonsterType.T_ICE))
            return false;
        if (p.getField().getEffectByType(SunEffect.class) != null)
            return false;
        return true;
    }
    
    public void unapply(Monster p) {
        p.getField().showMessage(p.getName() + " was defrosted!");
    }
    
    public String getDescription() {
        return " was frozen solid!";
    }
    
    public boolean isListener() {
        return true;
    }
    
    public void executeTurn(Monster p, BattleTurn turn) {
        String name = turn.getMove(p).getMoveListEntry().getName();
        if (name.equals("Flame Wheel") || name.equals("Sacred Fire")) {
            p.removeStatus(this);
        }
    }
    
    public void informDamaged(Monster source, Monster target, MoveListEntry entry, int damage) {
        MonsterMove move = entry.getMove();
        if (move != null) {
            if (move.getType().equals(MonsterType.T_FIRE)) {
                target.removeStatus(this);
            }
        }
    }
    
    /**
     * Freeze immolilises the monster.
     */
    public boolean immobilises(Monster poke) {
        BattleField field = poke.getField();
        if (field.getRandom().nextDouble() <= 0.25) {
            poke.removeStatus(this);
            return false;
        }
        field.showMessage(poke.getName() + " is frozen solid!");
        return true;
    }
    
}
