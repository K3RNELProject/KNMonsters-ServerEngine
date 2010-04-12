/*
 * HiddenPowerMove.java
 *
 * Created on August 12, 2007, 1:10 AM
 *
 * This file is a part of Shoddy Battle.
 * Copyright (C) 2007  Colin Fitzpatrick
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
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * The Free Software Foundation may be visited online at http://www.fsf.org.
 */

package net.k3rnel.server.battle.mechanics.moves;
import net.k3rnel.server.battle.BattleTurn;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.MonsterType;

/**
 *
 * @author Colin
 */
public class HiddenPowerMove extends MonsterMove {

    /** Creates a new instance of HiddenPowerMove */
    public HiddenPowerMove() {
        super(MonsterType.T_NORMAL, 0, 1.0, 15);
    }

    public boolean isAttack() {
        return true;
    }
    public void beginTurn(BattleTurn[] turns, int index, Monster source) {
        switchIn(source);
    }
    public void switchIn(Monster source) {
        int power = 0;
        int type = 0;
        for (int i = 0; i < 6; ++i) {
            final int iv = source.getIv(i);
            final int increment = 1 << i;
            if (iv % 2 != 0) {
                type += increment;
            }
            if ((iv % 4 == 2) || (iv % 4 == 3)) {
                power += increment;
            }
        }
        power = (int)((double)power * 40.0 / 63.0 + 30.0);
        setPower(power);
        type = (int)((double)type * (15.0 / 63.0));

        MonsterType moveType = new MonsterType[] {
                MonsterType.T_FIGHTING,
                MonsterType.T_FLYING,
                MonsterType.T_POISON,
                MonsterType.T_GROUND,
                MonsterType.T_ROCK,
                MonsterType.T_BUG,
                MonsterType.T_GHOST,
                MonsterType.T_STEEL,
                MonsterType.T_FIRE,
                MonsterType.T_WATER,
                MonsterType.T_GRASS,
                MonsterType.T_ELECTRIC,
                MonsterType.T_PSYCHIC,
                MonsterType.T_ICE,
                MonsterType.T_DRAGON,
                MonsterType.T_DARK
            }[type];

        setType(moveType);
    }
    
}
