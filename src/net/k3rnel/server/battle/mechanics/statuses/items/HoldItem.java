/*
 * HoldItem.java
 *
 * Created on February 14, 2007, 9:16 PM
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

package net.k3rnel.server.battle.mechanics.statuses.items;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.k3rnel.server.battle.BattleField;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.JewelMechanics;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.moves.MoveList;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;
import net.k3rnel.server.battle.mechanics.statuses.BurnEffect;
import net.k3rnel.server.battle.mechanics.statuses.ConfuseEffect;
import net.k3rnel.server.battle.mechanics.statuses.FreezeEffect;
import net.k3rnel.server.battle.mechanics.statuses.ParalysisEffect;
import net.k3rnel.server.battle.mechanics.statuses.PoisonEffect;
import net.k3rnel.server.battle.mechanics.statuses.SleepEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatChangeEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatusEffect;
import net.k3rnel.server.battle.mechanics.statuses.StatusListener;
import net.k3rnel.server.battle.mechanics.statuses.ToxicEffect;
import net.k3rnel.server.battle.mechanics.statuses.abilities.IntrinsicAbility;
import net.k3rnel.server.battle.mechanics.statuses.field.FieldEffect;

/**
 * A hold item that cures a status effect.
 */
class StatusCureItem extends HoldItem implements StatusListener, Berry {
    private Class<?> m_effect;
    
    public StatusCureItem(String name, Class<?> eff) {
        super(name);
        m_effect = eff;
    }
    
    public boolean isCurable(StatusEffect eff) {
        return m_effect.isAssignableFrom(eff.getClass());
    }
    
    public void informStatusApplied(Monster source, Monster p, StatusEffect eff) {
        if (isCurable(eff)) {
            cureEffect(p, eff);
        }
    }
    
    public void cureEffect(Monster p, StatusEffect eff) {
        p.removeStatus(eff);
        p.removeStatus(this);
        displayCureMessage(p, eff);
    }
    
    public void displayCureMessage(Monster p, StatusEffect eff) {
        p.getField().showMessage(p.getName() + "'s " + getName() + " cured its " 
                + eff.getName() + "!");
    }
    
    public void executeEffects(Monster p) {
        List<StatusEffect> statuses = p.getNormalStatuses(StatusEffect.SPECIAL_EFFECT_LOCK);
        Iterator<StatusEffect> i = statuses.iterator();
        while (i.hasNext()) {
            StatusEffect effect = (StatusEffect)i.next();
            if (isCurable(effect)) {
                cureEffect(p, effect);
            }    
        }
    }
    
    public void informStatusRemoved(Monster p, StatusEffect eff) {
        
    }
}

class WhiteHerbItem extends StatusCureItem {
    public WhiteHerbItem() {
        super("White Herb", StatChangeEffect.class);
    }
    public void displayCureMessage(Monster p, StatusEffect eff) {
        p.getField().showMessage(p.getName() + "'s White Herb cured its " 
                + "status!");
    }
    public void executeEffects(Monster p) {
        List<StatusEffect> statuses = p.getNormalStatuses(0);
        Iterator<StatusEffect> i = statuses.iterator();
        while (i.hasNext()) {
            StatusEffect effect = (StatusEffect)i.next();
            if (isCurable(effect)) {
                cureEffect(p, effect);
            }    
        }
    }
}

/**
 * A hold item that restores health so long as the holder does not have a
 * particular kind of nature; if he does then the item also confuses.
 */
class HealthBoostItem extends HoldItem implements Berry {
    private int m_stat;
    
    public HealthBoostItem(String name, int stat) {
        super(name);
        m_stat = stat;
    }
    
    public int getTier() {
        return 3;
    }
    
    public boolean tick(Monster p) {
        int hp = p.getHealth();
        int max = p.getStat(Monster.S_HP);
        if ((hp * 2) <= max) {
            executeEffects(p);
            p.removeStatus(this);
            return true;
        }
        return false;
    }
    
    public void executeEffects(Monster p) {
        p.getField().showMessage(
            p.getName() + "'s " + getName() + " restored its health a little!");
        
        // Restore a little health.
        p.changeHealth(p.getStat(Monster.S_HP) / 8);
        
        // Check if the monster hates the berry.
        if (p.getNature().getEffect(m_stat) < 1.0) {
            // Confuse the monster if it hated it.
            p.getField().showMessage("The berry was the wrong flavour for " + p.getName() + "!");
            p.addStatus(p, new ConfuseEffect());
        }
    }
}

/**
 * A hold item that boosts a stat once health falls below 25%.
 */
class StatBoostItem extends HoldItem implements Berry {
    private int m_stat;
    
    public StatBoostItem(String name, int stat) {
        super(name);
        m_stat = stat;
    }
    
    public int getTier() {
        return 3;
    }
    
    public int getStat(Monster p) {
        return m_stat;
    }
    
    public int getStages() {
        return 1;
    }
    
    public boolean tick(Monster p) {
        int hp = p.getHealth();
        int max = p.getStat(Monster.S_HP);
        if (((hp * 4) <= max) || (p.hasAbility("Gluttony") && (hp * 2) <= max)) {
            // Boost the stat.
            executeEffects(p);
            p.removeStatus(this);
            return true;
        }
        return false;
    }
    
    public void executeEffects(Monster p) {
        final int stat = getStat(p);
        final int stages = getStages();
        StatChangeEffect eff = new StatChangeEffect(stat, true, stages);
        eff.setDescription("'s "
                + getName()
                + ((stages > 1) ? " sharply " : "") + " raised its "
                + Monster.getStatName(stat) + "!");
        p.addStatus(p, eff);
    }
}

/**
 * A berry that restores a fixed amount of health.
 */
class ConstantHealthBoostItem extends HoldItem implements Berry {
    private int m_change;
    public ConstantHealthBoostItem(String name, int change) {
        super(name);
        m_change = change;
    }
    public void executeEffects(Monster p) {
        p.getField().showMessage(p.getName() + "'s" + getName() + " restored health!");
        p.changeHealth(m_change);
    }
    public boolean isListener() {
        return true;
    }
    public void informDamaged(Monster source, Monster target, MoveListEntry entry, int damage) {
        if (target.getHealth() <= target.getStat(Monster.S_HP) / 2) {
            executeEffects(target);
            target.removeStatus(this);
        }
    }
}

/**
 * An Arceus plate: boosts the type of one move, and also changes Arceus to
 * said type.
 * @author Colin
 */
class ArceusPlate extends HoldItem {
    private MonsterType[] m_oldType;
    private MonsterType m_type;
    private double m_factor = 1.1;
    
    public ArceusPlate(String name, MonsterType type) {
        super(name);
        m_type = type;
    }
    
    public void switchIn(Monster p) {
        BattleField field = p.getField();
        if (p.hasAbility("Multitype") && (field != null)) {
            field.showMessage("The foe's " + p.getName()
                    + " transformed into the " + m_type + " type!");
        }
    }
    
    public boolean apply(Monster p) {
        if (p.hasAbility("Multitype")) {
            m_oldType = p.getTypes();
            p.setType(new MonsterType[] { m_type });
        }
        return true;
    }
    
    public void unapply(Monster p) {
        if (p.hasAbility("Multitype")) {
            p.setType(m_oldType);
        }
    }
    
    public boolean isMoveTransformer(boolean enemy) {
        return !enemy;
    }
    
    public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
        MonsterMove move = entry.getMove();
        if (move.getType().equals(m_type)) {
            move.setPower((int)((double)move.getPower() * m_factor));
        }
        if (entry.getName().equals("Judgment")) {
            move.setType(m_type);
        }
        return entry;
    }
    
}

/**
 * A hold item that makes one type of move more powerful.
 * @author Colin
 */
class TypeBoostItem extends HoldItem {
    
    protected MonsterType m_type;
    protected double m_factor = 1.1;
    
    /** Creates a new instance of TypeBoostItem */
    public TypeBoostItem(String name, MonsterType type) {
        super(name);
        m_type = type;
    }
    
    public TypeBoostItem(String name, MonsterType type, double factor) {
        this(name, type);
        m_factor = factor;
    }
    
    public boolean isMoveTransformer(boolean enemy) {
        return !enemy;
    }
    
    /**
     * Boost the power of one type of move by 10%.
     */
    public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
        MonsterMove move = entry.getMove();
        if (move.getType().equals(m_type)) {
            move.setPower((int)((double)move.getPower() * m_factor));
        }
        return entry;
    }
}

/**
 * Raises the power of STAB moves for a particular monster.
 */
class StabOrbItem extends HoldItem {
    private String m_pokemon;
    
    public StabOrbItem(String name, String monster) {
        super(name);
        m_pokemon = monster;
    }
    
    public boolean isMoveTransformer(boolean enemy) {
        return !enemy;
    }
    
    public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
        if (!p.getSpeciesName().equals(m_pokemon)) {
            // No effect for Pokemon other than the specified one.
            return entry;
        }
        MonsterMove move = entry.getMove();
        MonsterType type = move.getType();
        if (p.isType(type)) {
            move.setPower((int)((double)move.getPower() * 1.2));
        }
        return entry;
    }
}

/**
 * An item that cuts a stat of the opponent when the monster with the item
 * switches in.
 */
class SwitchInBoostItem extends HoldItem {
    private int m_stat;
    private double m_mul;
    
    public SwitchInBoostItem(String name, int stat, double mul) {
        super(name);
        m_stat = stat;
        m_mul = mul;
    }
    
    public boolean apply(Monster p) {
        p.getMultiplier(m_stat).multiplyBy(m_mul);
        return true;
    }
    
    public void unapply(Monster p) {
        p.getMultiplier(m_stat).divideBy(m_mul);
    }
}

/**
 * Raise the damage done by one type of move (physical/special).
 */
class SpecialnessBoostItem extends HoldItem {
    private boolean m_special;
    
    public SpecialnessBoostItem(String name, boolean special) {
        super(name);
        m_special = special;
    }
    
    public boolean isMoveTransformer(boolean enemy) {
        return !enemy;
    }

    public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
        MonsterMove move = entry.getMove();
        if (move.isSpecial(p.getField().getMechanics()) == m_special) {
            move.setPower((int)((double)move.getPower() * 1.1));
        }
        return entry;
    }
}

/**
 * Item that applies a special effect to the user.
 */
class SpecialEffectItem extends HoldItem {
    private StatusEffect m_effect;
    
    public SpecialEffectItem(String name, StatusEffect effect) {
        super(name);
        m_effect = effect;
    }
    
    public int getTier() {
        return 3;
    }
    
    public boolean tick(Monster p) {
        if (!p.hasEffect(StatusEffect.SPECIAL_EFFECT_LOCK)) {
            p.addStatus(p, m_effect);
        }
        return false;
    }

}

/**
 * Destiny Knot: If attract is applied, foe is also attracted.
 */
class DestinyKnotItem extends HoldItem implements StatusListener {
    public DestinyKnotItem() {
        super("Destiny Knot");
    }
    public void informStatusApplied(Monster source, Monster p, StatusEffect eff) {
        if ((eff instanceof MoveList.AttractEffect) && (source != null)) {
            source.addStatus(p, eff);
        }
    }
    public void informStatusRemoved(Monster p, StatusEffect eff) {
        
    }
}

/**
 * Weakens the super-effective moves of one type used against the holder.
 */
class EffectiveMoveWeakener extends HoldItem implements Berry {
    private MonsterType m_type;
    private boolean m_suitable;
    public EffectiveMoveWeakener(String name, MonsterType type) {
        super(name);
        m_type = type;
    }
    public boolean isMoveTransformer(boolean enemy) {
        return enemy;
    }
    private boolean isAppropriateMove(Monster p, MonsterMove move) {
        return (MonsterType.T_NORMAL.equals(m_type)
                    || (move.getEffectiveness(p.getOpponent(), p) > 1.0))
                && move.getType().equals(m_type) && move.isDamaging();
    }
    public MoveListEntry getEnemyTransformedMove(Monster p, MoveListEntry entry) {
        MonsterMove move = entry.getMove();
        m_suitable = (isAppropriateMove(p, move) && !p.hasSubstitute());
        if (m_suitable) {
            move.setPower((int)(((double)move.getPower()) * 0.5));
        }
        return entry;
    }
    public boolean isListener() {
        return true;
    }
    public void informDamaged(Monster source, Monster target, MoveListEntry entry, int damage) {
        @SuppressWarnings("unused")
		MonsterMove move = entry.getMove();
        if (m_suitable) {
            target.getField().showMessage("The " + getName() + " weakened "
                    + entry.getName() + "'s power!");
            target.removeStatus(this);            
        }
    }
    public void executeEffects(Monster p) {
        // Activating this wouldn't make very much sense.
    }
}

/*
 * Raises one of Clamperl's stats.
 */
class DeepSeaItem extends HoldItem {
    private int m_stat;
    public DeepSeaItem(String name, int stat) {
        super(name);
        m_stat = stat;
    }
    public boolean isSuitable(Monster p) {
        return p.getSpeciesName().equals("Clamperl");
    }
    public boolean apply(Monster p) {
        if (isSuitable(p)) {
            p.getMultiplier(m_stat).multiplyBy(2.0);
        }
        return super.apply(p);
    }
    public void unapply(Monster p) {
        if (isSuitable(p)) {
            p.getMultiplier(m_stat).divideBy(2.0);
        }
        super.unapply(p);
    }
}

/**
 * This class implements an item that can be held by a monster during battle.
 * These type of classes basically provide the same functionality as intrinsic
 * abilities, so the class inherits from IntrinsicAbility, even though the
 * semantic value of this is questionable.
 *
 * @author Colin
 */
public class HoldItem extends IntrinsicAbility {
    
    private static HoldItemData m_default = new HoldItemData();
    
    static {
        new TypeBoostItem("Black Belt", MonsterType.T_FIGHTING);
        new TypeBoostItem("BlackGlasses", MonsterType.T_DARK);
        new TypeBoostItem("Charcoal", MonsterType.T_FIRE);
        new TypeBoostItem("Dragon Fang", MonsterType.T_DRAGON);
        new TypeBoostItem("Hard Stone", MonsterType.T_ROCK);
        new TypeBoostItem("Magnet", MonsterType.T_ELECTRIC);
        new TypeBoostItem("Metal Coat", MonsterType.T_STEEL);
        new TypeBoostItem("Miracle Seed", MonsterType.T_GRASS);
        new TypeBoostItem("Mystic Water", MonsterType.T_WATER);
        new TypeBoostItem("Nevermeltice", MonsterType.T_ICE);
        new TypeBoostItem("Poison Barb", MonsterType.T_POISON);
        new TypeBoostItem("Sharp Beak", MonsterType.T_FLYING);
        new TypeBoostItem("Silk Scarf", MonsterType.T_NORMAL);
        new TypeBoostItem("Silverpowder", MonsterType.T_BUG);
        new TypeBoostItem("Soft Sand", MonsterType.T_GROUND);
        new TypeBoostItem("Spell Tag", MonsterType.T_GHOST);
        new TypeBoostItem("Twisted Spoon", MonsterType.T_PSYCHIC);
        
        new StatBoostItem("Liechi Berry", Monster.S_ATTACK);
        new StatBoostItem("Ganlon Berry", Monster.S_DEFENCE);
        new StatBoostItem("Salac Berry", Monster.S_SPEED);
        new StatBoostItem("Petaya Berry", Monster.S_SPATTACK);
        new StatBoostItem("Apicot Berry", Monster.S_SPDEFENCE);
        // new StatBoostItem("Lansat Berry", Pokemon.S_CRITICAL_HITS);
        new StatBoostItem("Starf Berry", -1) {
            public int getStat(Monster p) {
                Random random = p.getField().getMechanics().getRandom();
                return random.nextInt(5) + 1;
            }
            public int getStages() {
                return 2;
            }
        };
        
        new HealthBoostItem("Figy Berry", Monster.S_ATTACK);
        new HealthBoostItem("Wiki Berry", Monster.S_SPATTACK);
        new HealthBoostItem("Mago Berry", Monster.S_SPEED);
        new HealthBoostItem("Aguav Berry", Monster.S_SPDEFENCE);
        new HealthBoostItem("Iapapa Berry", Monster.S_DEFENCE);
        new ConstantHealthBoostItem("Oran Berry", 10);
        new ConstantHealthBoostItem("Sitrus Berry", 30);
        
        new StatusCureItem("Aspear Berry", FreezeEffect.class);
        new StatusCureItem("Cheri Berry", ParalysisEffect.class);
        new StatusCureItem("Chesto Berry", SleepEffect.class);
        // Note: self-referential just for fun; parameter is unused.
        new StatusCureItem("Lum Berry", Class.class) {
            public boolean isCurable(StatusEffect eff) {
                return (eff.getLock() == StatusEffect.SPECIAL_EFFECT_LOCK)
                        || (eff instanceof ConfuseEffect);
            }
        };
        new StatusCureItem("Pecha Berry", PoisonEffect.class);
        new StatusCureItem("Persim Berry", ConfuseEffect.class);
        new StatusCureItem("Rawst Berry", BurnEffect.class);
        new StatusCureItem("Mental Herb", MoveList.AttractEffect.class);
        new WhiteHerbItem();
        
        new HoldItem("Leftovers") {
            public int getTier() {
                return 3;
            }
            public boolean tick(Monster p) {
                int max = p.getStat(Monster.S_HP);
                if (p.getHealth() < max) {
                    int restore = max / 16;
                    p.getField().showMessage(p.getName()
                        + "'s leftovers restored its health a little!");
                    p.changeHealth(restore);
                }
                return false;
            }
        };
        
        new ChoiceBandItem("Choice Band", Monster.S_ATTACK);
        
        new HoldItem("Thick Club") {
            public boolean isSuitable(Monster p) {
                String name = p.getSpeciesName();
                return (name.equals("Cubone") || name.equals("Marowak"));
            }
            public boolean apply(Monster p) {
                if (isSuitable(p)) {
                    p.getMultiplier(Monster.S_ATTACK).multiplyBy(2.0);
                }
                return true;
            }
            public void unapply(Monster p) {
                if (isSuitable(p)) {
                    p.getMultiplier(Monster.S_ATTACK).divideBy(2.0);
                }
            }
        };
        
        new HoldItem("Metal Powder") {
            public boolean isSuitable(Monster p) {
                return p.getSpeciesName().equals("Ditto");
            }
            public boolean apply(Monster p) {
                if (isSuitable(p)) {
                    //TODO: these effects should be removed when transformed
                    p.getMultiplier(Monster.S_DEFENCE).multiplyBy(2.0);
                    p.getMultiplier(Monster.S_SPDEFENCE).multiplyBy(2.0);
                }
                return true;
            }
            public void unapply(Monster p) {
                if (isSuitable(p)) {
                    p.getMultiplier(Monster.S_DEFENCE).multiplyBy(2.0);
                    p.getMultiplier(Monster.S_SPDEFENCE).multiplyBy(2.0);
                }
            }
        };
        
        /** Diamond/Pearl-exclusive items begin here. */
        
        new ChoiceBandItem("Choice Specs", Monster.S_SPATTACK);
        
        // Arceus plates.
        new ArceusPlate("Draco Plate", MonsterType.T_DRAGON);
        new ArceusPlate("Dread Plate", MonsterType.T_DARK);
        new ArceusPlate("Earth Plate", MonsterType.T_GROUND);
        new ArceusPlate("Flame Plate", MonsterType.T_FIRE);
        new ArceusPlate("Fist Plate", MonsterType.T_FIGHTING);
        new ArceusPlate("Icicle Plate", MonsterType.T_ICE);
        new ArceusPlate("Insect Plate", MonsterType.T_BUG);
        new ArceusPlate("Iron Plate", MonsterType.T_STEEL);
        new ArceusPlate("Meadow Plate", MonsterType.T_GRASS);
        new ArceusPlate("Mind Plate", MonsterType.T_PSYCHIC);
        new ArceusPlate("Sky Plate", MonsterType.T_FLYING);
        new ArceusPlate("Splash Plate", MonsterType.T_WATER);
        new ArceusPlate("Stone Plate", MonsterType.T_ROCK);
        new ArceusPlate("Toxic Plate", MonsterType.T_POISON);
        new ArceusPlate("Zap Plate", MonsterType.T_ELECTRIC);
        
        // TODO (tbd) : Full Incense
        
        new SwitchInBoostItem("Lax Incense", Monster.S_EVASION, 1.05);
        new HoldItem("Luck Incense"); // Does nothing.
        new HoldItem("Pure Incense"); // Does nothing.
        new TypeBoostItem("Odd Incense", MonsterType.T_PSYCHIC);
        new TypeBoostItem("Rock Incense", MonsterType.T_ROCK);
        new TypeBoostItem("Rose Incense", MonsterType.T_GRASS);
        new TypeBoostItem("Sea Incense", MonsterType.T_WATER, 1.05);
        new TypeBoostItem("Wave Incense", MonsterType.T_WATER);
        
        new StabOrbItem("Adamant Orb", "Dialga");
        new StabOrbItem("Lustrous Orb", "Palkia");
        
        new HoldItem("Black Sludge") {
            public int getTier() {
                return 3;
            }
            public boolean tick(Monster p) {
                int max = p.getStat(Monster.S_HP);
                int delta = max / 16;
                boolean damage = !p.isType(MonsterType.T_POISON);
                if (damage) {
                    p.getField().showMessage(p.getName()
                        + " was damaged by Black Sludge!");
                    p.changeHealth(-delta, true);
                } else if (p.getHealth() < max) {
                    p.getField().showMessage(p.getName()
                        + "'s Black Sludge restored a little health!");
                    p.changeHealth(delta);
                }
                return false;
            }
        };
        
        new HoldItem("Blue Scarf"); // Does nothing.
        new SwitchInBoostItem("Brightpowder", Monster.S_EVASION, 1.10);
        
        new SpecialnessBoostItem("Muscle Band", false);
        
        new DestinyKnotItem();
        new HoldItem("Scope Lens");
        
        new HoldItem("Life Orb") {
            public void switchIn(Monster p) {
                p.getField().applyEffect(new FieldEffect() {
                    private boolean[] m_damaged = new boolean[2];
                    public boolean applyToField(BattleField field) {
                        return true;
                    }
                    public int getTier() {
                        return -1;
                    }
                    public boolean apply(Monster p) {
                        m_damaged[p.getParty()] = false;
                        return true;
                    }
                    public void beginTick() {
                        super.beginTick();
                        Arrays.fill(m_damaged, false);
                    }
                    public String getName() {
                        return null;
                    }
                    public String getDescription() {
                        return null;
                    }
                    public boolean tickField(BattleField field) {
                        return false;
                    }
                    public boolean isListener() {
                        return true;
                    }
                    public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                        if (source.getItemName().equals("Life Orb")) {
                            int idx = source.getParty();
                            if (!m_damaged[idx]) {
                                m_damaged[idx] = true;
                                source.changeHealth(-source.getStat(Monster.S_HP) / 10, true);
                            }
                        }
                    }
                    public boolean isMoveTransformer(boolean enemy) {
                        return !enemy;
                    }
                    public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                        if (p.getItemName().equals("Life Orb")) {
                            MonsterMove move = entry.getMove();
                            move.setPower((int)(((double)move.getPower()) * 1.3));
                        }
                        return entry;
                    }
                });
            }
        };
        
        new HoldItem("Light Ball") {
            public void modifyStats(Monster p, boolean apply) {
                if (!p.getSpeciesName().equals("Pikachu")) {
                    return;
                }
                BattleField field = p.getField();
                boolean dp = ((field != null) &&
                        (field.getMechanics() instanceof JewelMechanics));
                if (apply) {
                    if (dp) {
                        p.getMultiplier(Monster.S_ATTACK).multiplyBy(2.0);
                    }
                    p.getMultiplier(Monster.S_SPATTACK).multiplyBy(2.0);
                } else {
                    if (dp) {
                        p.getMultiplier(Monster.S_ATTACK).divideBy(2.0);
                    }
                    p.getMultiplier(Monster.S_SPATTACK).divideBy(2.0);
                }
            }
            public boolean apply(Monster p) {
                modifyStats(p, true);
                return super.apply(p);
            }
            public void unapply(Monster p) {
                modifyStats(p, false);
            }
        };
        
        new HoldItem("Wide Lens") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }

            public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                move.setAccuracy(move.getAccuracy() * 1.1);
                return entry;
            }
        };
        
        new HoldItem("Expert Belt") {
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }

            public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                MonsterMove move = entry.getMove();
                if (move.getEffectiveness(p, p.getOpponent()) > 1.0) {
                    move.setPower((int)(((double)move.getPower()) * 1.2));
                }
                return entry;
            }
        };
        
        new SpecialnessBoostItem("Wise Glasses", true);
        
        new ChoiceBandItem("Choice Scarf", Monster.S_SPEED);
        
        new SpecialEffectItem("Toxic Orb", new ToxicEffect());
        new SpecialEffectItem("Flame Orb", new BurnEffect());

        new EffectiveMoveWeakener("Occa Berry", MonsterType.T_FIRE);
        new EffectiveMoveWeakener("Passho Berry", MonsterType.T_WATER);
        new EffectiveMoveWeakener("Wacan Berry", MonsterType.T_ELECTRIC);
        new EffectiveMoveWeakener("Rindo Berry", MonsterType.T_GRASS);
        new EffectiveMoveWeakener("Yache Berry", MonsterType.T_ICE);
        new EffectiveMoveWeakener("Chople Berry", MonsterType.T_FIGHTING);
        new EffectiveMoveWeakener("Kebia Berry", MonsterType.T_POISON);
        new EffectiveMoveWeakener("Shuca Berry", MonsterType.T_GROUND);
        new EffectiveMoveWeakener("Coba Berry", MonsterType.T_FLYING);
        new EffectiveMoveWeakener("Payapa Berry", MonsterType.T_PSYCHIC);
        new EffectiveMoveWeakener("Tanga Berry", MonsterType.T_BUG);
        new EffectiveMoveWeakener("Charti Berry", MonsterType.T_ROCK);
        new EffectiveMoveWeakener("Kasib Berry", MonsterType.T_GHOST);
        new EffectiveMoveWeakener("Haban Berry", MonsterType.T_DRAGON);
        new EffectiveMoveWeakener("Colbur Berry", MonsterType.T_DARK);
        new EffectiveMoveWeakener("Babiri Berry", MonsterType.T_STEEL);
        new EffectiveMoveWeakener("Chilan Berry", MonsterType.T_NORMAL);
        
        new HoldItem("Soul Dew") {
            public boolean isSuitable(Monster p) {
                String name = p.getSpeciesName();
                return (name.equals("Latios") || name.equals("Latias"));
            }
            public boolean apply(Monster p) {
                if (isSuitable(p)) {
                    p.getMultiplier(Monster.S_SPATTACK).multiplyBy(1.5);
                    p.getMultiplier(Monster.S_SPDEFENCE).multiplyBy(1.5);
                }
                return true;
            }
            public void unapply(Monster p) {
                if (isSuitable(p)) {
                    p.getMultiplier(Monster.S_SPATTACK).divideBy(1.5);
                    p.getMultiplier(Monster.S_SPDEFENCE).divideBy(1.5);
                }
            }
        };
        
        /** The actual logic for these two is in shoddybattle.Pokemon
         *  It is a bit ugly, but oh well. */
        new HoldItem("Focus Sash");
        new HoldItem("Focus Band");
        
        // The implementation of these is in WeatherMove.
        new HoldItem("Heat Rock");
        new HoldItem("Damp Rock");
        new HoldItem("Icy Rock");
        new HoldItem("Smooth Rock");
        
        // The implementation for this is in MoveList.TrappingEffect.
        new HoldItem("Shed Shell");
        
        /** The implementation for this is in the stupidly named
         *  MoveList.StatCutEffect class. */
        new HoldItem("Light Clay");
        
        new DeepSeaItem("Deepseatooth", Monster.S_SPATTACK);
        new DeepSeaItem("Deepseascale", Monster.S_SPDEFENCE);
        
        new HoldItem("Metronome") {
            private int m_level = 0;
            private MoveListEntry m_choice = null;
            public void switchIn(Monster p) {
                m_choice = null;
            }
            public boolean apply(Monster p) {
                m_choice = null;
                return true;
            }
            public boolean isMoveTransformer(boolean enemy) {
                return !enemy;
            }
            public MoveListEntry getTransformedMove(Monster p, MoveListEntry entry) {
                if (m_choice == null) {
                    m_choice = entry;
                    m_level = 0;
                } else if (entry.equals(m_choice)) {
                    ++m_level;
                } else {
                    m_choice = null;
                    return entry;
                }
                MonsterMove move = entry.getMove();
                move.setPower(move.getPower() * (10 + m_level) / 10);
                return entry;
            }
        };
        
        new HoldItem("Macho Brace") {
            public boolean apply(Monster p) {
                p.getMultiplier(Monster.S_SPEED).divideBy(2.0);
                return super.apply(p);
            }
            public void unapply(Monster p) {
                p.getMultiplier(Monster.S_SPEED).multiplyBy(2.0);
            }
        };
        
        new HoldItem("Shell Bell") {
            public void switchIn(Monster p) {
                p.getField().applyEffect(new FieldEffect() {
                    private boolean[] m_damaged = new boolean[2];
                    public boolean applyToField(BattleField field) {
                        return true;
                    }
                    public int getTier() {
                        return -1;
                    }
                    public boolean apply(Monster p) {
                        m_damaged[p.getParty()] = false;
                        return true;
                    }
                    public void beginTick() {
                        super.beginTick();
                        Arrays.fill(m_damaged, false);
                    }
                    public String getName() {
                        return null;
                    }
                    public String getDescription() {
                        return null;
                    }
                    public boolean tickField(BattleField field) {
                        return false;
                    }
                    public boolean isListener() {
                        return true;
                    }
                    public void informDamaged(Monster source, Monster target, MoveListEntry move, int damage) {
                        if (source.getItemName().equals("Shell Bell")) {
                            int idx = source.getParty();
                            if (!m_damaged[idx]) {
                                m_damaged[idx] = true;
                                source.getField().showMessage(source.getName() + "'s Shell Bell " +
                                    "restored a little health!");
                                int change = damage / 8;
                                if (change < 1) change = 1;
                                source.changeHealth(change);
                            }
                        }
                    }
                });
            }
        };
    }
    
    protected void registerAbility() {
        super.registerAbility();
        m_default.m_items.add(getName());
    }
    
    /**
     * Return the default item data.
     */
    public static HoldItemData getDefaultData() {
        return m_default;
    }
    
    /**
     * Initialise a HoldItem that can be used only by certain named monster.
     */
    public HoldItem(String name, String[] monster) {
        super(false, name);
        for (int i = 0; i < monster.length; ++i) {
            m_default.addExclusiveItem(name, monster[i]);
        }
    }
    
    public HoldItem(boolean register, String name) {
        super(register, name);
    }
    
    /** Creates a new instance of HoldItem */
    public HoldItem(String name) {
        super(name);
    }
    
}
