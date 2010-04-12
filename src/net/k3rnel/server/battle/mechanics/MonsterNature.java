package net.k3rnel.server.battle.mechanics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import net.k3rnel.server.battle.Monster;

import org.simpleframework.xml.Element;

/**
 * This class represents the nature of a monster in the advance generation.
 *
 * @author Colin
 */
public class MonsterNature implements Serializable {
    
    private static final long serialVersionUID = -549059028197342801L;
    
    @SuppressWarnings("unused")
	@Element
    /*serializable*/ private int m_nature;
        
    private static final ArrayList<MonsterNature> m_natures = new ArrayList<MonsterNature>();
    
    public static final MonsterNature N_LONELY = new MonsterNature("Lonely", Monster.S_ATTACK, Monster.S_DEFENCE);
    public static final MonsterNature N_BRAVE = new MonsterNature("Brave", Monster.S_ATTACK, Monster.S_SPEED);
    public static final MonsterNature N_ADAMANT = new MonsterNature("Adamant", Monster.S_ATTACK, Monster.S_SPATTACK);
    public static final MonsterNature N_NAUGHTY = new MonsterNature("Naughty", Monster.S_ATTACK, Monster.S_SPDEFENCE);
    public static final MonsterNature N_BOLD = new MonsterNature("Bold", Monster.S_DEFENCE, Monster.S_ATTACK);
    public static final MonsterNature N_RELAXED = new MonsterNature("Relaxed", Monster.S_DEFENCE, Monster.S_SPEED);
    public static final MonsterNature N_IMPISH = new MonsterNature("Impish", Monster.S_DEFENCE, Monster.S_SPATTACK);
    public static final MonsterNature N_LAX = new MonsterNature("Lax", Monster.S_DEFENCE, Monster.S_SPDEFENCE);
    public static final MonsterNature N_TIMID = new MonsterNature("Timid", Monster.S_SPEED, Monster.S_ATTACK);
    public static final MonsterNature N_HASTY = new MonsterNature("Hasty", Monster.S_SPEED, Monster.S_DEFENCE);
    public static final MonsterNature N_JOLLY = new MonsterNature("Jolly", Monster.S_SPEED, Monster.S_SPATTACK);
    public static final MonsterNature N_NAIVE = new MonsterNature("Naive", Monster.S_SPEED, Monster.S_SPDEFENCE);
    public static final MonsterNature N_MODEST = new MonsterNature("Modest", Monster.S_SPATTACK, Monster.S_ATTACK);
    public static final MonsterNature N_MILD = new MonsterNature("Mild", Monster.S_SPATTACK, Monster.S_DEFENCE);
    public static final MonsterNature N_QUIET = new MonsterNature("Quiet", Monster.S_SPATTACK, Monster.S_SPEED);
    public static final MonsterNature N_RASH = new MonsterNature("Rash", Monster.S_SPATTACK, Monster.S_SPDEFENCE);
    public static final MonsterNature N_CALM = new MonsterNature("Calm", Monster.S_SPDEFENCE, Monster.S_ATTACK);
    public static final MonsterNature N_GENTLE = new MonsterNature("Gentle", Monster.S_SPDEFENCE, Monster.S_DEFENCE);
    public static final MonsterNature N_SASSY = new MonsterNature("Sassy", Monster.S_SPDEFENCE, Monster.S_SPEED);
    public static final MonsterNature N_CAREFUL = new MonsterNature("Careful", Monster.S_SPDEFENCE, Monster.S_SPATTACK);
    public static final MonsterNature N_QUIRKY = new MonsterNature("Quirky", -1, -1);
    public static final MonsterNature N_HARDY = new MonsterNature("Hardy", -1, -1);
    public static final MonsterNature N_SERIOUS = new MonsterNature("Serious", -1, -1);
    public static final MonsterNature N_BASHFUL = new MonsterNature("Bashful", -1, -1);
    public static final MonsterNature N_DOCILE = new MonsterNature("Docile", -1, -1);
    
    @Element
    transient private String m_name;
    @Element
    transient private int m_harms;
    @Element
    transient private int m_benefits;
    
    /**
     * Get a nature by index.
     */
    public static MonsterNature getNature(int i) {
        if ((i < 0) || (i >= m_natures.size()))
            return new MonsterNature("Error", -1, -1);
        return (MonsterNature)m_natures.get(i);
    }
    
    /*private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        MonsterNature nature = getNature(m_nature);
        m_name = nature.m_name;
        m_harms = nature.m_harms;
        m_benefits = nature.m_benefits;
    }*/
    
    public MonsterNature() {
    	// DO NOT USE OUTSIDE OF LOADING DATA
    }
    /**
     * Creates a new instance of MonsterNature by arbitrary indices.
     */
    private MonsterNature(String name, int benefits, int harms) {
        m_name = name;
        m_benefits = benefits;
        m_harms = harms;
        m_nature = m_natures.size();
        m_natures.add(this);
    }
    
    /**
     * Initialise this nature by its name. Note that the first letter should
     * be capital, e.g., "Hardy", "Naive", etc.
     */
    @SuppressWarnings("unused")
	private MonsterNature(String name) {
        Iterator<MonsterNature> i = m_natures.iterator();
        while (i.hasNext()) {
            MonsterNature nature = (MonsterNature)i.next();
            if (name.equals(name)) {
                m_name = name;
                m_benefits = nature.m_benefits;
                m_harms = nature.m_harms;
                break;
            }
        }
    }
    
    /**
     * Get the effect a nature has on a particular stat.
     * This will be 0.9, 1, or 1.1.
     *
     * @param i the index of the statistic
     */
    public double getEffect(int i) {
        return (i == m_benefits) ? 1.1 : ((i == m_harms) ? 0.9 : 1.0);
    }
    
    /**
     * Get a list of natures.
     */
    public static String[] getNatureNames() {
        String[] natures = new String[m_natures.size()];
        Iterator<MonsterNature> i = m_natures.iterator();
        int j = 0;
        while (i.hasNext()) {
            natures[j++] = ((MonsterNature)i.next()).getName();
        }
        return natures;
    }
    
    /**
     * Returns a monster nature based on its name
     * @param nature
     * @return
     */
    public static MonsterNature getNatureByName(String nature) {
    	Iterator<MonsterNature> i = m_natures.iterator();
    	MonsterNature result;
    	while(i.hasNext()) {
    		result = (MonsterNature) i.next();
    		if(result.getName().equalsIgnoreCase(nature))
    			return result;
    	}
    	return N_MODEST;
    }
    
    /**
     * Get a textual representation of the nature.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Gets the stat that this nature benefits
     */
    public int getBenefits() {
        return m_benefits;
    }
    
    /**
     * Gets the stat that this nature hinders
     */
    public int getHarms() {
        return m_harms;
    }
    
}
