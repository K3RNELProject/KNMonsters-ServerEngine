package net.k3rnel.server.feature;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

/**
 * Stores a database of monster caught by fishing
 * @author shadowkanji
 * @author Fshy
 *
 */
@Root
public class FishDatabase {
	@ElementMap
	private HashMap<String, ArrayList<FishMonsters>> m_database;
	
	/**
	 * Adds an entry to the database
	 * @param monster
	 * @param fishes
	 */
	public void addEntry(String monster, ArrayList<FishMonsters> fishes) {
		if(m_database == null)
			m_database = new HashMap<String, ArrayList<FishMonsters>>();
		m_database.put(monster, fishes);
	}
	
	/**
	 * Removes an entry from the database
	 * @param monster
	 */
	public void deleteEntry(String monster) {
		if(m_database == null) {
			m_database = new HashMap<String, ArrayList<FishMonsters>>();
			return;
		}
		m_database.remove(monster);
	}
	
	public FishMonsters getFish(String monster) {
		monster = monster.toUpperCase();
		return m_database.get(monster).get(0);
		}
	
	/**
	 * Reinitialises the database
	 */
	public void reinitialise() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				m_database = new HashMap<String, ArrayList<FishMonsters>>();
				try {
					/* Read all the data from the text file */
					Scanner s = new Scanner(new File("./res/fishing.txt"));
					String monster = "";
					ArrayList<FishMonsters> fishies = new ArrayList<FishMonsters>();
					while(s.hasNextLine()) {
						monster = s.nextLine();
						fishies = new ArrayList<FishMonsters>();
						/* Parse the data in the order EXPERIENCE, LEVELREQ, RODREQ*/
						StringTokenizer st = new StringTokenizer(monster);
						String monsterName = st.nextToken().toUpperCase();
						while(st.hasMoreTokens()) {
							int levelreq = Integer.parseInt(st.nextToken());
							int exp = Integer.parseInt(st.nextToken());
							int rodreq = Integer.parseInt(st.nextToken());
							FishMonsters d = new FishMonsters(exp, levelreq, rodreq);
							fishies.add(d);
						}
						addEntry(monsterName, fishies);
					}
					s.close();
					System.out.println("INFO: Fishing database reinitialised");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
}
