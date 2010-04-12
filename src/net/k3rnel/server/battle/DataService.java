package net.k3rnel.server.battle;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import net.k3rnel.server.backend.item.DropData;
import net.k3rnel.server.battle.Monster.ExpTypes;
import net.k3rnel.server.battle.MonsterEvolution.EvolutionTypes;
import net.k3rnel.server.battle.mechanics.JewelMechanics;
import net.k3rnel.server.battle.mechanics.MonsterType;
import net.k3rnel.server.battle.mechanics.moves.MoveList;
import net.k3rnel.server.battle.mechanics.moves.MoveSetData;
import net.k3rnel.server.feature.FishDatabase;

import org.ini4j.Ini;
import org.ini4j.Ini.Section;
import org.simpleframework.xml.core.Persister;

/**
 * Provides a data service for accessing various databases used by the server
 * @author shadowkanji
 *
 */
public class DataService {
	private MonsterSpeciesData m_speciesData;
	private static JewelMechanics m_mechanics;
	private static MoveList m_moveList;
	private static MoveSetData m_moveSetData;
	private static FishDatabase m_fishingData;
	private static ArrayList<String> m_nonTrades;
	
	/**
	 * Default constructor. Loads data immediately.
	 */
	public DataService() {
		try {
			Persister stream = new Persister();
			/*
			 * Load all of shoddy's databases
			 */
			m_moveList = new MoveList(true);
			m_moveSetData = new MoveSetData();
			m_speciesData = new MonsterSpeciesData();
			m_mechanics = new JewelMechanics(5);
			m_fishingData = new FishDatabase();
			m_fishingData.reinitialise();
			JewelMechanics.loadMoveTypes("res/movetypes.txt");
			File f = new File(".");
			m_moveSetData = stream.read(MoveSetData.class, new File(f.getCanonicalPath() + "/res/movesets.xml"));
			initialiseSpecies();
			MonsterSpecies.setDefaultData(m_speciesData);
			System.out.println("INFO: Monsters Databases loaded.");
			/*
			 * List of non-tradeable Monsters
			 * TODO: Load list from a txt file... :-)
			 */
			m_nonTrades = new ArrayList<String>();
			m_nonTrades.add("MonsterMon");
			System.out.println("INFO: Trade Block List established.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns true if the pokemon is tradeable
	 * @param pokemon
	 * @return
	 */
	public static boolean canTrade(String pokemon) {
		for(int i = 0; i < m_nonTrades.size(); i++) {
			if(m_nonTrades.get(i).equalsIgnoreCase(pokemon))
				return false;
		}
		return true;
	}
	
	/**
	 * Initializes the species database
	 */
	public void initialiseSpecies() {
		/* Load shoddy database */
		try {
			m_speciesData.loadSpeciesDatabase(new File("./res/dpspecies.db"));
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		Ini ini = null;
		/* Load updated Monster db */
		try {
			ini = new Ini(new FileInputStream("./res/monsters.ini"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		for (int i = 0; i < 493; i++) { //TODO: Change to iterate to all, no hardcoding!
			Ini.Section s = ini.get(String.valueOf(i+1));
            MonsterSpecies species = null;
            String name = s.get("InternalName");
         	name = s.get("Name");
         	species = m_speciesData.getMonsterByName(name);
         	initialiseMonster(species, s);
            
		}
		/* Load TM info */
		try {
			ini = new Ini(new FileInputStream("./res/tms.ini"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Iterator<String> iterator = ini.keySet().iterator();
		while(iterator.hasNext()) {
			String tm = (String) iterator.next();
			Ini.Section s = ini.get(tm);
			String [] monsters = s.get("MONSTER").split(",");
			for(int i = 0; i < monsters.length; i++) {
				MonsterSpecies species = m_speciesData.getMonsterByName(monsters[i]);
				if(species != null) {
					for(int j = 0; j < species.getTMMoves().length; j++) {
						if(species.getTMMoves()[j] == null) {
							species.getTMMoves()[j] = tm;
							break;
						}
					}
				}
			}
		}
		/* We originally gave 92 possible TMs for every Monster, lets trim that down */
		for(int i = 0; i < m_speciesData.getSpeciesCount(); i++) {
			int a = 0;
			MonsterSpecies s = m_speciesData.getSpecies()[i];
			for(int j = 0; j < s.getTMMoves().length; j++) {
				if(s.getTMMoves()[j] != null) {
					a++;
				} else {
					break;
				}
			}
			String [] newTMList = new String[a];
			for(int j = 0; j < newTMList.length; j++) {
				if(s.getTMMoves()[j] != null) {
					newTMList[j] = s.getTMMoves()[j];
				}
			}
			s.setTMMoves(newTMList);
		}
		/* Load Drop Data */
		try {
			Scanner s = new Scanner(new File("./res/itemdrops.txt"));
			String monster = "";
			while(s.hasNextLine()) {
				monster = s.nextLine();
				DropData [] drops = new DropData[10];
				/* Parse the data in the form ITEM, PROBABILITY */
				StringTokenizer st = new StringTokenizer(monster);
				String pokeName = st.nextToken().toUpperCase();
				int dp = 0;
				while(st.hasMoreTokens()) {
					int item = Integer.parseInt(st.nextToken());
					int p = Integer.parseInt(st.nextToken());
					DropData d = new DropData(item, p);
					if(dp < drops.length) {
						drops[dp] = d;
						dp++;
					}
				}
				m_speciesData.getMonsterByName(pokeName).setDropData(drops);
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Initialises a monster from an ini section
	 * @param species
	 * @param s
	 */
	private void initialiseMonster(MonsterSpecies species, Section s) {
		if(species != null) {
			species.setInternalName(s.get("InternalName"));
            species.setKind(s.get("Kind"));
            species.setMonsterDexInfo(s.get("Monsterdex"));
            species.setType1(s.get("Type1"));
            species.setType2(s.get("Type2"));
            if (species.getType2() == null)
                species.setType2("");
            MonsterType [] types;
            if(species.getType2() != null && !species.getType2().equalsIgnoreCase("")) {
            	types = new MonsterType[2];
            	types[0] = MonsterType.getType(species.getType1().toUpperCase());
            	types[1] = MonsterType.getType(species.getType2().toUpperCase());
            } else {
            	types = new MonsterType[1];
                types[0] = MonsterType.getType(species.getType1().toUpperCase());
            }
            species.setType(types);

            String[] stringBaseStats = s.get("BaseStats").split(",");
            for (int j = 0; j < stringBaseStats.length; j++)
                    species.getBaseStats()[j] = 
                            Integer.parseInt(stringBaseStats[j]);
            species.setRareness(Integer.parseInt(s.get("Rareness")));
            species.setBaseEXP(Integer.parseInt(s.get("BaseEXP")));
            species.setHappiness(Integer.parseInt(s.get("Happiness")));
            species.setGrowthRate(ExpTypes.valueOf(s.get("GrowthRate").toUpperCase()));
            species.setStepsToHatch(Integer.parseInt(s.get("StepsToHatch")));
            species.setColor(s.get("Color"));
            species.setHabitat(s.get("Habitat"));
            if (species.getHabitat() == null)
                    species.setHabitat("");
            String[] stringEffortPoints = s.get("EffortPoints").split(",");
            for (int j = 0; j < stringEffortPoints.length; j++)
                    species.getEffortPoints()[j] = 
                            Integer.parseInt(stringEffortPoints[j]);
            String [] abilities = new String[2];
            if(s.get("Abilities").contains(",")) {
                String [] temp = s.get("Abilities").split(",");
                abilities[0] = temp[0].trim();
                abilities[1] = temp[1].trim();
            } else {
            	abilities = new String[1];
            	abilities[0] = s.get("Abilities").trim();
            }
            species.setAbilities(abilities);
            String[] stringCompatibility = s.get("Compatibility").split(",");
            for (int j = 0; j < stringCompatibility.length; j++)
                    species.getCompatibility()[j] = 
                            Integer.parseInt(stringCompatibility[j]);
            species.setHeight(Float.parseFloat(s.get("Height")));
            species.setWeight(Float.parseFloat(s.get("Weight")));
            String gender = s.get("GenderRate");
            if(gender.equalsIgnoreCase("Female50Percent")) {
            	 species.setFemalePercentage(50);
            	 species.setGenders(MonsterSpecies.GENDER_BOTH);
            } else if(gender.equalsIgnoreCase("Female75Percent")) {
            	 species.setFemalePercentage(75);
            	 species.setGenders(MonsterSpecies.GENDER_BOTH);
            } else if(gender.equalsIgnoreCase("Genderless")) {
            	 species.setFemalePercentage(-1);
            	 species.setGenders(MonsterSpecies.GENDER_NONE);
            } else if(gender.equalsIgnoreCase("AlwaysMale")) {
            	 species.setFemalePercentage(0);
            	 species.setGenders(MonsterSpecies.GENDER_MALE);
            } else if(gender.equalsIgnoreCase("AlwaysFemale")) {
            	 species.setFemalePercentage(100);
            	 species.setGenders(MonsterSpecies.GENDER_FEMALE);
            } else if(gender.equalsIgnoreCase("Female25Percent")) {
            	 species.setFemalePercentage(25);
            	 species.setGenders(MonsterSpecies.GENDER_BOTH);
            } else {
            	/* Female one eighth */
            	 species.setFemalePercentage(12);
            	 species.setGenders(MonsterSpecies.GENDER_BOTH);
            }
            String[] stringMoves = s.get("Moves").split(",");
            species.setLevelMoves(new HashMap<Integer, String>());
            String [] startMoves = new String [4];
            int sp = 0;
            for (int j = 0; j < stringMoves.length; j++) {
                    if (j % 2 == 0) {
                    	int level = Integer.parseInt(stringMoves[j]);
                    	String move = stringMoves[j + 1].charAt(0) + 
                    		stringMoves[j + 1].substring(1).toLowerCase();
                    	if(move.contains(" ")) {
                    		//Capitalise words correctly
                    		String tmp = "";
                    		for(int i = 1; i <= move.length(); i++) {
                    			if(i < move.length() && move.substring(i - 1, i).compareTo(" ") == 0) {
                    				tmp = tmp + " " + move.substring(i, i + 1).toUpperCase();
                    				i++;
                    			} else {
                    				tmp = tmp + move.charAt(i - 1);
                    			}
                    		}
                    		move = tmp;
                    	}
                    	if(level < 2) {
                    		if(sp <= 3) {
	                    		startMoves[sp] = move;
	                    		sp++;
                    		}
                    	} else {
                    		species.getLevelMoves().put(level, move);
                    	}
                    }
            }
            species.setStarterMoves(startMoves);
            species.setEggMoves(s.get("EggMoves").split(","));
            String[] stringEvolutions = s.get("Evolutions").split(",");
            
           	MonsterEvolution [] evos = new MonsterEvolution[(int) Math.ceil(stringEvolutions.length / 3.0)];
           	int ep = 0;
            for (int j = 0; j < stringEvolutions.length; j = j + 3) {
            	MonsterEvolution evo = new MonsterEvolution();
            	if(stringEvolutions[j] != null && !stringEvolutions[j].equalsIgnoreCase("")) {
                	evo.setEvolveTo(stringEvolutions[j]);
                	evo.setType(EvolutionTypes.valueOf(stringEvolutions[j + 1]));
                	if(evo.getType() == EvolutionTypes.Level)
                		evo.setLevel(Integer.parseInt(stringEvolutions[j + 2]));
                	else if(evo.getType() != EvolutionTypes.Happiness &&
                			evo.getType() != EvolutionTypes.HappinessDay &&
                			evo.getType() != EvolutionTypes.HappinessNight &&
                			evo.getType() != EvolutionTypes.Trade)
                		evo.setAttribute(stringEvolutions[j + 2]);
                	if(ep < evos.length) {
                		evos[ep] = evo;
                	}
            	}
            }
            species.setEvolutions(evos);
            species.setTMMoves(new String [92]);
            
		}
	}
	
	/**
	 * Returns the fish database
	 * @return
	 */
	public static FishDatabase getFishDatabase() {
		return m_fishingData;
	}
	
	/**
	 * Returns shoddybattle battle mechanics
	 * @return
	 */
	public static JewelMechanics getBattleMechanics() {
		return m_mechanics;
	}
	
	/**
	 * Returns the move list
	 * @return
	 */
	public static MoveList getMovesList() {
		return m_moveList;
	}
	
	/**
	 * Returns move set data
	 * @return
	 */
	public static MoveSetData getMoveSetData() {
		return m_moveSetData;
	}
}
