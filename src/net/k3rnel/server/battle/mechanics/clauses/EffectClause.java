package net.k3rnel.server.battle.mechanics.clauses;

import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.statuses.StatusEffect;

/**
 * This clause prevents a trainer from putting a particular status on more
 * than one of the opponent's monsters at a time.
 * 
 * @author Colin
 */
@SuppressWarnings("unchecked")
public abstract class EffectClause extends Clause {

	private Class m_effect;
    
    /**
     * @param effect the status effect to restrict
     */
    public EffectClause(String name, Class effect) {
        super(name);
        m_effect = effect;
    }
    
    public boolean equals(Object o2) {
        if (!(o2 instanceof EffectClause))
            return false;
        if (o2 == null)
            return false;
        return ((EffectClause)o2).m_effect.equals(m_effect);
    }
    
    public boolean allowsStatus(StatusEffect eff, Monster source, Monster target) {
        if (source == target)
            return true;
        if (!m_effect.isAssignableFrom(eff.getClass()))
            return true;
        /** See if the opponent already has a monster with this effect and that
         *  that effect was induced by this enemy trainer. */
        Monster[] party = target.getTeammates();
        for (int i = 0; i < party.length; ++i) {
            Monster mon = party[i];
            if (mon.isFainted())
                continue;
            StatusEffect effect = mon.getEffect(m_effect);
            if (effect != null) {
                Monster inducer = effect.getInducer();
                if ((inducer != null) &&
                    (inducer.getParty() == source.getParty()))
                    return false;
            }
        }
        return true;
    }

}
