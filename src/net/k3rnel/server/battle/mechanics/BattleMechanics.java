package net.k3rnel.server.battle.mechanics;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Random;

import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.mechanics.moves.MonsterMove;

/**
 * This class represents the mechanics for a battle in a particular generation
 * of monster. Derive classes from this class to implement a desired generation.
 *
 * @author Colin
 */
public abstract class BattleMechanics implements Serializable {

	/**
	 * Constant version of this class.
	 */
	private static final long serialVersionUID = 2907773868045621558L;

	/**
	 * One universal random number generator is defined here for the whole
	 * server so that there are not several identical streams of random
	 * numbers kicking around.
	 */
	@SuppressWarnings("unused")
	private static final Random m_masterRandom;

	/**
	 * A random number generator specific to this instance of the mechanics.
	 */
	private final Random m_random;

	/**
	 * Calculate the initial value of a stat from a monster's base stats and
	 * hidden stats.
	 *
	 * @param p the monster whose stats to calculate
	 * @param i the stat to calculate (use the constants monster.S_HP, etc.)
	 */
	abstract public int calculateStat(Monster p, int i) throws StatException;

	/**
	 * Validate the hidden stats of a monster.
	 */
	abstract public void validateHiddenStats(Monster p)
	throws ValidationException;

	/**
	 * Randomly decide whether a move hits.
	 */
	abstract public boolean attemptHit(MonsterMove move,
			Monster user,
			Monster target);

	/**
	 * Calculate the damage done by a move.
	 * Does not actually inflict damage to monster.
	 * Optionally do not display any messages.
	 */
	abstract public int calculateDamage(MonsterMove move,
			Monster attacker,
			Monster defender,
			boolean silent);

	/**
	 * Calculate the damage done by a move.
	 * Does not actually inflict damage to monster.
	 */
	public int calculateDamage(MonsterMove move,
			Monster attacker,
			Monster defender) {
		return calculateDamage(move, attacker, defender, false);
	}

	/**
	 * Return whether a given move deals special damage.
	 */
	public abstract boolean isMoveSpecial(MonsterMove move);

	/**
	 * Get an instance of the Random class.
	 */
	public final Random getRandom() {
		return m_random;
	}

	/**
	 * Initialise an instance of the mechanics.
	 */
	public BattleMechanics(int bytes) {
		if (bytes == 4) {
			m_random = new Random();
		} else {
			m_random = getRandomSource(bytes);
		}
	}

	static {
		m_masterRandom = getRandomSource(25);
	}

	/**
	 * Returns the amount of EXP required to reach a level
	 * based on a monster's EXP type
	 * @param poke
	 * @param level
	 * @return
	 */
	public double getExpForLevel(Monster poke, int level){
		double exp = 0;
		switch (poke.getExpType()){
		case MEDIUM: 
			exp = (int)java.lang.Math.pow((double)level, 3);
			break;
		case ERRATIC: 
			double p = 0;
			switch (level % 3){
			case 0:
				p = 0;
				break;
			case 1:
				p = 0.008;
				break;
			case 2:
				p = 0.014;
				break;
			}
			if (level <= 50){
				exp = java.lang.Math.pow((double)level, 3) * ((100 - (double)level) / 50);
			} else if (level <= 68){
				exp = java.lang.Math.pow((double)level, 3) * ((150 - (double)level) / 50);
			} else if (level <= 98){
				exp = (java.lang.Math.pow((double)level, 3) *
						(1.274 - ((1/50) * ((double)level / 3)) - p));
			} else {
				exp = java.lang.Math.pow((double)level, 3) * ((160 - (double)level) / 50);
			}
			break;
		case FAST: 
			exp = 4 * java.lang.Math.pow((double)level, 3) / 5;
			break;
		case FLUCTUATING: 
			if (level <= 15){
				exp = java.lang.Math.pow((double)level, 3) * 
					((24 + (((double)level + 1) / 3)) / 50);
			} else if ((double)level <= 35){
				exp = java.lang.Math.pow((double)level, 3) * 
				((14 + (double)level) / 50);
			} else {
				exp = java.lang.Math.pow((double)level, 3) * 
				((32 + ((double)level / 2)) / 50);
			}    		
			break;
		case PARABOLIC: 
			exp = (6 * (java.lang.Math.pow((double)level, 3) / 5)) - 
			(15 * java.lang.Math.pow((double)level, 2)) + (100 * (double)level) - 140;
			break;
		case SLOW: 
			exp = 5 * java.lang.Math.pow((double)level, 3) / 4;;
			break;
		}
		return exp;
	}

	/**
	 * Initialise the battle mechanics. Try to use the SecureRandom class for
	 * the random number generator, with a seed from /dev/random. However, if
	 * /dev/random is unavailable (e.g. if we are running on Windows) then an
	 * instance of Random, seeded from the time, is used instead.
	 *
	 * For best results, use an operating system that supports /dev/random.
	 */
	public static Random getRandomSource(int bytes) {
		try {
			return new SecureRandom(/*seed*/);
		} catch (Exception e) {
			System.out.println("Could not use SecureRandom: " + e.getMessage());
			return new Random();
		}
	}
	
	/**
	 * Calcultes the level of a monster based on their EXP amount
	 * @param a
	 * @return
	 */
	public int calculateLevel(Monster a){
		double result = 0;
		switch (a.getExpType())	{
		case MEDIUM:
		{
			for(double i = 1; i <= 100; i++)
			{
				if(i < 100 && a.getExp() >= (i * i * i) && a.getExp() < ((i + 1) * (i + 1) * (i + 1)))
				{
					result = i;
					break;
				}
				else if(i == 100 && a.getExp() >= (i * i * i))
				{
					result = 100;
					break;
				}
				else if(a.getExp() >= 1000000)
				{
					result = 100;
					a.setExp(1000000);
					break;
				}
			}
		}
		break;
		case ERRATIC:
		{
			for(double i = 1; i < 101; i++)
			{
				if(i < 50)
				{
					if((a.getExp()) >= ((i * i * i)*((100 - i)/50)) && (a.getExp()) < (((i + 1) * (i + 1) * (i + 1))*((100 - (i + 1))/50)))
					{
						result = i;
						break;
					}
				}
				else if(i == 50)
					result = i;
				else if(i >= 51 && i < 68)
				{
					if((a.getExp()) >= ((i * i * i)*((150 - i)/50)) && (a.getExp()) < (((i + 1) * (i + 1) * (i + 1))*((150 - (i + 1))/50)))
					{
						result = i;
						break;
					}				
				}
				else if(i == 68)
					result = i;
				else if(i >= 69 && i < 98)
				{
					double temp = a.getExp();
					double funt = i % 3;
					double funt1 = (i + 1) % 3;
					if(funt == 1)
						funt = 0.008;
					else if(funt == 2)
						funt = 0.014;
					if(funt1 == 1)
						funt1 = 0.008;
					else if(funt1 == 2)
						funt1 = 0.014;		
					if(temp >= ((i * i * i) * (1.274 - ((1 / 50)*(i / 3)) - funt)) && temp < (((i + 1) * (i + 1) * (i + 1)) * (1.274 - ((1 / 50)*((i + 1) / 3)) - funt1)))
					{
						result = i;
						break;
					}
				}
				else if(i == 98)
					result = i;
				else if(i == 99)
				{
					if((a.getExp()) >= ((i * i * i)*((160 - i)/50)) && (a.getExp()) < (((i + 1) * (i + 1) * (i + 1))*((160 - (i + 1))/50)))
					{
						result = i;
						break;
					}						
				}
				else if(i == 100)
				{
					if((a.getExp()) >= ((i * i * i)*((160 - i)/50)))
					{
						result = i;
						break;
					}						
				}
				else if(a.getExp() >= 600000)
				{
					result = 100;
					a.setExp(600000);
					break;
				}
			}
		}
		break;
		case FLUCTUATING:
		{
			for(double i = 101; i > 36; i--)
			{
				if((a.getExp()) < ((i * i * i)*((32 + (i / 2))/50)))
					result = i - 1;		
			}
			for(double i = 36; i > 15; i--)
			{
				System.out.println(i);
				if((a.getExp()) < ((i * i * i)*((14 + i)/50)))
					result = i - 1;
			}
			for(double i = 15; i > 1; i--)
			{
				double reqExp = (i * i * i)*((24 + ((i + 1) / 3))/50);
				if((a.getExp()) < reqExp)
					result = i - 1;
			}
			if(a.getExp() >= 1640000)
			{
				result = 100;
				a.setExp(1640000);
				break;
			}
		}

		break;
		case PARABOLIC:
		{
			for(double i = 101; i > 1; i--)
			{
				if(a.getExp() < (((6 * (i * i * i))/5) - (15 * (i * i)) + (100 * i) - 140))
				{
					result = i - 1;
				}
				else if(a.getExp() >= 1059860)
				{
					result = 100;
					a.setExp(1059860);
					break;
				}
			}
		}
		break;
		case FAST:
		{
			for(double i = 101; i > 1; i--)
			{
				if(a.getExp() < ((4 * (i * i * i))/5))
				{
					result = i - 1;
				}
				else if(a.getExp() >= 800000)
				{
					result = 100;
					a.setExp(800000);
					break;
				}
			}
		}
		break;
		case SLOW:
		{
			for(double i = 101; i > 1; i--)
			{
				if(a.getExp() < ((5 * (i * i * i))/4))
				{
					result = i - 1;
				}
				else if(a.getExp() >= 1250000)
				{
					result = 100;
					a.setExp(1250000);
					break;
				}
			}
		}
		}
		return (int) result;
	}

	/**
	 * Calculates the EXP gained (per monster who defeated it) from defeating a monster
	 * @param a - The defeated monster
	 * @param u - How many monster defeated it
	 * @return
	 */
	public double calculateExpGain(Monster a, int u){
		double result = (((((a.getLevel() * a.getBaseExp())/7))/u));
		return result / 2;
	}
	
	/**
	 * Returns true if a monster was successfully caught
	 * @param monster
	 * @param rate
	 * @param net
	 * @param status
	 * @return
	 */
	public boolean isCaught(Monster monster, int rate, double net, int status){
		int maxHP = monster.getStat(Monster.S_HP);
		int currentHP = monster.getHealth();
		//maxHP represents the max HP of the wild monster
		//currentHP represents the current HP of the wild monster
		//rate represents the monsters catch rate by default
		//net is the thrown ball's catch rate(1 for Small Net; 1.5 for Great Net; 2 for Ultra Net; 255 for MasterNet)
		//status represents any status ailments the monster has (2 for sleep and freeze, 1.5 for paralyze, poison and burn), if none set to 1

		//The following calculation recalculates the monster's catch rate with status ailments/currentHP/pokeball used/etc. taken into account
		double a = 0;
		a = ((((((3 * maxHP) - (2 * currentHP)) * rate) * net) / (3 * maxHP)) * status);
		if(a >= 255){
			return true;
		} else {
			double b = 1048560 / (Math.sqrt(Math.sqrt((16711680 / a))));
            double po = getRandom().nextInt(65536);
            double pt = getRandom().nextInt(65536);
            double pth = getRandom().nextInt(65536);
            double pf = getRandom().nextInt(65536);
			if(po <= b)
				if(pt <= b)
					if(pth <= b)
						if(pf <= b)
							return true;
						else
							return false;
					else
						return false;
				else
					return false;
			else
				return false;
		}
	}
}
