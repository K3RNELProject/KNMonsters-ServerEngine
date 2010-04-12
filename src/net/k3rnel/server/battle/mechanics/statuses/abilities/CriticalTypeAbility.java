/*
 * CriticalTypeAbility.java
 *
 * Created on May 6, 2007, 5:35 PM
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

package net.k3rnel.server.battle.mechanics.statuses.abilities;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;

/**
 *
 * @author Colin
 */
public class CriticalTypeAbility extends IntrinsicAbility {
    private MonsterType m_type;
    
    public CriticalTypeAbility(String name, MonsterType type) {
        super(name);
        m_type = type;
    }
    
    public boolean isMoveTransformer(boolean enemy) {
        return !enemy;
    }
    
    /**
     * If a monsters has 1/3 or less health then CriticalType transforms moves
     * of a certain type by giving them one and a half times the power.
     */
    public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
        double maximum = p.getStat(Monster.S_HP);
        double current = p.getHealth();
        if ((current * 3.0) > maximum) {
            return entry;
        }
        MonsterMove move = entry.getMove();
        if (move.getType().equals(m_type)) {
            move.setPower((int)((double)move.getPower() * 1.5));
        }
        return entry;
    }
}
