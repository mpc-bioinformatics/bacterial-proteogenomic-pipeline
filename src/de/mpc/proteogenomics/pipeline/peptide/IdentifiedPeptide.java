package de.mpc.proteogenomics.pipeline.peptide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.mpc.proteogenomics.pipeline.protein.AbstractProtein;


public class IdentifiedPeptide {
	
	/** the sequence of the peptide */
	private String sequence;
	
	/** mapping from the accessions to the proteins this peptide belongs to */
	private Map<String, AbstractProtein> proteins;
	
	/** the start and stop positions for each protein (genome positions) */
	private Map<String, Map<Long, Long>> proteinPositions;
	
	/** whether only direct genome translations are associated with this peptide */
	private boolean onlyGenomeTranslation;
	
	/** map from the identification/mzTab file to the psmIDs (the different identifications)*/
	private Map<String, Set<String>> filesIdentifications;
	
	
	
	/**
	 * Constructor
	 * @param sequence
	 */
	public IdentifiedPeptide(String sequence) {
		this.sequence = sequence;
		proteins = new HashMap<String, AbstractProtein>();
		onlyGenomeTranslation = true;
		filesIdentifications = new HashMap<String, Set<String>>();
		proteinPositions = new HashMap<String, Map<Long, Long>>();
	}
	
	
	public String getSequence() {
		return sequence;
	}
	
	
	/**
	 * Returns true, if only genome translated proteins are connected to this
	 * peptide.
	 * 
	 * @return
	 */
	public boolean getHasOnlyGenomeTranslations() {
		return onlyGenomeTranslation;
	}
	
	
	/**
	 * Returns true, if the peptide has a protein, which has "elongates" in its
	 * description.
	 * 
	 * @return
	 */
	public boolean getIsElongation() {
		for (AbstractProtein protein : proteins.values()) {
			if (protein.getDescription().contains(" elongates ")) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Puts the given protein in the list, if no protein with the same accession
	 * is already in the list.
	 *  
	 * @param protein
	 * @return null, if no protein with same accession is in the list, else the
	 * protein already in the list
	 */
	private AbstractProtein addProtein(AbstractProtein protein) {
		// check, whether the protein is already in the list
		if (proteins.containsKey(protein.getAccession())) {
			return proteins.get(protein.getAccession());
		}
		
		proteins.put(protein.getAccession(), protein);
		proteinPositions.put(protein.getAccession(), new HashMap<Long, Long>());
		return null;
	}
	
	
	public List<AbstractProtein> getProteins() {
		return new ArrayList<AbstractProtein>(proteins.values());
	}
	
	
	/**
	 * Returns a mapping from the start-end positions (in a Long array) to the
	 * proteins.
	 * 
	 * @return
	 */
	public Map<Long[], List<AbstractProtein>> getPositionsToProteins() {
		
		Map<Long[], List<AbstractProtein>> posToProteins =
				new HashMap<Long[], List<AbstractProtein>>();
		
		for (Map.Entry<String, Map<Long, Long>> protIt
				: proteinPositions.entrySet()) {
			
			for (Map.Entry<Long, Long> posIt : protIt.getValue().entrySet()) {
				Long[] pos = new Long[2];
				pos[0] = posIt.getKey();
				pos[1] = posIt.getValue();
				
				List<AbstractProtein> proteinList = posToProteins.get(pos);
				if (proteinList == null) {
					proteinList = new ArrayList<AbstractProtein>();
					posToProteins.put(pos, proteinList);
				}
				
				proteinList.add(proteins.get(protIt.getKey()));
			}
		}
		
		return posToProteins;
	}
	
	
	/**
	 * Adds the PSM with the given ID to the PSMs and increases the
	 * identifications  counter, if this PSM was not already in this peptide.
	 * Also adds the protein, if it was not yet in the peptide, and the position
	 * in the protein. The identifications can be divided into different groups.
	 * 
	 * @param identificationFile
	 * @param psmID
	 * @param protein 
	 * @param isGenomeTranslation whether the added PSM belongs to a genome
	 * translated protein
	 * @param groupName
	 */
	public void addPSM(String identificationFile, String psmID,
			AbstractProtein protein, Long start, Long end,
			boolean isGenomeTranslation, String groupName) {
		Set<String> psmIDs = filesIdentifications.get(identificationFile);
		if (psmIDs == null) {
			psmIDs = new HashSet<String>();
			filesIdentifications.put(identificationFile, psmIDs);
		}
		
		psmIDs.add(psmID);
		
		addProtein(protein);
		onlyGenomeTranslation &= isGenomeTranslation;
		
		if (!proteinPositions.get(protein.getAccession()).containsKey(start)) {
			proteinPositions.get(protein.getAccession()).put(start, end);
		}
		
		if (protein.getDescription().contains("part of")) {
			onlyGenomeTranslation &= false;
		}
	}
	
	
	/**
	 * returns the number of identifications for the given file.
	 * 
	 * @param groupName
	 * @return
	 */
	public int getNrIdentifiedSpectra(String fileName) {
		Set<String> psmIDs = filesIdentifications.get(fileName);
		if (psmIDs != null) {
			return psmIDs.size();
		} else {
			return 0;
		}
	}
	
	
	/**
	 * Returns the number of all identifications
	 * 
	 * @return
	 */
	public int getNrAllIdentifications() {
		int count = 0;
		for (Set<String> psmIDs : filesIdentifications.values()) {
			count += psmIDs.size();
		}
		
		return count;
	}
	
	
	/**
	 * Returns a map mapping from the identification file name to the set of
	 * PSM IDs.
	 * 
	 * @return
	 */
	public Map<String, Set<String>> getAllIdentifications() {
		return filesIdentifications;
	}
	
	
	/**
	 * Returns the positions of the peptide in this protein.
	 * 
	 * @param accession
	 * @return
	 */
	public Map<Long, Long> getProteinPositions(String accession) {
		return proteinPositions.get(accession);
	}
}
