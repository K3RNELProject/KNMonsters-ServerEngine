package net.k3rnel.server.feature;

import java.util.ArrayList;
import java.util.Random;

import net.k3rnel.server.battle.DataService;
import net.k3rnel.server.battle.Monster;
import net.k3rnel.server.battle.MonsterEgg;
import net.k3rnel.server.battle.MonsterSpecies;
import net.k3rnel.server.battle.mechanics.MonsterNature;
import net.k3rnel.server.battle.mechanics.moves.MoveList;
import net.k3rnel.server.battle.mechanics.moves.MoveListEntry;

/**
 * 
 * @author ZombieBear
 * 
 */
public class BreedingLogic {
	private Monster maleMonster;
	private Monster femaleMonster;

	/**
	 * Constructor
	 * @param mons1
	 * @param mons2
	 * @return
	 * @throws Exception
	 */
	public MonsterEgg generateEgg(Monster mons1, Monster mons2) throws Exception{
		Monster mon = null;
		if (canBreed(mons1, mons2)) {
			try{
				mon = generateHatchling(generateEggSpecies());
				return new MonsterEgg(mon, 200);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("BreedingException: Egg generation issue");
			}
		} else {
			throw new Exception("BreedingException: The given monster can't breed");
		}
	}

	/**
	 * Returns true if the given monster are able to breed.
	 * @param mons1
	 * @param moons2
	 * @return
	 */
	public boolean canBreed(Monster mons1, Monster moons2) {
		for (int i : MonsterSpecies.getDefaultData().getMonsterByName(
				mons1.getName()).getCompatibility()) {
			for (int x : MonsterSpecies.getDefaultData().getMonsterByName(
					moons2.getName()).getCompatibility()) {
				if (i == x) {
					if (mons1.getGender() == Monster.GENDER_MALE
							&& (moons2.getGender() == Monster.GENDER_FEMALE)
							|| moons2.getSpeciesName() == "Ditto") {
						maleMonster = mons1;
						femaleMonster = moons2;
						return true;
					} else if (moons2.getGender() == Monster.GENDER_MALE
							&& (mons1.getGender() == Monster.GENDER_FEMALE || mons1
									.getSpeciesName() == "Ditto")) {
						maleMonster = moons2;
						femaleMonster = mons1;
						return true;
					} else if (mons1.getGender() == Monster.GENDER_NONE
							&& moons2.getSpeciesName() == "Ditto") {
						maleMonster = mons1;
						femaleMonster = moons2;
						return true;
					}
				}
			}
		}
		return false;
	}

	
	/**
	 * Generates the new egg's species based on the parents
	 * @return the species number
	 */
	private int generateEggSpecies() {
		// TODO: Add code for incenses!
		// If the female monster is a ditto, species is set by the male parent
		if (femaleMonster.getSpeciesName() != "Ditto") {
			// Nidoran species
			if (femaleMonster.getSpeciesName() == "NidoranF"
					|| femaleMonster.getSpeciesName() == "Nidorina"
					|| femaleMonster.getSpeciesName() == "Nidoqueen") {
				if (DataService.getBattleMechanics().getRandom().nextInt(2) == 0)
					return MonsterSpecies.getDefaultData().getMonsterByName("NidoranM").getSpeciesNumber();
				else
					return MonsterSpecies.getDefaultData().getMonsterByName("NidoranF").getSpeciesNumber();
			}
			// Volbeat and Illumise
			else if (femaleMonster.getSpeciesName() == "Illumise") {
				if (DataService.getBattleMechanics().getRandom().nextInt(2) == 0)
					return MonsterSpecies.getDefaultData().getMonsterByName("Illumise").getSpeciesNumber();
				else
					return MonsterSpecies.getDefaultData().getMonsterByName("Volbeat").getSpeciesNumber();
			}
			// Normal case
			else
				return femaleMonster.getSpeciesNumber();
		}
		return maleMonster.getSpeciesNumber();
	}

	/**
	 * Generates the baby monster's moves
	 * @param species
	 * @return
	 */
	public MoveListEntry[] getBabyMoves(int species) {
		MoveListEntry[] moves = new MoveListEntry[4];
		MoveList moveList = MoveList.getDefaultData();
		ArrayList<MoveListEntry> possibleMoves = new ArrayList<MoveListEntry>();
		MonsterSpecies s = MonsterSpecies.getDefaultData().getSpecies(species);
		// List of moves by level 5
		for (int i = 1; i <= 5; i++) {
			if (s.getLevelMoves().containsKey(i)) {
				possibleMoves.add(moveList.getMove(s.getLevelMoves().get(i)));
			}
		}

		int moveNum = possibleMoves.size();
		if (possibleMoves.size() <= 4) {
			for (int i = 0; i < possibleMoves.size(); i++) {
				moves[i] = possibleMoves.get(i);
			}
		} else {
			for (int i = 0; i < moves.length; i++) {
				if (possibleMoves.size() == 0)
					moves[i] = null;
				moves[i] = possibleMoves.get(moveNum);
				moveNum--;
				if (moveNum == 0)
					break;
			}
		}

		// Moves that both parents know

		// List of egg moves
		possibleMoves.clear();
		for (int i = 0; i < s.getEggMoves().length; i++) {
			for (int x = 0; i < 4; i++) {
				if (maleMonster.getMove(x) == moveList.getMove(s.getEggMoves()[i])) {
					possibleMoves.add(moveList.getMove(s.getEggMoves()[i]));
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			if (moves[i] == null && possibleMoves.size() < i) {
				moves[i] = possibleMoves.get(i);
			}
		}

		return moves;
	}

	/**
	 * Generates the baby monster to hatch from the egg.
	 * @param species
	 * @return
	 * @throws Exception
	 */
	private Monster generateHatchling(int species) throws Exception{
		Monster hatchling;
		try{
		MonsterSpecies speciesData = MonsterSpecies.getDefaultData()
				.getSpecies(species);
		Random random = DataService.getBattleMechanics().getRandom();

		// get Nature if female or ditto is holding an everstone, 50% chance
		String nature = "";
		if (femaleMonster.getItemName() == "Everstone") {
			if (random.nextInt(2) == 0) {
				nature = femaleMonster.getNature().getName();
			}
		} else
			nature = MonsterNature.getNature(
					random.nextInt(MonsterNature.getNatureNames().length))
					.getName();

		int natureIndex = 0;
		for (String name : MonsterNature.getNatureNames()) {
			if (name == nature) {
				break;
			}
			natureIndex++;
		}

		// Get 3 random IVS from parents
		int[] ivs = new int[6];
		for (int iv : ivs) {
			ivs[iv] = speciesData.getBaseStats()[iv];
		}

		int[] attempt = new int[3];
		for (int i = 0; i < 3; i++) {
			int randomNum = DataService.getBattleMechanics().getRandom()
					.nextInt(2);
			attempt[i] = randomNum;
			if (i == 2) {
				if (attempt[0] == 0 && attempt[1] == 0) {
					randomNum = 1;
				} else if (attempt[0] == 1 && attempt[1] == 1) {
					randomNum = 0;
				}
			}
			int iv = DataService.getBattleMechanics().getRandom().nextInt(6);
			if (randomNum == 0) {
				ivs[iv] = maleMonster.getBaseStats()[iv];
			} else {
				ivs[iv] = femaleMonster.getBaseStats()[iv];
			}
		}

		hatchling = new Monster(DataService.getBattleMechanics(), 
				MonsterSpecies.getDefaultData().getSpecies(species),
				MonsterNature.getNature(natureIndex),
				speciesData.getPossibleAbilities(MonsterSpecies.getDefaultData())[random
						.nextInt(speciesData.getPossibleAbilities(
								MonsterSpecies.getDefaultData()).length)], "", Monster
						.generateGender(speciesData.getPossibleGenders()), 5,
				ivs, new int[6], getBabyMoves(species), new int[4]);
		} catch (Exception e) {
			throw new Exception("BreedingException: Hatchling generation issue");
		}
		return hatchling;
	}
}