/*
 * RainEffect.java
 *
 * Created on May 6, 7:49 PM
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
import net.k3rnel.server.battle.mechanics.BattleMechanics;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.moves.MoveList;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;
import net.k3rnel.server.battle.mechanics.moves.StatusMove;
import net.k3rnel.server.battle.mechanics.statuses.ChargeEffect;
import net.k3rnel.server.battle.mechanics.statuses.ParalysisEffect;
import net.k3rnel.server.battle.mechanics.statuses.PercentEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatusEffect;

/**
 * 1. Raises the power of Water moves by 1.5x
 * 2. Cuts the power of Fire moves by 1/2.
 * 3. Cuts the power of Solarbeam to 60.
 * 4. Makes Thunder have perfect accuracy.  (Perfect, not 100%)
 * 5. Doubles the current effective Speed of monster with the Swift Swim
 * ability.
 * 6. Restores 1/16 HP to monster with the Rain Dish ability.
 * 7. Makes monster with the Forecast ability Water-type.
 * 8. Makes Weather Ball a power 100 Water-type move.
 * 9. Makes Moonlight and Morning Sun restore 1/4 of the user's max HP.
 * 10. Heals Pokemon with Dry Skin 1/8 health each turn
 *
 * @author Ben
 */
public class RainEffect extends WeatherEffect {
    
    /** Creates a new instance of SunEffect */
    public RainEffect(int turns) {
        super(turns);
    }
    
    public String getName() {
        return "Rain";
    }
    
    public RainEffect() {
        super(5);
    }
    
    private void modifySpeed(Monster p, double factor) {
        if (!p.hasAbility("Swift Swim")) {
            return;
        }
        p.getMultiplier(Monster.S_SPEED).multiplyBy(factor);
    }
    
    public boolean apply(Monster p) {
        if (m_applied[p.getParty()] || !(m_applied[p.getParty()] = hasEffects(p.getField())))
            return true;
        // Double the current effective Speed of monster with the
        // Swift Swim ability.
        modifySpeed(p, 2.0);
        setTypes(p, new MonsterType[] { MonsterType.T_WATER }, true);
        return true;
    }
    
    public void unapply(Monster p) {
        if (!m_applied[p.getParty()])
            return;
        m_applied[p.getParty()] = false;
        modifySpeed(p, 0.5);
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
        field.showMessage("The rain continues to fall.");
    }
    
    public boolean tickMonster(Monster p) {
        if (!hasEffects(p.getField()))
            return false;
        if (p.hasAbility("Dry Skin")) {
            p.getField().showMessage(p.getName() + " soaked up rain!");
            p.changeHealth(p.getStat(Monster.S_HP) / 8);
        }
        if (p.hasAbility("Hydration")) {
            if (p.hasEffect(StatusEffect.SPECIAL_EFFECT_LOCK)) {
                p.getField().showMessage(p.getName() + "'s status was cured!");
                p.removeStatus(StatusEffect.SPECIAL_EFFECT_LOCK);
            }
        }            
        return false;
    }
    
    /**
     * Apply this effect to a field.
     */
    public boolean applyToField(BattleField field) {
        field.showMessage("Rain began to fall!");
        return true;
    }
    
    /**
     * Remove this effect from a field.
     */
    public void unapplyToField(BattleField field) {
        field.showMessage("The rain stopped.");
    }
    
    /**
     * 1. Raises the power of Water moves by 1.5x
     * 2. Cuts the power of Fire moves by 1/2.
     * 3. Cuts the power of Solarbeam to 60.
     * 4. Makes Thunder have perfect accuracy.  (Perfect, not 100%)
     * 5. Doubles the current effective Speed of monster with the Swift Swim
     * ability.
     * 6. Restores 1/16 HP to monster with the Rain Dish ability.
     * 7. Makes monster with the Forecast ability Water-type.
     * 8. Makes Weather Ball a power 100 Water-type move.
     * 9. Makes Moonlight and Morning Sun restore 1/4 of the user's max HP.
     */
    public MoveListEntry getTransformedMove(Monster poke, MoveListEntry entry) {
        if (!hasEffects(poke.getField()))
            return entry;
        
        MonsterMove move = entry.getMove();
        MonsterType type = move.getType();
        String name = entry.getName();
        if (type.equals(MonsterType.T_WATER)) {
            move.setPower((int)((double)move.getPower() * 1.5));
        } else if (type.equals(MonsterType.T_FIRE)) {
            move.setPower((int)((double)move.getPower() / 2.0));
        } else if (name.equals("Weather Ball")) {
            move.setPower(100);
            move.setType(MonsterType.T_WATER);
        } else if (name.equals("Moonlight") || name.equals("Morning Sun") || name.equals("Synthesis")) {
            StatusMove statusMove = (StatusMove)move;
            // Assume that the first effect is the PercentEffect!
            PercentEffect perc = (PercentEffect)statusMove.getEffects()[0];
            perc.setPercent(1.0/3.0);
        } else if (name.equals("Solar Beam")) {
            if (move instanceof StatusMove) {
                StatusMove statusMove = (StatusMove)move;
                ChargeEffect charge = (ChargeEffect)statusMove.getEffects()[0];
                charge.setTurns(2);
            }
        } else if (name.equals("Thunder")) {
            return new MoveListEntry("Thunder", new StatusMove(
                MonsterType.T_ELECTRIC, 120, 0.7, 10, new StatusEffect[] {
                    new ParalysisEffect()
                    },
                new boolean[] { false },
                new double[] { 0.3 }
                ) {
                    public boolean attemptHit(BattleMechanics mech, Monster user, Monster target) {
                        return MoveList.PerfectAccuracyMove.isHit(mech, user, target);
                    }
                });
                
        }
        
        return entry;
    }
    
}
