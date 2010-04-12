/*
 * StatusListener.java
 *
 * Created on February 14, 2007, 8:37 PM
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
 * along with this program; if not, visit the Free Software Foundation, Inc.
 * online at http://gnu.org.
 */

package net.k3rnel.server.battle.mechanics.statuses;
import net.k3rnel.server.battle.Monster;

/**
 *
 * @author Colin
 */
public interface StatusListener {
    
    /**
     * Called when a status effect is successfully applied to a pokemon.
     */
    public void informStatusApplied(Monster source, Monster poke, StatusEffect eff);
    
    /**
     * Called when a status effect is remove from a pokemon.
     */
    public void informStatusRemoved(Monster poke, StatusEffect eff);
    
}
