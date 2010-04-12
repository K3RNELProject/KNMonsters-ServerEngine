/*
 * MonsterType.java
 *
 * Created on December 15, 2006, 1:50 PM
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

package net.k3rnel.server.battle.mechanics;
import java.io.Serializable;
import java.util.ArrayList;

import org.simpleframework.xml.Element;

/**
 * This class represents the type of a pokemon or of a move.
 * @author Colin
 */
public class MonsterType implements Serializable {
    
    private static final long serialVersionUID = 328662720352042529L;
    
    @Element
    private int m_type;
    private static ArrayList<MonsterType> m_typeList = new ArrayList<MonsterType>();

    /**
     * Constants representing each of the types.
     */
    public static final MonsterType T_NORMAL = new MonsterType(0);
    public static final MonsterType T_FIRE = new MonsterType(1);
    public static final MonsterType T_WATER = new MonsterType(2);
    public static final MonsterType T_ELECTRIC = new MonsterType(3);
    public static final MonsterType T_GRASS = new MonsterType(4);
    public static final MonsterType T_ICE = new MonsterType(5);
    public static final MonsterType T_FIGHTING = new MonsterType(6);
    public static final MonsterType T_POISON = new MonsterType(7);
    public static final MonsterType T_GROUND = new MonsterType(8);
    public static final MonsterType T_FLYING = new MonsterType(9);
    public static final MonsterType T_PSYCHIC = new MonsterType(10);
    public static final MonsterType T_BUG = new MonsterType(11);
    public static final MonsterType T_ROCK = new MonsterType(12);
    public static final MonsterType T_GHOST = new MonsterType(13);
    public static final MonsterType T_DRAGON = new MonsterType(14);
    public static final MonsterType T_DARK = new MonsterType(15);
    public static final MonsterType T_STEEL = new MonsterType(16);
    public static final MonsterType T_TYPELESS = new MonsterType(17);
    
    private static final String m_types[] = new String[] {
        "Normal",
        "Fire",
        "Water",
        "Electric",
        "Grass",
        "Ice",
        "Fighting",
        "Poison",
        "Ground",
        "Flying",
        "Psychic",
        "Bug",
        "Rock",
        "Ghost",
        "Dragon",
        "Dark",
        "Steel",
        "Typeless"
    };
    
    private static final boolean m_special[] = new boolean[] {
        false,
        true,
        true,
        true,
        true,
        true,
        false,
        false,
        false,
        false,
        true,
        false,
        false,
        false,
        true,
        true,
        false,
        false
    };
    
    private static final double m_multiplier[][] = new double[][] {
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5, 0, 1, 1, 0.5, 1 },
	{ 1, 0.5, 0.5, 1, 2, 2, 1, 1, 1, 1, 1, 2, 0.5, 1, 0.5, 1, 2, 1 },
	{ 1, 2, 0.5, 1, 0.5, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0.5, 1, 1, 1 },
	{ 1, 1, 2, 0.5, 0.5, 1, 1, 1, 0, 2, 1, 1, 1, 1, 0.5, 1, 1, 1 },
	{ 1, 0.5, 2, 1, 0.5, 1, 1, 0.5, 2, 0.5, 1, 0.5, 2, 1, 0.5, 1, 0.5, 1 },
	{ 1, 0.5, 0.5, 1, 2, 0.5, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 0.5, 1 },
	{ 2, 1, 1, 1, 1, 2, 1, 0.5, 1, 0.5, 0.5, 0.5, 2, 0, 1, 2, 2, 1 },
	{ 1, 1, 1, 1, 2, 1, 1, 0.5, 0.5, 1, 1, 1, 0.5, 0.5, 1, 1, 0, 1 },
	{ 1, 2, 1, 2, 0.5, 1, 1, 2, 1, 0, 1, 0.5, 2, 1, 1, 1, 2, 1 },
	{ 1, 1, 1, 0.5, 2, 1, 2, 1, 1, 1, 1, 2, 0.5, 1, 1, 1, 0.5, 1 },
	{ 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 0.5, 1, 1, 1, 1, 0, 0.5, 1 },
	{ 1, 0.5, 1, 1, 2, 1, 0.5, 0.5, 1, 0.5, 2, 1, 1, 0.5, 1, 2, 0.5, 1 },
	{ 1, 2, 1, 1, 1, 2, 0.5, 1, 0.5, 2, 1, 2, 1, 1, 1, 1, 0.5, 1 },
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0.5, 0.5, 1 },
	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0.5, 1 },
	{ 1, 1, 1, 1, 1, 1, 0.5, 1, 1, 1, 2, 1, 1, 2, 1, 0.5, 0.5, 1 },
	{ 1, 0.5, 0.5, 0.5, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0.5, 1 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
    };
    
    /**
     * Return the list of types.
     */
    public static MonsterType[] getTypes() {
        return (MonsterType[])m_typeList.toArray(new MonsterType[m_typeList.size()]);
    }
    
    /** Constructor used for serialization */
    public MonsterType() {}
    
    /**
     * Creates a new instance of MonsterType.
     * Note: this must be invoked in sequential order!
     */
    private MonsterType(int i) {
        m_type = i;
        m_typeList.add(i, this);
    }
    
    /**
     * Get a MonsterType object by its id.
     */
    public static MonsterType getType(int i) {
        return (MonsterType)m_typeList.get(i);
    }
    
    /**
     * Initialise the type by name.
     */
    public static MonsterType getType(String type) {
        for (int i = 0; i < m_types.length; ++i) {
            if (type.equalsIgnoreCase(m_types[i])) {
                return getType(i);
            }
        }
        return null;
    }
    
    /**
     * Return whether this type deals special damage.
     */
    public boolean isSpecial() {
        return m_special[m_type];
    }
    
    /**
     * Get the multiplier when attacking a pokemon of a given type.
     *
     * @param type the type of the defending pokemon
     */
    public double getMultiplier(MonsterType type) {
        return m_multiplier[m_type][type.m_type];
    }
    
    /**
     * Return whether this type is equal to the test type.
     */
    public boolean equals(Object type) {
        if (type == null) {
            return false;
        }
        try {
            MonsterType poketype = (MonsterType)type;
            return (poketype.m_type == m_type);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    /**
     * Get a textual representation of this type.
     */
    public String toString() {
        return m_types[m_type];
    }
    
    /**
     * Returns the Monster's type's id
     * @return
     */
    public int getType() {
    	return m_type;
    }
}
