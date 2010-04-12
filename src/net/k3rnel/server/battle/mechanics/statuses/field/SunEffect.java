/*
 * SunEffect.java
 *
 * Created on January 7, 2007, 5:36 PM
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

package net.k3rnel.server.battle.mechanics.statuses.field;

import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.StatMultiplier;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;
import net.k3rnel.server.battle.mechanics.moves.StatusMove;
import net.k3rnel.server.battle.mechanics.statuses.ChargeEffect;
import net.k3rnel.server.battle.mechanics.statuses.PercentEffect;

/**
 * 1. Raises the power of Fire moves by 1.5x
 * 2. Cuts the power of Water moves by 1/2.
 * 3. Makes Solarbeam a one-turn move.
 * 4. Cuts the Accuracy of Thunder to 50%.
 * 5. Doubles the current effective Speed of monster with the Chlorophyll
 *    ability.
 * 6. Makes monster with the Forecast ability Fire-type.
 * 7. Makes Weather Ball a power 100 Fire-type move.
 * 8. Makes Moonlight and Morning Sun restore 3/4 of the user's max HP.
 * 9. Does 1/8 damage per turn to Pokemon with the Dry Skin ability.
 * 10. Does 1/8 damage per turn to Pokemon with the Solar Power ability.
 * 11. Raises the special attack of Pokemon with the Solar Power ability by 50%
 *
 * @author Colin
 */
public class SunEffect extends WeatherEffect {
    
    /** Creates a new instance of SunEffect */
    public SunEffect(int turns) {
        super(turns);
    }
    
    public String getName() {
        return "Sun";
    }
    
    public SunEffect() {
        super(5);
    }
    
    private void modifySpeed(Monster p, double factor) {
        if (!p.hasAbility("Chlorophyll")) {
            return;
        }
        p.getMultiplier(Monster.S_SPEED).multiplyBy(factor);
    }
    
    private void modifySpAttack(Monster p, double factor) {
        if (!p.hasAbility("Solar Power")) {
             return;
        }
        p.getMultiplier(Monster.S_SPATTACK).multiplyBy(factor);
    }
    
    private void flowerGift(Monster p, boolean increase) {
        if (!p.hasAbility("Flower Gift")) {
            return;
        }
        StatMultiplier multiplier = p.getMultiplier(Monster.S_ATTACK);
        if (increase) multiplier.increaseMultiplier();
        else multiplier.decreaseMultiplier();
        
        multiplier = p.getMultiplier(Monster.S_SPDEFENCE);
        if (increase) multiplier.increaseMultiplier();
        else multiplier.decreaseMultiplier();
    }
    
    public boolean apply(Monster p) {
        if (m_applied[p.getParty()] || !(m_applied[p.getParty()] = hasEffects(p.getField())))
            return true;
        // Double the current effective Speed of monster with the
        // Chlorophyll ability.
        modifySpeed(p, 2.0);
        modifySpAttack(p, 1.5);
        flowerGift(p, true);
        setTypes(p, new MonsterType[] { MonsterType.T_FIRE }, true);
        return true;
    }
    
    public void unapply(Monster p) {
        if (!m_applied[p.getParty()])
            return;
        m_applied[p.getParty()] = false;
        modifySpeed(p, 0.5);
        modifySpAttack(p, 2.0 / 3.0);
        flowerGift(p, false);
        setTypes(p, null, false);
    }
    
    public String getDescription() {
        return null;
    }
    
    public boolean immobilises(Monster p) {
        return false;
    }
    
    /**
     * Tick this effect for the whole field.
     */
    protected void tickWeather(BattleField field) {
        field.showMessage("The sun continues to shine.");
    }
    
    public boolean tickMonster(Monster p) {
        if (!hasEffects(p.getField()))
            return false;
        if (p.hasAbility("Dry Skin") || p.hasAbility("Solar Power")) {
            p.getField().showMessage(p.getName() + " was hurt by the sunlight!");
            p.changeHealth(-p.getStat(Monster.S_HP) / 8, true);
        }
        return false;
    }
    
    /**
     * Remove this effect from a field.
     */
    public void unapplyToField(BattleField field) {
        field.showMessage("The sun faded.");
    }
    
    /**
     * Apply this effect to a field.
     */
    public boolean applyToField(BattleField field) {
        field.showMessage("The sun began to shine!");
        return true;
    }
    
    /**
     * 1. Raises the power of Fire moves by 1.5x
     * 2. Cuts the power of Water moves by 1/2.
     * 3. Makes Solarbeam a one-turn move.
     * 4. Cuts the Accuracy of Thunder to 50%.
     * 6. Makes Weather Ball a power 100 Fire-type move.
     * 7. Makes Moonlight and Morning Sun restore 3/4 of the user's max HP.
     */
    public MoveListEntry getTransformedMove(Monster poke, MoveListEntry entry) {
        if (!hasEffects(poke.getField()))
            return entry;
        
        MonsterMove move = entry.getMove();
        MonsterType type = move.getType();
        String name = entry.getName();
        if (type.equals(MonsterType.T_FIRE)) {
            move.setPower((int)((double)move.getPower() * 1.5));
        } else if (type.equals(MonsterType.T_WATER)) {
            move.setPower((int)((double)move.getPower() / 2.0));
        } else if (name.equals("Solarbeam")) {
            if (move instanceof StatusMove) {
                StatusMove statusMove = (StatusMove)move;
                // Assume that the first effect is the ChargeEffect!
                ChargeEffect charge = (ChargeEffect)statusMove.getEffects()[0];
                charge.setTurns(0);
            }
        } else if (name.equals("Thunder")) {
            move.setAccuracy(0.5);
        } else if (name.equals("Weather Ball")) {
            move.setPower(100);
            move.setType(MonsterType.T_FIRE);
        } else if (name.equals("Moonlight") || name.equals("Morning Sun") || name.equals("Synthesis")) {
            StatusMove statusMove = (StatusMove)move;
            // Assume that the first effect is the PercentEffect!
            PercentEffect perc = (PercentEffect)statusMove.getEffects()[0];
            perc.setPercent(2.0/3.0);
        }
        
        return entry;
    }
    
}
