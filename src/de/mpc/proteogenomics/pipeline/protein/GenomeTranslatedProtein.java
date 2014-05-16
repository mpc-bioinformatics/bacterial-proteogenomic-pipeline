package de.mpc.proteogenomics.pipeline.protein;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


public class GenomeTranslatedProtein extends AbstractProtein {
	
	private final static Logger logger =
			Logger.getLogger(GenomeTranslatedProtein.class);
	
	/** the genome name of the protein */
	private String genomeName;
	
	/** the sequence of the protein */
	private StringBuffer sequence;
	
	/** the start position in the genome */
	private Long start;
	
	/** the end position in the genome */
	private Long end;
	
	/** the frame (1,2,3 for forward, 4,5,6 for backward strand) */
	private Integer frame;
	
	/** whether this protein is an ORF protein, i.e. it starts with a start codon (which is translated as M) */
	private Boolean isORFprotein;
	
	/** a list of related (overlapped, elongated...) proteins */
	private Map<Integer, List<AbstractProtein>> relatedProteins;
	
	
	/** the prefix for the accession */
	public static final String genome_translation_accession_prefix = "genometranslation_";
	
	/** the pattern to grep the whole accession */
	public final static String grepAccessionPattern = ">?(" +
			genome_translation_accession_prefix +
			"(?:|ORF_)(?:|-)[0-9]+-[^-]+-(?:direct|complement)-[a-zA-Z0-9.:^*$@!+_?-|]+)" +
			AbstractProtein.headerSeparator.replace("|", "\\|") + ".*$";
	
	/** the pattern to grep the description */
	public final static String grepDescriptionPattern = ">?" +
			genome_translation_accession_prefix +
			"(?:|ORF_)(?:|-)[0-9]+-[^-]+-(?:direct|complement)-[a-zA-Z0-9.:^*$@!+_?-|]+" +
			AbstractProtein.headerSeparator.replace("|", "\\|") + "(.*)$";
	
	
	// possible pseudo protein states compared to known proteins
	public static final Integer NOT_IN_KNOWN                = 0;	// no overlap between known protein
	public static final Integer IN_KNOWN                    = 1;	// exactly the same start and end position as known protein
	public static final Integer ELONGATION_OF_KNOWN         = 2;	// start or end is equal to known protein, but the protein is longer
	public static final Integer PARTIAL_ELONGATION_OF_KNOWN = 3;	// the protein overlaps the known protein and elongates in one direction
	public static final Integer PART_OF_KNOWN               = 4;	// the protein is part of another protein

	
	/**
	 * Simple constructor, frame is set to the given value and start to null,
	 * the sequence is initialised.
	 * @param start
	 * @param frame
	 */
	public GenomeTranslatedProtein(String genomeName, Long start,
			Integer frame) {
		this.genomeName = genomeName;
		this.sequence = new StringBuffer();
		this.start = start;
		this.end = null;
		this.frame = frame;
		this.isORFprotein = false;
		this.relatedProteins = null;
	}
	
	
	/**
	 * Appends the given amino acid to the sequence
	 * @param aminoAcid
	 */
	public void appendAminoAcid(Character aminoAcid) {
		sequence.append(aminoAcid);
	}
	
	
	/**
	 * Reverses the sequence (but leaves the start and stop position as they
	 * are), used for frames 3,4,5
	 */
	public void reverseProtein() {
		sequence.reverse();
	}
	
	
	/**
	 * Returns the amino acid sequence
	 * @return
	 */
	public String getSequence() {
		return sequence.toString();
	}
	
	
	/**
	 * Returns the start position of the protein
	 * @return
	 */
	public Long getStart() {
		return start;
	}
	
	
	/**
	 * Calculates the start position and frame with potentially negative start
	 * from the correctly set end position and the length of the protein.
	 */
	public void recalculateStartForOverlap() {
		if (frame < 3) {
			start = end - 3 * sequence.length() - 3;
			frame = end.intValue() % 3;
		} else {
			start = end - 3 * sequence.length() - 2;
			frame = end.intValue() % 3 + 3;
		}
	}
	
	
	/**
	 * Sets the end of the protein
	 * @return
	 */
	public void setEnd(Long end) {
		this.end = end;
	}
	
	
	/**
	 * Returns the end position of the protein
	 * @return
	 */
	public Long getEnd() {
		return end;
	}
	
	
	/**
	 * Returns the frame of the protein
	 * @return
	 */
	public Integer getFrame() {
		return frame;
	}
	
	
	public Boolean getIsComplement() {
		return (frame >= 3);
	}
	
	
	/**
	 * Returns the length of the sequence.
	 * @return
	 */
	public Integer length() {
		return sequence.length();
	}
	
	
	/**
	 * Returns the accession, build of the start, end frame and
	 * whether it is an ORF.
	 * @return
	 */
	public String getAccession() {
		StringBuilder accession =
				new StringBuilder(genome_translation_accession_prefix);
		
		if (isORFprotein) {
			accession.append("ORF_");
		}
		
		accession.append((end > start) ? start : end);
		accession.append("-");
		accession.append((end > start) ? end : start);
		accession.append("-");
		
		if (frame < 3) {
			accession.append("direct");
		} else {
			accession.append("complement");
		}
		
		accession.append("-");
		try {
			accession.append(
					URLEncoder.encode(genomeName, "UTF-8").replace("+", "%20"));
		} catch (UnsupportedEncodingException e) {
			logger.warn("trouble encoding genome name, using unencoded " +
					genomeName,  e);
			accession.append(genomeName);
		}
		
		return accession.toString();
	}
	
	
	/**
	 * Returns the description, consisting of short description and possibly the equal
	 * proteins.
	 * 
	 * @return
	 */
	public String getDescription() {
		StringBuilder description = new StringBuilder("translation protein of " +
				genomeName + " frame " + frame + ", " + start + "-" + end);
		
		if (isORFprotein) {
			description.append(", ORF protein");
		}
		
		// as the relations may span multiple position hashes, put them into a set 
		Set<String> relationTexts = new HashSet<String>();
		if (relatedProteins != null) {
			if (relatedProteins.containsKey(IN_KNOWN)) {
				for (AbstractProtein protein : relatedProteins.get(IN_KNOWN)) {
					relationTexts.add(", equal to " + protein.getAccession());
				}
			}
			
			if (relatedProteins.containsKey(ELONGATION_OF_KNOWN)) {
				for (AbstractProtein protein
						: relatedProteins.get(ELONGATION_OF_KNOWN)) {
					relationTexts.add(", elongates " + protein.getAccession());
				}
			}
			
			if (relatedProteins.containsKey(PARTIAL_ELONGATION_OF_KNOWN)) {
				for (AbstractProtein protein
						: relatedProteins.get(PARTIAL_ELONGATION_OF_KNOWN)) {
					relationTexts.add(", partial elongates " +
							protein.getAccession());
				}
			}
			
			if (relatedProteins.containsKey(PART_OF_KNOWN)) {
				for (AbstractProtein protein
						: relatedProteins.get(PART_OF_KNOWN)) {
					relationTexts.add(", part of " + protein.getAccession());
				}
			}
		}
		
		for (String relationText : relationTexts) {
			description.append(relationText);
		}
		
		return description.toString();
	}
	
	
	@Override
	public String getGenomeName() {
		return genomeName;
	}
	
	
	/**
	 * Sets, whether this protein is an ORF protein
	 * @return
	 */
	public void setIsORFProtein(Boolean isORF) {
		this.isORFprotein = isORF;
	}
	
	
	/**
	 * Returns whether this protein is an ORF protein, i.e. it starts with a
	 * start codon (which is translated as M).
	 * 
	 * @return
	 */
	public Boolean getIsORFProtein() {
		return isORFprotein;
	}
	
	
	/**
	 * Checks whether this protein has any entry in the map of related proteins
	 * for IN_KNOWN.
	 * 
	 * @return
	 */
	public Boolean isInKnownProtein() {
		if (relatedProteins == null) {
			return false;
		} else {
			return relatedProteins.containsKey(IN_KNOWN);
		}
	}
	
	
	/**
	 * Checks, whether the protein is in the hash map of the given known
	 * proteins or has any overlapping-relations to a protein in it.
	 * 
	 * @param prot
	 * @param knownProteins
	 * @param hashWidth
	 * @return
	 */
	public void checkProteinRelations(
			Map<Long, List<GenericProtein>> knownProteins, Long hashWidth) {
		Long startHash = start / hashWidth;
		Long endHash = end / hashWidth;
		
		for (Long h=startHash; h <= endHash; h++) {
			List<GenericProtein> protList = knownProteins.get(h);
			if (protList != null) {
				for (GenericProtein protein : protList) {
					if (genomeName.equals(protein.getGenomeName()) &&
							(Math.max(start, protein.getStart()) < Math.min(end, protein.getEnd())) && // overlap
							getIsComplement().equals(protein.getIsComplement()) &&	// same direction
							(start % 3 == protein.getStart() % 3)) {	// same frame
						
						if (start.equals(protein.getStart()) && 
								end.equals(protein.getEnd())) {
							// exact match of start and end position
							addRelatedProtein(IN_KNOWN, protein);
						} else if ((start <= protein.getStart()) &&
								(end >= protein.getEnd())) {
							// the protein is an elongation of a known
							addRelatedProtein(ELONGATION_OF_KNOWN, protein);
						} else if (((start > protein.getStart()) && (end > protein.getEnd())) ||
								((start < protein.getStart()) && (end < protein.getEnd()))) {
							// the protein overlaps one end of a known protein
							addRelatedProtein(PARTIAL_ELONGATION_OF_KNOWN,
									protein);
						} else if ((start >= protein.getStart()) &&
								(end <= protein.getEnd())) {
							// the protein is part of a known protein
							addRelatedProtein(PART_OF_KNOWN, protein);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Adds the protein to the relations map with the given relation.
	 * 
	 * @param relation
	 * @param protein
	 */
	private void addRelatedProtein(Integer relation, AbstractProtein protein) {
		if (relatedProteins == null) {
			relatedProteins = new HashMap<Integer, List<AbstractProtein>>();
		}
		
		List<AbstractProtein> proteinList = relatedProteins.get(relation);
		if (proteinList == null) {
			proteinList = new ArrayList<AbstractProtein>();
			relatedProteins.put(relation, proteinList);
		}
		
		proteinList.add(protein);
	}
}