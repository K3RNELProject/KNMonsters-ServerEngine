/*
 * FieldMove.java
 *
 * Created on January 13, 2007, 12:35 PM
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

package net.k3rnel.server.battle.mechanics.moves;
import java.lang.reflect.Constructor;

import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.BattleMechanics;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.statuses.field.WeatherEffect;

/**
 * A FieldMove is a move that a monster can use that applies a set of field
 * effects to the field (not to a particular monster).
 *
 * @author Colin
 */
public class WeatherMove extends MonsterMove {
    
    private Class<?>[] m_effects;
    private String m_item;
    
    /** Creates a new instance of FieldMove */
    public WeatherMove(MonsterType type, int pp, Class<?>[] effects, String item) {
        super(type, 0, 1.0, pp);
        m_effects = effects;
        m_item = item;
    }
    
    public Object clone() {
        WeatherMove ret = (WeatherMove)super.clone();
        ret.m_effects = (Class[])ret.m_effects.clone();
        return ret;
    }
    
    public boolean attemptHit(BattleMechanics mech, Monster user, Monster target) {
        return true;
    }
    
    public int use(BattleMechanics mech, Monster user, Monster target) {
        BattleField field = user.getField();
        for (int i = 0; i < m_effects.length; ++i) {
            int length = user.hasItem(m_item) ? 8 : 5;
            WeatherEffect eff = null;
            try {
                Constructor<?> ctor = m_effects[i].getConstructor(new Class[] { int.class });
                eff = (WeatherEffect)ctor.newInstance(new Object[] { new Integer(length) });
            } catch (Exception e) {
                throw new InternalError();
            }
            field.applyEffect(eff);
        }
        return 0;
    }
    
}
