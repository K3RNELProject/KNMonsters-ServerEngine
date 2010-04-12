/*
 * IntrinsicAbility.java
 *
 * Created on January 6, 2007, 5:47 PM
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.MonsterSpecies;
import net.k3rnel.server.battle.mechanics.JewelMechanics;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.moves.MoveList;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;
import net.k3rnel.server.battle.mechanics.moves.RecoilMove;
import net.k3rnel.server.battle.mechanics.statuses.BurnEffect;
import net.k3rnel.server.battle.mechanics.statuses.ConfuseEffect;
import net.k3rnel.server.battle.mechanics.statuses.FreezeEffect;
import net.k3rnel.server.battle.mechanics.statuses.ParalysisEffect;
import net.k3rnel.server.battle.mechanics.statuses.PoisonEffect;
import net.k3rnel.server.battle.mechanics.statuses.SleepEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatChangeEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatusEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatusListener;
import net.k3rnel.server.battle.mechanics.statuses.field.FieldEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.HailEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.RainEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.SandstormEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.SunEffect;
import net.k3rnel.server.battle.mechanics.statuses.field.WeatherEffect;
import net.k3rnel.server.battle.mechanics.statuses.items.HoldItem;

/**
 * Implementation of Guts: boosts attack whilst a status effect is present.
 */
class StatusReactionAbility extends IntrinsicAbility implements StatusListener {
    private int m_stat;
    public StatusReactionAbility(String name, int stat) {
        super(name);
        m_stat = stat;
    }
    public void informStatusApplied(Monster source, Monster poke, StatusEffect eff) {
        if (eff.getLock() == StatusEffect.SPECIAL_EFFECT_LOCK) {
            poke.getMultiplier(m_stat).increaseMultiplier();
        }
    }
    public void informStatusRemoved(Monster poke, StatusEffect eff) {
        if (eff.getLock() == StatusEffect.SPECIAL_EFFECT_LOCK) {
            poke.getMultiplier(m_stat).decreaseMultiplier();
        }
    }
}

/**
 * Implementation of Synchronize: applies status to enemy that is
 * inflicted on user.
 */
class SynchronizeAbility extends IntrinsicAbility implements StatusListener {
    public SynchronizeAbility() {
        super("Synchronize");
    }
    public void informStatusApplied(Monster source, Monster poke, StatusEffect eff) {
        if (eff.getLock() == StatusEffect.SPECIAL_EFFECT_LOCK) {
            if ((eff instanceof SleepEffect) || (eff instanceof FreezeEffect))
                return;
            // Synchronize requires that the opponent applied the effect.
            if ((source != poke) && (source != null)) {
                source.addStatus(poke, eff);
            }
        }
        
    }
    public void informStatusRemoved(Monster poke, StatusEffect eff) {

    }
}

/**
 *
 * @author Colin
 */
public class IntrinsicAbility extends StatusEffect implements Comparable<Object> {
    
    protected static HashMap<String, IntrinsicAbility> m_map = new HashMap<String, IntrinsicAbility>();
    private String m_name = null;
    
    /**
     * List of abilities nullified by Mold Breaker.
     */
    private static final HashSet<String> m_moldBreaker = new HashSet<String>(Arrays.asList(new String[] {
        "Battle Armor",
        "Clear Body",
        "Damp",
        "Dry Skin",
        "Filter",
        "Flash Fire",
        "Flower Gift",
        "Heatproof",
        "Hyper Cutter",
        "Immunity",
        "Inner Focus",
        "Insomnia",
        "Keen Eye",
        "Leaf Guard",
        "Levitate",
        "Lightningrod",
        "Limber",
        "Magma Armor",
        "Marvel Scale",
        "Motor Drive",
        "Oblivious",
        "Own Tempo",
        "Sand Veil",
        "Shell Armor",
        "Shield Dust",
        "Simple",
        "Snow Cloak",
        "Solid Rock",
        "Soundproof",
        "Sticky Hold",
        "Storm Drain",
        "Suction Cups",
        "Tangled Feet",
        "Thick Fat",
        "Unaware",
        "Vital Spirit",
        "Volt Absorb",
        "Water Absorb",
        "Water Veil",
        "White Smoke",
        "Wonder Guard"
    }));
    
    /**
     * List of moves that count as physical ("contact") attacks.
     */
    private static final HashSet<String> m_physical = new HashSet<String>(Arrays.asList(new String[] {
        "Aerial Ace",
        "Ancientpower",
        "Arm Thrust",
        "Astonish",
        "Bide",
        "Bind",
        "Bite",
        "Blaze Kick",
        "Body Slam",
        "Bounce",
        "Brick Break",
        "Clamp",
        "Comet Punch",
        "Constrict",
        "Counter",
        "Crabhammer",
        "Cross Chop",
        "Crunch",
        "Crush Claw",
        "Cut",
        "Dig",
        "Dive",
        "Dizzy Punch",
        "Double Kick",
        "Double-Edge",
        "Doubleslap",
        "Dragon Claw",
        "Drill Peck",
        "Dynamicpunch",
        "Endeavor",
        "Extremespeed",
        "Facade",
        "False Swipe",
        "Fire Punch",
        "Flail",
        "Flame Wheel",
        "Fly",
        "Focus Punch",
        "Frustration",
        "Fury Attack",
        "Fury Cutter",
        "Fury Swipes",
        "Guillotine",
        "Headbutt",
        "Hi Jump Kick",
        "Horn Attack",
        "Horn Drill",
        "Hyper Fang",
        "Ice Ball",
        "Ice Punch",
        "Iron Tail",
        "Jump Kick",
        "Karate Chop",
        "Knock Off",
        "Leaf Blade",
        "Leech Life",
        "Lick",
        "Low Kick",
        "Mach Punch",
        "Mega Kick",
        "Mega Punch",
        "Megahorn",
        "Metal Claw",
        "Meteor Mash",
        "Needle Arm",
        "Outrage",
        "Overheat",
        "Peck",
        "Petal Dance",
        "Poison Fang",
        "Poison Tail",
        "Pound",
        "Pursuit",
        "Quick Attack",
        "Rage",
        "Rapid Spin",
        "Return",
        "Revenge",
        "Reversal",
        "Rock Smash",
        "Rolling Kick",
        "Rollout",
        "Scratch",
        "Seismic Toss",
        "Shadow Punch",
        "Skull Bash",
        "Sky Uppercut",
        "Slam",
        "Slash",
        "Smellingsalt",
        "Spark",
        "Steel Wing",
        "Stomp",
        "Strength",
        "Struggle",
        "Submission",
        "Super Fang",
        "Superpower",
        "Tackle",
        "Take Down",
        "Thief",
        "Thrash",
        "Thunderpunch",
        "Tickle",
        "Triple Kick",
        "Vice Grip",
        "Vine Whip",
        "Vital Throw",
        "Waterfall",
        "Wing Attack",
        "Wrap"
    }));
    
    /**
     * Return whether a move is a physical attack.
     */
    public static boolean isPhysical(MoveListEntry entry) {
        return m_physical.contains(entry.getName());
    }
    
    public static Set<String> getAbilityNames() {
    	Set<String> result = new HashSet<String>();
    	Iterator<String> it = m_physical.iterator();
    	while(it.hasNext()) {
    		result.add((String) it.next());
    	}
    	it = m_moldBreaker.iterator();
    	while(it.hasNext()) {
    		result.add((String) it.next());
    	}
    	it = m_map.keySet().iterator();
    	while(it.hasNext()) {
    		result.add((String) it.next());
    	}
    	return result;
    }
    /**
     * Initialise the HaspMap of abilities.
     */
    static {
        new IntrinsicAbility("Battle Armor");
        new IntrinsicAbility("Chlorophyll");
        new IntrinsicAbility("Rock Head");
        new IntrinsicAbility("Clear Body");
        new IntrinsicAbility("White Smoke");
        
        new IntrinsicAbility("Color Change") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                MonsterType type = move.getMove().getType();
                if (!target.isType(type)) {
                    target.setType(new MonsterType[] { type });
                    target.getField().showMessage(target.getName()
                        + " became the "
                        + type
                        + " type!");
                }
            }
            public void switchIn(Monster p) {
                p.setType(new MonsterType[] { MonsterType.T_NORMAL });
            }
        };
        
        new CriticalTypeAbility("Blaze", MonsterType.T_FIRE);
        new CriticalTypeAbility("Overgrow", MonsterType.T_GRASS);
        new CriticalTypeAbility("Swarm", MonsterType.T_BUG);
        new CriticalTypeAbility("Torrent", MonsterType.T_WATER);
        new CompoundEyesAbility();
        new IntrinsicAbility("Damp");
        
        new IntrinsicAbility("Drought") {
               public void switchIn(Monster p) {
                   p.getField().showMessage(p.getName() + "'s Drought intensified the sun's rays!");
                   p.getField().applyEffect(new SunEffect(0));
               }
            };
            
        new IntrinsicAbility("Drizzle") {
               public void switchIn(Monster p) {
                   p.getField().showMessage(p.getName() + "'s Drizzle caused a storm!");
                   p.getField().applyEffect(new RainEffect(0));
                }
            };
            
        new IntrinsicAbility("Sand Stream") {
               public void switchIn(Monster p) {
                   BattleField field = p.getField();
                   field.showMessage(p.getName() + "'s Sand Stream whipped up a sandstorm!"); 
                   field.applyEffect(new SandstormEffect(0));
                }
            };

        new IntrinsicAbility("Early Bird");
        
        new IntrinsicAbility("Effect Spore") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                if (!isPhysical(move)) {
                    return;
                }
                
                StatusEffect eff = null;
                String description = "";
                String name = source.getName();
                BattleField field = source.getField();
                final double random = field.getRandom().nextDouble();
                if (random <= 1.0) {
                    eff = new ParalysisEffect();
                    description = "paralysed " + name;
                } else if (random <= 2.0) {
                    eff = new PoisonEffect();
                    description = "poisoned " + name;
                } else if (random <= 3.0) {
                    eff = new SleepEffect();
                    description = "put " + name + " to sleep";
                }
                if (eff != null) {
                    field.setNarrationEnabled(false);
                    boolean applied = (source.addStatus(target, eff) != null);
                    field.setNarrationEnabled(true);
                    if (applied) {
                        source.getField().showMessage(target.getName()
                            + "'s Effect Spore " + description + "!");
                    }
                }
            }
        };
        
        new IntrinsicAbility("Flame Body") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                if (!isPhysical(move)) return;
                if (source.getField().getRandom().nextDouble() <= 0.3) {
                    if (source.addStatus(target, new BurnEffect()) != null) {
                        source.getField().showMessage(target.getName() + "'s Flame Body burned "
                            + source.getName() + "!");
                    }
                }
            }
        };
        
        new IntrinsicAbility("Poison Point") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                if (!isPhysical(move)) return;
                if (source.getField().getRandom().nextDouble() <= 0.3) {
                    if (source.addStatus(target, new PoisonEffect()) != null) {
                        source.getField().showMessage(target.getName() + "'s Poison Point poisoned "
                            + source.getName() + "!");
                    }
                }
            }
        };
        
        new IntrinsicAbility("Static") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                if (!isPhysical(move)) return;
                if (source.getField().getRandom().nextDouble() <= 0.3) {
                    if (source.addStatus(target, new ParalysisEffect()) != null) {
                        source.getField().showMessage(target.getName() + "'s Static paralysed "
                            + source.getName() + "!");
                    }
                }
            }
        };
        
        new IntrinsicAbility("Cute Charm") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                if (!isPhysical(move)) return;
                if (source.getField().getRandom().nextDouble() <= 0.3) {
                    if ((source.addStatus(target, new MoveList.AttractEffect()) != null)) {
                        source.getField().showMessage(target.getName() + "'s Cute Charm infatuated "
                            + source.getName() + "!");
                    }
                }
            }
        };
        
        new IntrinsicAbility("Liquid Ooze");
        
        IntrinsicAbility power = new IntrinsicAbility("Huge Power") {
                public boolean apply(Monster p) {
                    p.getMultiplier(Monster.S_ATTACK).multiplyBy(2.0);
                    return true;
                }
                public void unapply(Monster p) {
                    p.getMultiplier(Monster.S_ATTACK).divideBy(2.0);
                }
                /**
                 * According to AA, you were totally wrong about these not
                 * working with Skill Swap and Role Play, Ben.
                 */
        };
        
        power = (IntrinsicAbility)power.clone();
        power.m_name = "Pure Power";
        power.registerAbility();
        
        new IntrinsicAbility("Hustle") {
               public boolean apply(Monster p) {
                    p.getMultiplier(Monster.S_ATTACK).multiplyBy(1.5);
                    p.getAccuracy().multiplyBy(0.8);
                    return true;
                }
               public void unapply(Monster p) {
                    p.getMultiplier(Monster.S_ATTACK).divideBy(1.5);
                    p.getAccuracy().divideBy(0.8);
               }
        };
        
        new IntrinsicAbility("Hyper Cutter");
        new IntrinsicAbility("Illuminate");
        new IntrinsicAbility("Immunity");
        new IntrinsicAbility("Inner Focus");
        new IntrinsicAbility("Insomnia");
        new IntrinsicAbility("Forecast");
        
        new IntrinsicAbility("Intimidate") {
            public void switchIn(Monster a) {
                Monster b = a.getOpponent();
                if (b.hasAbility("Clear Body") || b.hasAbility("White Smoke") || 
                        b.hasAbility("Hyper Cutter") || b.hasSubstitute()) {
                    return;
                }
                a.getField().showMessage(a.getName()
                    + "'s intimidate cut "
                    + b.getName()
                    + "'s attack!");
                StatChangeEffect effect =
                        new StatChangeEffect(Monster.S_ATTACK, false, 1);
                effect.setDescription(null);
                b.addStatus(a, effect);
            }
        };
        
        new IntrinsicAbility("Keen Eye");
        new IntrinsicAbility("Limber");
        new IntrinsicAbility("Magma Armor");
        new IntrinsicAbility("Own Tempo");
        new IntrinsicAbility("Pickup");
        new IntrinsicAbility("Serene Grace");
        new IntrinsicAbility("Shell Armor");
        new IntrinsicAbility("Shield Dust");
        new IntrinsicAbility("Stench");
        new IntrinsicAbility("Sticky Hold");
        new IntrinsicAbility("Water Veil");
        
        new IntrinsicAbility("Truant") {
            private boolean m_inactive = true;
            public boolean immobilises(Monster p) {
                if (m_inactive == !m_inactive) {
                    p.getField().showMessage(p.getName() + " is loafing around!");
                    return true;
                }
                return false;
            }
            public boolean switchOut(Monster p) {
                m_inactive = true;
                return super.switchOut(p);
            }
        };
        
        new IntrinsicAbility("Natural Cure") {
            public boolean switchOut(Monster p) {
                p.removeStatus(StatusEffect.SPECIAL_EFFECT_LOCK);
                return super.switchOut(p);
            }
        };
        
        new IntrinsicAbility("Pressure") {
            public void switchIn(Monster p) {
                if (p.getField().getMechanics() instanceof JewelMechanics) {
                    p.getField().showMessage(p.getName() + " is exerting its pressure!");
                }
            }
        };
        new IntrinsicAbility("Run Away");
        
        new IntrinsicAbility("Shed Skin") {
            public boolean tick(Monster p) {
                if ((p.getField().getRandom().nextDouble() * 3.0) <= 1.0) {
                    if (p.hasEffect(StatusEffect.SPECIAL_EFFECT_LOCK)) {
                        p.removeStatus(StatusEffect.SPECIAL_EFFECT_LOCK);
                        p.getField().showMessage(p.getName() + " shed its skin!");
                    }
                }
                return false;
            }
            public int getTier() {
                // TODO: Get the correct tier!
                return 0;
            }
        };

        new IntrinsicAbility("Sturdy");
        new IntrinsicAbility("Vital Spirit");
        new IntrinsicAbility("Lightningrod"); // Has no effect in 1 v. 1.
        new IntrinsicAbility("Plus"); // Has no effect in 1 v. 1.
        new IntrinsicAbility("Minus"); // Has no effect in 1 v. 1.
        
        new IntrinsicAbility("Rough Skin") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                if (!isPhysical(move)) return;
                int part = damage / 16;
                if (part < 1) part = 1;
                source.changeHealth(-part, true);
            }
        };
        
        new IntrinsicAbility("Levitate") {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getType().equals(MonsterType.T_GROUND) && move.isAttack()) {
                    BattleField field = p.getField();
                    field.showMessage(p.getName() + " makes ground moves miss with Levitate!");
                    move.setAccuracy(0);
                    return entry;
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Thick Fat") {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getType().equals(MonsterType.T_FIRE) || move.getType().equals(MonsterType.T_ICE)) {
                    move.setPower(move.getPower() / 2);
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Trace") {
            public void switchIn(Monster p) {
                Monster target = p.getOpponent();
                IntrinsicAbility ability = target.getAbility();
                if (ability == null) {
                    return;
                }
                if (ability.getName().equals("Multitype")) {
                    // Multitype cannot be traced.
                    return;
                }
                p.setAbility(ability, true);
                BattleField field = p.getField();
                field.showMessage(p.getName() + " traced " + target.getName()
                        + "'s " + ability.getName() + "!");
                if (field.getMechanics() instanceof JewelMechanics) {
                    IntrinsicAbility ablility = p.getAbility();
                    if (!ablility.equals(this)) {
                        ablility.switchIn(p);
                    }
                }
            }
        };
        
        new IntrinsicAbility("Speed Boost") {
            public int getTier() {
                return 5;
            }
            
            public boolean tick(Monster p) {
                StatChangeEffect eff = new StatChangeEffect(Monster.S_SPEED, true);
                eff.setDescription("'s Speed Boost raised its speed!");
                p.addStatus(p, eff);
                return true;
            }
        };
        
        new IntrinsicAbility("Sand Veil");    
        new IntrinsicAbility("Swift Swim");
        
        new IntrinsicAbility("Rain Dish") {
            public int getTier() {
                return 2;
            }
            
            public boolean tick(Monster p) {
                if (!p.hasEffect(RainEffect.class)) {
                   return false;
                }
                int maximum = p.getStat(Monster.S_HP);
                int raise = maximum / 16;
                p.getField().showMessage(p.getName() + "'s Rain Dish restored its health a little!");
                p.changeHealth(raise);
                return true;
            }
        };

        new OpponentEffectAbility("Mold Breaker") {
            public void applyToOpponent(Monster owner, Monster p) {
                IntrinsicAbility ability = p.getAbility();
                if ((ability != null) && ability.isActive() &&
                        m_moldBreaker.contains(ability.getName())) {
                    // Nullify this ability.
                    ability.unapply(p);
                    ability.deactivate();
                }
            }
            public boolean switchOut(Monster p) {
                /* This will intefere with Gastro Acid, but whatever; the
                 * interaction is unlikely to come up. */
                Monster opponent = p.getOpponent();
                IntrinsicAbility ability = opponent.getAbility();
                if ((ability != null) && !ability.isActive()
                        && !ability.isRemovable()) {
                    ability.apply(opponent);
                    ability.activate();
                }
                return super.switchOut(p);
            }
        };
        
        new IntrinsicAbility("Wonder Guard") {
            private Monster m_monster;
            public boolean apply(Monster p) {
                m_monster = p;
                return true;
            }
            public boolean isEffectivenessTransformer(boolean enemy) {
                return enemy;
            }
            public double getEnemyTransformedEffectiveness(
                    MonsterType move, MonsterType target) {
                double actual = MonsterMove.getEffectiveness(move, null, m_monster);
                if (actual <= 1.0)
                    return 0.0;
                return actual;
            }
            public boolean isSwappable() {
                return false;
            }
        };
        
        new AbsorbAbility("Volt Absorb", MonsterType.T_ELECTRIC);
        new AbsorbAbility("Water Absorb", MonsterType.T_WATER);
        
        new IntrinsicAbility("Flash Fire") {
            private boolean m_boosted = false;
            public boolean isMoveTransformer(boolean enemy) {
                return true;
            }
            public boolean switchOut(Monster p) {
                m_boosted = false;
                return super.switchOut(p);
            }
            protected MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (m_boosted && move.getType().equals(MonsterType.T_FIRE)) {
                    move.setPower(move.getPower() * 3 / 2);
                }
                return entry;
            }
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (!move.getType().equals(MonsterType.T_FIRE)) return entry;
                BattleField field = p.getField();
                if (!move.isAttack() || !(field.getMechanics() instanceof JewelMechanics)) return entry;
                field.informUseMove(p.getOpponent(), entry.getName());
                field.showMessage(p.getName() + "'s Flash Fire raised its fire power!");
                m_boosted = true;
                return null;
            }
        };
                    
        
        new TrappingAbility("Arena Trap") {
            public boolean isTrappable(Monster p) {
                return (MonsterMove.getEffectiveness(MonsterType.T_GROUND,
                            p.getOpponent(), p) != 0.0)
                        && !p.hasAbility("Levitate");
            }
        };
        
        new TrappingAbility("Magnet Pull") {
            public boolean isTrappable(Monster p) {
                return p.isType(MonsterType.T_STEEL);
            }
        };
        
        new TrappingAbility("Shadow Tag");
        
        new IntrinsicAbility("Oblivious");
        
        //DP abilities
        
        new IntrinsicAbility("Anticipation") {
            public void switchIn(Monster p) {
                Monster target = p.getOpponent();
                for (int i = 0; i < 4; i++) {
                    MoveListEntry entry = target.getMove(i);
                    if (entry == null) continue;
                    MonsterMove move = entry.getMove();
                    if (move.getEffectiveness(p.getOpponent(), p) > 1.0 && move.isAttack()) {
                        p.getField().showMessage(p.getName() + "'s Anticipation made it shudder!");
                        return;
                    }
                }
            }
        };
        
        new IntrinsicAbility("Forewarn") {
            public void switchIn(Monster p) {
                Monster target = p.getOpponent();
                MoveListEntry mostPowerful = null;
                int power = 0;
                for (int i = 0; i < 4; i++) {
                    MoveListEntry move = target.getMove(i);
                    if (move == null) continue;
                    int movePower = move.getMove().getPower();
                    if (movePower > power) {
                        power = movePower;
                        mostPowerful = move;
                    }
                }
                if (mostPowerful != null) {
                    p.getField().showMessage(p.getName() + " foresaw " + mostPowerful.getName() + "!");
                }
            }
        };
        
        new IntrinsicAbility("Aftermath") {
            public boolean isListener() {
                return true;
            }
            public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                if (!isPhysical(move)) return;
                if (target.isFainted()) {
                    source.getField().showMessage(source.getName() + " was hurt by " + target.getName() + "'s Aftermath!");
                    int hp = source.getStat(Monster.S_HP) / 4;
                    if (hp < 1) hp = 1;
                    source.changeHealth(-hp, true);
                }
            }
        };
        
        new IntrinsicAbility("Bad Dreams") {
            public int getTier() {
                //todo: find the right teir
                return 3;
            }            
            public boolean tick(Monster p) {
                Monster target = p.getOpponent();
                if (target.hasEffect(SleepEffect.class)) {
                    int damage = target.getStat(Monster.S_HP) / 8;
                    if (damage < 1) damage = 1;
                    target.changeHealth(-damage, true);
                }
                return false;
            }
        };
        
        new IntrinsicAbility("Suction Cups");
        
        new IntrinsicAbility("Download") {
            public void switchIn(Monster p) {
                Monster target = p.getOpponent();
                int stat = (target.getStat(Monster.S_DEFENCE) < target.getStat(Monster.S_SPDEFENCE)) ?
                    Monster.S_ATTACK : Monster.S_SPATTACK;
                p.getField().showMessage(p.getName() + "'s Download raised its stats!");
                p.addStatus(p, new StatChangeEffect(stat, true));
            }
        };
        
        new AbsorbAbility("Dry Skin", MonsterType.T_WATER) {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                if (super.getEnemyTransformedMove(p, entry) == null)
                    return null;
                MonsterMove move = entry.getMove();
                if (move.getType().equals(MonsterType.T_FIRE)) {
                    move.setPower(move.getPower() * 5 / 4);
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Filter") {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getEffectiveness(p, p.getOpponent()) > 1.0) {
                    move.setPower(move.getPower() * 3 / 4);
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Solid Rock") {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getEffectiveness(p, p.getOpponent()) > 1.0) {
                    move.setPower(move.getPower() * 3 / 4);
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Frisk") {
            public void switchIn(Monster p) {
                Monster target = p.getOpponent();
                HoldItem item = target.getItem();
                if (item != null) {
                    p.getField().showMessage(p.getName() + " found "
                        + target.getName() + "'s " + item.getName() + "!");
                }
            }
        };
        
        new IntrinsicAbility("Gluttony");
        //Doesn't do anything in battle
        new IntrinsicAbility("Honey Gather");
        //Doesn't do anything in 1 v 1
        new IntrinsicAbility("Storm Drain");
        new IntrinsicAbility("Hyrdation");
        new IntrinsicAbility("Ice Body");
        new IntrinsicAbility("Skill Link");
        new IntrinsicAbility("Poison Heal");
        new IntrinsicAbility("Sniper");
        new IntrinsicAbility("Snow Cloak");
        new IntrinsicAbility("Solar Power");
        new IntrinsicAbility("Tangled Feet");
        new IntrinsicAbility("Steadfast");
        new IntrinsicAbility("Hydration");
        
        new IntrinsicAbility("Heatproof") {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getType().equals(MonsterType.T_FIRE)) {
                    move.setPower(move.getPower() / 2);
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Magic Guard");
        
        new IntrinsicAbility("Iron Fist") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }
            
            protected MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                String[] moves = { "Comet Punch", "Mach Punch", "Bullet Punch", "Thunderpunch", "Ice Punch",
                    "Fire Punch", "Sky Uppercut", "Mega Punch", "Focus Punch", "Drain Punch" };
                String name = entry.getName();
                MonsterMove move = entry.getMove();
                for (int i = 0; i < moves.length; i++) {
                    if (name.equals(moves[i])) {
                        move.setPower(move.getPower() * 6 / 5);
                    }
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Motor Drive") {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            
            protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getType().equals(MonsterType.T_ELECTRIC) && move.isAttack()) {
                    p.getField().informUseMove(p.getOpponent(), entry.getName());
                    p.addStatus(p, new StatusEffect() {
                        public String getName() {
                            return "Motor Drive";
                        }
                        public String getDescription() {
                            return "'s Motor Drive increased its speed!";
                        }
                        public int getTier() {
                            return -1;
                        }
                        public boolean apply(Monster p) {
                            p.getMultiplier(Monster.S_SPEED).multiplyBy(1.5);
                            return true;
                        }
                        public boolean switchOut(Monster p) {
                            p.getMultiplier(Monster.S_SPEED).divideBy(1.5);
                            return true;
                        }
                        public boolean tick(Monster p) {
                            return false;
                        }
                        public boolean isPassable() {
                            return false;
                        }
                    });
                    return null;
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Normalize") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }
            
            protected MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                entry.getMove().setType(MonsterType.T_NORMAL);
                return entry;
            }
        };
        
        new IntrinsicAbility("Rivalry") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }
            
            protected MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                @SuppressWarnings("unused")
				Monster target = p.getOpponent();
                int g1 = p.getGender();
                int g2 = p.getOpponent().getGender();
                if ((g1 == MonsterSpecies.GENDER_NONE) ||
                    (g2 == MonsterSpecies.GENDER_NONE))
                        return entry;
                int numerator = 5;
                if (g1 != g2)
                    numerator = 3;
                entry.getMove().setPower(entry.getMove().getPower() * numerator / 4);
                return entry;
            }
        };
        
        new IntrinsicAbility("Snow Warning") {
               public void switchIn(Monster p) {
                   p.getField().showMessage(p.getName() + "'s Snow Warning started a blizzard!");
                   p.getField().applyEffect(new HailEffect(0));
               }
            };
        
        new IntrinsicAbility("Slow Start") {
            private int m_turns = 0;
            public void switchIn(Monster p) {
                if (m_turns == 0) {
                    p.getMultiplier(Monster.S_ATTACK).multiplyBy(0.5);
                    p.getMultiplier(Monster.S_SPEED).multiplyBy(0.5);
                }
                m_turns = 5;
                p.getField().showMessage(p.getName() + " can't get going due to its Slow Start!");
            }
            public int getTier() {
                return 5;
            }
            public String getName() {
                return "Slow Start";
            }
            public boolean tick(Monster p) {
                if (--m_turns == 0) {
                    p.getMultiplier(Monster.S_ATTACK).multiplyBy(2.0);
                    p.getMultiplier(Monster.S_SPEED).multiplyBy(2.0);
                    p.getField().showMessage(p.getName() + " finally got its act together!");
                }
                return false;
            }
        };
        
        new IntrinsicAbility("Scrappy") {
            public boolean isEffectivenessTransformer(boolean enemy) {
                return !enemy;
            }
            public double getTransformedEffectiveness(MonsterType move, MonsterType defender) {
                if (defender.equals(MonsterType.T_GHOST)) {
                    if (move.equals(MonsterType.T_NORMAL) ||
                            move.equals(MonsterType.T_FIGHTING))
                        return 1.0;
                }
                return super.getTransformedEffectiveness(move, defender);
            }
        };
        
        new IntrinsicAbility("Technician") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }
            
            protected MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getPower() <= 60) {
                    entry.getMove().setPower(move.getPower() * 3 / 2);
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Tinted Lens") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }
            
            protected MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getEffectiveness(p, p.getOpponent()) < 1.0) {
                    move.setPower(move.getPower() * 2);
                }
                return entry;
            }
        };
        
        new IntrinsicAbility("Klutz") {
            public boolean apply(Monster p) {
                HoldItem item = p.getItem();
                if ((item != null) && item.isActive()) {
                    item.unapply(p);
                    item.deactivate();
                }
                return true;
            }
            public void unapply(Monster p) {
                HoldItem item = p.getItem();
                if ((item != null) && !item.isRemovable()) {
                    item.activate();
                    item.apply(p);
                }
            }
        };
        
        new IntrinsicAbility("Reckless") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }
            public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move instanceof RecoilMove) {
                    move.setPower((int)((double)move.getPower() * 1.2));
                }
                return entry;
            }
        };
        
        new StatusReactionAbility("Guts", Monster.S_ATTACK);
        new StatusReactionAbility("Quick Feet", Monster.S_SPEED);
        new StatusReactionAbility("Marvel Scale", Monster.S_DEFENCE);
        
        new IntrinsicAbility("Multitype") {
            public boolean isSwappable() {
                return false;
            }
        };
        new IntrinsicAbility("Super Luck");
        new IntrinsicAbility("Adaptability");
        new IntrinsicAbility("No Guard");
        new SynchronizeAbility();
        new IntrinsicAbility("Flower Gift");
        new IntrinsicAbility("Anger Point");
        new AirLockAbility("Air Lock");
        new AirLockAbility("Cloud Nine");
        new UnburdenAbility();
        
        new IntrinsicAbility("Soundproof") {
            public boolean isMoveTransformer(boolean enemy) {
                return enemy;
            }
            public MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
                Set<String> set = new HashSet<String>(Arrays.asList(new String[] {
                    "Grasswhistle", "Growl", "Hyper Voice", "Metal Sound", "Roar",
                    "Sing", "Supersonic", "Screech", "Snore", "Uproar"
                }));
                String name = entry.getName();
                if (set.contains(name)) {
                    BattleField field = p.getField();
                    field.informUseMove(p, name);
                    field.showMessage("But it failed!");
                    return null;
                }
                return entry;
            }
        };
        
        m_map.put("Cacophony", getInstance("Soundproof"));
        
        new IntrinsicAbility("Leaf Guard") {
            public boolean allowsStatus(StatusEffect eff, Monster source, Monster target) {
            	if(source == null || target == null)
            		return true;
                BattleField field = source.getField();
                if (field == null) return true;
                if (field.getEffectByType(SunEffect.class) ==  null) return true;
                if (source == target) return true;
                return !((eff instanceof BurnEffect) || (eff instanceof FreezeEffect) || 
                        (eff instanceof ParalysisEffect) || (eff instanceof PoisonEffect) ||
                        (eff instanceof SleepEffect) || (eff instanceof ConfuseEffect));
            }
        };
    }
    
    private static class UnburdenAbility extends IntrinsicAbility implements StatusListener {
        public UnburdenAbility() {
            super("Unburden");
        }
        public void informStatusApplied(Monster source, Monster poke, StatusEffect eff) {
        }
        public void informStatusRemoved(Monster p, StatusEffect eff) {
            if (eff instanceof HoldItem) {
                p.addStatus(p, new StatusEffect() {
                        public String getName() {
                            return "Unburden";
                        }
                        public String getDescription() {
                            return " lost its burden!";
                        }
                        public boolean apply(Monster p) {
                            p.getMultiplier(Monster.S_SPEED).multiplyBy(2.0);
                            return super.apply(p);
                        }
                        public void unapply(Monster p) {
                            p.getMultiplier(Monster.S_SPEED).divideBy(2.0);
                        }
                        public boolean isPassable() {
                            return false;
                        }
                    });
                }
            }
        }
    
    private static class AirLockAbility extends IntrinsicAbility {
        public AirLockAbility(String name) {
            super(name);
        }
        public void switchIn(Monster p) {
            BattleField field = p.getField();
            FieldEffect eff = field.getEffectByType(WeatherEffect.class);
            if (eff != null) {
                eff.unapply(p);
                eff.unapply(p.getOpponent());
            }
        }
        public boolean switchOut(Monster p) {
            p.setAbility(null, true);
            BattleField field = p.getField();
            FieldEffect eff = field.getEffectByType(WeatherEffect.class);
            if (eff != null) {
                eff.apply(p);
                eff.apply(p.getOpponent());
            }
            return super.switchOut(p);
        }
    }
    
    /**
     * Absorb 25% damage of one type.
     */
    private static class AbsorbAbility extends IntrinsicAbility {
        private MonsterType m_type;
        
        public AbsorbAbility(String name, MonsterType type) {
            super(name);
            m_type = type;
        }
        
        public boolean isMoveTransformer(boolean enemy) {
            return enemy;
        }

        protected MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
            MonsterMove move = entry.getMove();
            if (move.getType().equals(m_type) && move.isAttack()) {
                p.getField().informUseMove(p.getOpponent(), entry.getName());
                int max = p.getStat(Monster.S_HP);
                if (p.getHealth() < max) {
                    p.getField().showMessage(p.getName() + "'s " + getName() + " absorbed damage!");
                    p.changeHealth(max / 4);
                }
                return null;
            }
            return entry;
        }

    }
    
    /***
     * Registration constructor.
     */
    protected IntrinsicAbility(String name) {
        this(true, name);
    }
    
    /**
     * Prevent external creation.
     */
    protected IntrinsicAbility(boolean register, String name) {
        m_name = name;
        if (register && (name != null)) {
            registerAbility();
        }
    }
    
    /**
     * Register an intrinsic ability.
     */
    protected void registerAbility() {
        m_map.put(m_name, this);
    }
    
    /**
     * Get this ability's name.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Get an intrinsic ability by name.
     */
    public static IntrinsicAbility getInstance(String ability) {
    	Iterator<String> it = m_map.keySet().iterator();
    	while(it.hasNext()) {
    		String a = it.next();
    		if(a.equalsIgnoreCase(ability)) {
    			return (IntrinsicAbility)m_map.get(a);
    		}
    	}
        return null;
    }
    
    /**
     * Intrinsic abilities generally do not tick.
     */
    public boolean tick(Monster p) {
        return false;
    }
    
    /**
     * Because they do not tick, intrinsic abilities do not need a tier.
     */
    public int getTier() {
        return -1;
    }
    
    /**
     * Intrinsic abilities do not employ descriptions.
     */
    public String getDescription() {
        return null;
    }
    
    /**
     * Intrinsic abilities generally do not immobilise.
     */
    public boolean immobilises(Monster p) {
        return false;
    }
    
    public boolean apply(Monster p) {
        return true;
    }
    
    public void unapply(Monster p) {
        
    }
    
    public boolean switchOut(Monster p) {
        return false;
    }
    
    /**
     * Return whether this ability works with role play and skill swap.
     */
    public boolean isSwappable() {
        return true;
    }
    
    /**
     * Returns whether an opponent claims the effect of this intrinsic
     * ability after taking it by way of a move (etc.).
     */
    public boolean isEffectTransferrable() {
        return true;
    }
    
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        IntrinsicAbility ability = (IntrinsicAbility)obj;
        return ability.m_name.equals(m_name);
    }
    
    /**
     * Compare this object to another IntrinsicAbility.
     */
    public int compareTo(Object obj) {
        IntrinsicAbility ability = (IntrinsicAbility)obj;
        return m_name.compareTo(ability.m_name);
    }
}
