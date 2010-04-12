package net.k3rnel.server.battle;

/**
 * 
 * @author ZombieBear
 * 
 */
public class MonsterEgg extends Monster {
	private static final long serialVersionUID = -3895787504332248433L;
	private int m_timeRemaining;

	/**
	 * Constructor
	 * @param poke The baby monster to hatch from the egg
	 * @param time The time in milliseconds for the egg to hatch
	 */
	public MonsterEgg(Monster mon, int time) {
		super(mon);
		m_timeRemaining = time;
	}

	/**
	 * Returns the amount of time remaining
	 * @return
	 */
	public int getTimeRemaining(){
		return m_timeRemaining;
	}
	
	/**
	 * Returns the pokemon held inside the egg
	 * @return
	 */
	public Monster hatch() {
		return (Monster)this;
	}
}