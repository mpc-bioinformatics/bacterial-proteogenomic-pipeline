package de.mpc.proteogenomics.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.protein.GenericProtein;


/**
 * This class helps to get further protein information from another FASTA file.
 * <p>
 * To do this, first a GFF and FASTA file containing the target proteins is
 * parsed with {@link #getDataForTargetProteinlist(String, String, String)}. If
 * a mapping file with mappings from the reference to the target proteins is
 * available, this information can be parsed by calling
 * {@link #parseAccessionMapping(String, String, String)}.<br>
 * After this information is parsed, the reference FASTA can be mapped to the
 * targets by calling
 * {@link #mapTargetsToReference(String, String, String, String)}.
 * <p>
 * Finally, the enriched information can be written to GFF
 * ({@link #writeToGFF(String)} and FASTA ({@link #writeToFASTA(String)}.
 * 
 * @author julian
 *
 */
public class CompareAndCombineProteinInformation {
	
	private final static Logger logger =
			Logger.getLogger(CompareAndCombineProteinInformation.class);
	
	
	/** the target proteins (old, unprecise locations...) */
	private Map<String, GenericProtein> targetProteins;
	
	/** maps from the reference accession to the target accession */
	private Map<String, String> accessionsMap;
	
	/** all the mapped proteins */
	private Map<String, GenericProtein> mappedProteins;
	
	/** Proteins, which had no sequence in the FASTA file and therefore  are unmappable */
	private Map<String, GenericProtein> sequencelessProteins;
	
	
	/**
	 * Basic constructor
	 */
	public CompareAndCombineProteinInformation() {
		targetProteins = new HashMap<String, GenericProtein>(1000);
		accessionsMap = new HashMap<String, String>(1000);
		mappedProteins = new HashMap<String, GenericProtein>(1000);
	}
	
	
	/**
	 * Get additional data, actually only sequence information, from a FASTA
	 * file for proteins in a GFF file.
	 * 
	 * @param gffFileName
	 * @param fastaFileName
	 * @param accessionRegex
	 * @return
	 */
	public int getDataForTargetProteinlist(String gffFileName,
			String fastaFileName, String accessionRegex) {
		int parsedProteins = 0;
		
		try {
			GenericProtein.parseProteinsFromGFF(gffFileName, targetProteins,
					logger);
			
			parsedProteins = parseSequencesForProteins(fastaFileName,
					accessionRegex, targetProteins);
		} catch(IOException ex) {
			logger.error("Error while parsing files: ", ex);
		}
		
		// remove proteins without sequences
		Set<String> removeKeys = new HashSet<String>();
		for (Map.Entry<String, GenericProtein> proteinIt
				: targetProteins.entrySet()) {
			
			if ((proteinIt.getValue().getSequence() == null) ||
					(proteinIt.getValue().getSequence().length() < 1)) {
				removeKeys.add(proteinIt.getKey());
			}
			
		}
		
		sequencelessProteins =
				new HashMap<String, GenericProtein>(removeKeys.size());
		for (String key : removeKeys) {
			GenericProtein remProt = null;
			if ((remProt = targetProteins.remove(key)) != null) {
				sequencelessProteins.put(key, remProt);
				parsedProteins--;
			}
		}
		
		return parsedProteins;
	}
	
	
	/**
	 * Tests parsing of the FASTA file with the given regular expression for the
	 * accession.
	 * 
	 * @param fastaFileName path to the FASTA file
	 * @param accessionRegex regulear expression for the accessions
	 * @param nrEntries number of entrues to test
	 * @return a map from the FASTA headers to the retrieved accessions
	 * @throws FileNotFoundException 
	 */
	public static Map<String, String> testSettingsForTargetProteinlist(
			String fastaFileName, String accessionRegex, int nrEntries) {
		Map<String, String> headerToAccessionMap =
				new HashMap<String, String>(nrEntries);
		BufferedReader fastaReader = null;
		
		try {
			fastaReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fastaFileName)));
			
			Pattern accessionPattern = Pattern.compile(accessionRegex);
			Matcher accessionMatcher;
			
			String line;
			while (((line = fastaReader.readLine()) != null) &&
					(headerToAccessionMap.size() < nrEntries)) {
				
				if (line.startsWith(">")) {
					// set the sequence of the prior protein
					accessionMatcher = accessionPattern.matcher(line);
					
					if (accessionMatcher.matches()) {
						headerToAccessionMap.put(line, accessionMatcher.group(1));
					} else {
						headerToAccessionMap.put(line, null);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Error parsing FASTA file.", e);
		} finally {
			if (fastaReader != null) {
				try {
					fastaReader.close();
				} catch (IOException e) {
					logger.error("Error closing FASTA file.", e);
				}
			}
		}
		return headerToAccessionMap;
	}
	
	
	
	/**
	 * Parse the sequences for the proteins in the proteinMap from a FASTA file.
	 * The accessions in the proteinMap and the FASTA file have to be identical.
	 * 
	 * @param fastaFileName protein FASTA file
	 * @param headerSeparator separation 
	 * @param proteinMap
	 * @return
	 * @throws IOException
	 */
	public static int parseSequencesForProteins(String fastaFileName,
			String accessionRegex, Map<String, GenericProtein> proteinMap)
			throws IOException {
		logger.info("Start parsing " + fastaFileName +
				" to get protein sequences.");
		
		BufferedReader fastaReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fastaFileName)));
		
		StringBuilder sequence = null;
		GenericProtein protein = null;
		
		Pattern accessionPattern = Pattern.compile(accessionRegex);
		Matcher accessionMatcher;
		
		String line;
		int nrParsed = 0;
		while ((line = fastaReader.readLine()) != null) {
			if (line.startsWith(">")) {
				// set the sequence of the prior protein
				if ((protein != null) && (sequence != null)) {
					protein.setSequence(sequence.toString());
					nrParsed++;
					
					if ((protein.getEnd() != null) &&
							(protein.getStart() != null) &&
							(sequence.length() != (protein.getEnd() - protein.getStart() - 2) / 3)) {
						logger.warn("Different length in GFF and FASTA for " +
								protein.getAccession() + ": " + 
								(protein.getEnd() - protein.getStart() - 2) / 3 +
								" != " + sequence.length());
					}
				}
				
				protein = null;
				sequence = null;
				
				accessionMatcher = accessionPattern.matcher(line);
				
				if (accessionMatcher.matches()) {
					String accession = accessionMatcher.group(1);
					protein = proteinMap.get(accession);
					sequence = new StringBuilder();
				} else {
					logger.warn("Unparsable accession in " + line);
				}
				
			} else {
				if (sequence != null) {
					sequence.append(line.trim());
				}
			}
		}
		
		// set the sequence of the last protein
		if ((protein != null) && (sequence != null)) {
			protein.setSequence(sequence.toString());
			nrParsed++;
			if (sequence.length() !=
					(protein.getEnd() - protein.getStart() - 2) / 3) {
				logger.warn("Different length in GFF and FASTA for " +
						protein.getAccession() + ": " + 
						(protein.getEnd() - protein.getStart() - 2) / 3 +
						" != " + sequence.length());
			}
		}
		
		fastaReader.close();
		
		logger.info("Parsing of " + fastaFileName + " done, " + nrParsed +
				" proteins parsed");
		return nrParsed;
	}
	
	
	
	/**
	 * Parses mapping information from a file
	 * 
	 * @param fileName the mapping file
	 * @param referenceAccessionRegex regular expression to parse the reference
	 * accession in a line
	 * @param targetAccessionRegex regular expression to parse the target
	 * accession in a line
	 * @return number of parsed lines
	 */
	public int parseAccessionMapping(String fileName,
			String referenceAccessionRegex, String targetAccessionRegex) {
		Pattern referencePattern = Pattern.compile(referenceAccessionRegex);
		Matcher referenceMatcher;
		Pattern targetPattern = Pattern.compile(targetAccessionRegex);
		Matcher targetMatcher;
		Set<String> doubleMapped = new HashSet<String>();
		
		BufferedReader fastaReader = null;
		try {
			fastaReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName)));
			
			String line;
			while ((line = fastaReader.readLine()) != null) {
				referenceMatcher = referencePattern.matcher(line);
				targetMatcher = targetPattern.matcher(line);
				
				if (referenceMatcher.matches() && targetMatcher.matches() &&
						!doubleMapped.contains(referenceMatcher.group(1))) {
					if (accessionsMap.put(referenceMatcher.group(1),
							targetMatcher.group(1)) != null) {
						doubleMapped.add(referenceMatcher.group(1));
						accessionsMap.remove(referenceMatcher.group(1));
						logger.warn(referenceMatcher.group(1) +
								" double mapped, will be ignored because it is "
								+ "probably a concatenation of proteins.");
					}
				}
			}
		} catch (IOException ex) {
			logger.error("Error while parsing mapping file.", ex);
		} finally {
			if (fastaReader != null) {
				try {
					fastaReader.close();
				} catch (IOException ex) {
					logger.error("Error while closing mapping file.", ex);
				}
			}
		}
		
		return accessionsMap.size();
	}
	
	
	/**
	 * Tests the mapping of the target to the reference accessions in the given
	 * file with the given regular expressions.
	 * 
	 * @param fileName mapping file
	 * @param referenceAccessionRegex regular expression of the reference
	 * accession
	 * @param targetAccessionRegex regular expression of the target accession
	 * @param nrEntries maximal number of parsed entries
	 * @return a {@link List} of String[3], the first entry in each array is the
	 * line, the second the reference and the third the target accession
	 */
	public static List<String[]> testAccessionMapping(String fileName,
			String referenceAccessionRegex, String targetAccessionRegex,
			int nrEntries) {
		List<String[]> entries = new ArrayList<String[]>();
		
		Pattern referencePattern = Pattern.compile(referenceAccessionRegex);
		Matcher referenceMatcher;
		Pattern targetPattern = Pattern.compile(targetAccessionRegex);
		Matcher targetMatcher;
		
		BufferedReader fastaReader = null;
		try {
			fastaReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName)));
			
			String line;
			while (((line = fastaReader.readLine()) != null) &&
					(entries.size() < nrEntries)) {
				referenceMatcher = referencePattern.matcher(line);
				targetMatcher = targetPattern.matcher(line);
				
				String[] entry = new String[3];
				entry[0] = line;
				
				if (referenceMatcher.matches()) {
					entry[1] = referenceMatcher.group(1);
				} else {
					entry[1] = "NO MATCH";
				}
				
				if (targetMatcher.matches()) {
					entry[2] = targetMatcher.group(1);
				} else {
					entry[2] = "NO MATCH";
				}
				
				entries.add(entry);
			}
		} catch (IOException ex) {
			logger.error("Error while parsing mapping file.", ex);
		} finally {
			if (fastaReader != null) {
				try {
					fastaReader.close();
				} catch (IOException ex) {
					logger.error("Error while closing mapping file.", ex);
				}
			}
		}
		
		
		return entries;
	}
	
	
	/**
	 * Tries to map all the entries in the referenceFasta to target proteins.
	 * 
	 * @param referenceFasta
	 * @param accessionRegex
	 * @param alternativeMappingRegex
	 * @param descriptionRegex
	 * @return the number of entries in the mapped proteins map 
	 */
	public int mapTargetsToReference(String referenceFasta,
			String accessionRegex, String alternativeMappingRegex,
			String descriptionRegex) {
		Pattern accessionPattern = Pattern.compile(accessionRegex);
		Matcher accessionMatcher;
		Pattern alternativeMappingPattern =
				Pattern.compile(alternativeMappingRegex);
		Matcher alternativeMappingMatcher;
		
		Pattern descriptionPattern =
				Pattern.compile(descriptionRegex);
		Matcher descriptionMatcher;
		
		Map<String, Integer> mapCounts = new HashMap<String, Integer>(5);
		mapCounts.put("equal", 0);
		mapCounts.put("elongation", 0);
		mapCounts.put("mutations", 0);
		mapCounts.put("longTarget", 0);
		mapCounts.put("unmapped", 0);
		
		logger.info("Start parsing " + referenceFasta +
				" to map targets to reference.");
		BufferedReader fastaReader = null;
		try  {
			fastaReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(referenceFasta)));
			
			StringBuilder sequence = null;
			GenericProtein protein = null;
			String accession = null;
			String alternativeAccession = null;
			String description = null;
			
			String line;
			while ((line = fastaReader.readLine()) != null) {
				
				if (line.startsWith(">")) {
					// set the sequence of the prior protein
					if ((accession != null) && (sequence != null)) {
						if (!mapProteinToTargets(sequence.toString(), accession,
								description, protein, mappedProteins, mapCounts)) {
							mapCounts.put("unmapped",
									mapCounts.get("unmapped") + 1);
						}
					}
					
					protein = null;
					sequence = null;
					accession = null;
					description = null;
					alternativeAccession = null;
					
					accessionMatcher = accessionPattern.matcher(line);
					if (accessionMatcher.matches()) {
						accession = accessionMatcher.group(1);
						
						alternativeMappingMatcher =
								alternativeMappingPattern.matcher(line);
						if (alternativeMappingMatcher.matches()) {
							alternativeAccession =
									alternativeMappingMatcher.group(1);
						}
						
						descriptionMatcher = descriptionPattern.matcher(line);
						if (descriptionMatcher.matches()) {
							description = descriptionMatcher.group(1);
						} else {
							description = "";
						}
						
						String mappedAccession = accessionsMap.get(accession);
						
						protein = targetProteins.get(mappedAccession);
						if (protein == null) {
							protein = targetProteins.get(alternativeAccession);
						}
						
						sequence = new StringBuilder(500);
					} else {
						logger.warn("Unparsable accession in " + line);
					}
				} else {
					if (sequence != null) {
						sequence.append(line.trim());
					}
				}
			}
			
			// the last protein in the FASTA file
			if ((accession != null) && (sequence != null)) {
				if (!mapProteinToTargets(sequence.toString(), accession,
						description, protein, mappedProteins, mapCounts)) {
					mapCounts.put("unmapped",
							mapCounts.get("unmapped") + 1);
				}
			}
			
		} catch (IOException ex) {
			logger.error("error parsing fasta file:", ex);
		} finally {
			if (fastaReader != null) {
				try {
					fastaReader.close();
				} catch (IOException ex) {
					logger.error("error while closing fasta file:", ex);
				}
			}
		}
		
		
		// add all remaining targets to the proteinMap
		for (Map.Entry<String, GenericProtein> proteinIt
				: targetProteins.entrySet()) {
			if (!mappedProteins.containsKey(proteinIt.getKey())) {
				mappedProteins.put(proteinIt.getKey(), proteinIt.getValue());
			}
		}
		
		logger.info("Totally mapped proteins: " +
				(mapCounts.get("equal") + mapCounts.get("elongation") +
				mapCounts.get("mutations") + mapCounts.get("longTarget")));
		logger.info("Mapped on equal sequence: " + mapCounts.get("equal"));
		logger.info("Mapped with elongation: " + mapCounts.get("elongation"));
		logger.info("Not mapped because of probable mutations (both proteins taken): " + mapCounts.get("mutations"));
		logger.info("Targets longer (and taken): " + mapCounts.get("longTarget"));
		
		logger.info("Unmapped proteins: " + mapCounts.get("unmapped"));
		
		logger.info("Proteins for export: " + mappedProteins.size());
		
		return mappedProteins.size();
	}
	
	
	/**
	 * This method maps a reference protein to a target. Either the target
	 * protein is already given, because there was an entry in the mapping file,
	 * or every possible target protein will be tested.<br>
	 * If the protein can be mapped to a target protein, this will be noted
	 * in the target's description. If the sequence needs to be changed for a
	 * mapping, because either the target or the reference has a longer
	 * sequence, also the start and end will be adjusted.
	 * 
	 * @param sequence sequence of the reference protein
	 * @param accession accession of the reference protein
	 * @param description description of the reference protein
	 * @param targetProtein the dedicated target protein, found by mapping
	 * @param proteinMap map of all proteins (which will be exported)
	 * @param counter map from "equal", "elongation", "mutations", "longTarget"
	 * and "unmapped" to the corresponding number of protein entries
	 * @return true, if the protein was mapped to a target protein, false if not
	 */
	private boolean mapProteinToTargets(String sequence, String accession,
			String description, GenericProtein targetProtein,
			Map<String, GenericProtein> proteinMap,
			Map<String, Integer> counter) {
		boolean mapped = false;
		
		if (targetProtein != null) {
			if (targetProtein.getSequence().equals(sequence)) {
				// sequences are equal -> just note in description
				if (!proteinMap.containsKey(targetProtein.getAccession())) {
					proteinMap.put(targetProtein.getAccession(), targetProtein);
				} else {
					targetProtein = proteinMap.get(
							targetProtein.getAccession());
				}
				targetProtein.setDescription(targetProtein.getDescription() +
						" equal to " + accession);
				
				counter.put("equal", counter.get("equal") + 1);
				mapped = true;
			} else {
				// sequences are not equal
				if (targetProtein.getSequence().length() <= sequence.length()) {
					// the target sequence is smaller (or equal)
					if (sequence.contains(targetProtein.getSequence())) {
						// the whole target sequence is in the reference sequence
						int preLength = sequence.indexOf(targetProtein.getSequence());
						int postLength = sequence.length() - 
								targetProtein.getSequence().length() -
								preLength;
						
						if (!targetProtein.getIsComplement()) {
							targetProtein.setStart(targetProtein.getStart() - preLength * 3);
							targetProtein.setEnd(targetProtein.getEnd() + postLength * 3);
						} else {
							targetProtein.setStart(targetProtein.getStart() - postLength * 3);
							targetProtein.setEnd(targetProtein.getEnd() + preLength * 3);
						}
						
						if (!proteinMap.containsKey(targetProtein.getAccession())) {
							proteinMap.put(targetProtein.getAccession(), targetProtein);
						} else {
							targetProtein = proteinMap.get(targetProtein.getAccession());
						}
						targetProtein.setSequence(sequence);
						targetProtein.setDescription(targetProtein.getDescription() + 
								" elongation by " + accession);
						
						counter.put("elongation", counter.get("elongation") + 1);
						mapped = true;
					} else {
						// mostly the first AA is changed, try to map without
						String proteinWithoutStartAA = 
								targetProtein.getSequence().substring(1);
						
						if (sequence.contains(proteinWithoutStartAA)) {
							int preLength = sequence.indexOf(
									proteinWithoutStartAA) - 1;
							int postLength = sequence.length() - 
									proteinWithoutStartAA.length() - 1 - 
									preLength;
							
							if (!targetProtein.getIsComplement()) {
								targetProtein.setStart(targetProtein.getStart() - preLength * 3);
								targetProtein.setEnd(targetProtein.getEnd() + postLength * 3);
							} else {
								targetProtein.setStart(targetProtein.getStart() - postLength * 3);
								targetProtein.setEnd(targetProtein.getEnd() + preLength * 3);
							}
							
							if (!proteinMap.containsKey(targetProtein.getAccession())) {
								proteinMap.put(targetProtein.getAccession(), targetProtein);
							} else {
								targetProtein = proteinMap.get(targetProtein.getAccession());
							}
							
							targetProtein.setSequence(sequence);
							targetProtein.setDescription(targetProtein.getDescription() + 
									" elongation by " + accession);
							
							counter.put("elongation", counter.get("elongation") + 1);
							mapped = true;
						} else {
							// there seems to be modifications, take both proteins
							if (!proteinMap.containsKey(targetProtein.getAccession())) {
								proteinMap.put(targetProtein.getAccession(), targetProtein);
							} else {
								targetProtein = proteinMap.get(targetProtein.getAccession());
							}
							targetProtein.setDescription(targetProtein.getDescription() + 
									" ref=" + accession);
							
							Long start = null;
							Long end = null;
							
							if (targetProtein.getSequence().length() == sequence.length()) {
								// same length -> same start/end
								start = targetProtein.getStart();
								end = targetProtein.getEnd();
							} else {
								// check for same start or end and calculate length
								if (targetProtein.getSequence().startsWith(
										sequence.substring(0, 15))) {
									// same start
									if (!targetProtein.getIsComplement()) {
										start = targetProtein.getStart();
										end = start + 3 * sequence.length();
									} else {
										end = targetProtein.getEnd();
										start = end - 3 * sequence.length();
									}
								} else if (targetProtein.getSequence().endsWith(
										sequence.substring(sequence.length()-15))){
									// same end
									if (!targetProtein.getIsComplement()) {
										end = targetProtein.getEnd();
										start = end - 3 * sequence.length();
									} else {
										start = targetProtein.getStart();
										end = start + 3 * sequence.length();
									}
								} else {
									// neither start nor end are the same
									start = -1L;
									end = -1L;
								}
							}
							
							GenericProtein refProtein = null;
							if (!proteinMap.containsKey(accession)) {
								refProtein = new GenericProtein(
										accession,
										description,
										targetProtein.getGenomeName(),
										sequence,
										start,
										end,
										targetProtein.getIsComplement());
								
								proteinMap.put(accession, refProtein);
							} else {
								refProtein = proteinMap.get(accession);
							}
							refProtein.setDescription(
									refProtein.getDescription() + 
									" ref=" + targetProtein.getAccession());
							
							
							counter.put("mutations", counter.get("mutations") + 1);
							mapped = true;
						}
					}
				} else {
					// the target sequence is longer, keep it
					if (!proteinMap.containsKey(targetProtein.getAccession())) {
						proteinMap.put(targetProtein.getAccession(), targetProtein);
					} else {
						targetProtein = proteinMap.get(targetProtein.getAccession());
					}
					
					targetProtein.setDescription(targetProtein.getDescription() + 
							" elongation of " + accession);
					
					counter.put("longTarget", counter.get("longTarget") + 1);
					mapped = true;
				}
			}
		} else {
			for (GenericProtein target : targetProteins.values()) {
				if ((target.getSequence().length() <= sequence.length()) &&
						sequence.contains(target.getSequence().substring(1))) {
					// the target (without first AA) is contained in the sequence
					
					if (target.getSequence().equals(sequence)) {
						// sequences are equal -> just note in description
						if (!proteinMap.containsKey(target.getAccession())) {
							proteinMap.put(target.getAccession(), target);
						} else {
							target = proteinMap.get(
									target.getAccession());
						}
						target.setDescription(target.getDescription() +
								" equal to " + accession);
						
						counter.put("equal", counter.get("equal") + 1);
						mapped = true;
					} else if (sequence.length() - target.getSequence().length() < 100) {
						// the length difference is not too much -> take it as elongation
						int preLength = sequence.indexOf(
								target.getSequence().substring(1)) - 1;
						
						int postLength = sequence.length() - 
								target.getSequence().length() -
								preLength;
						
						if (!target.getIsComplement()) {
							target.setStart(target.getStart() - preLength * 3);
							target.setEnd(target.getEnd() + postLength * 3);
						} else {
							target.setStart(target.getStart() - postLength * 3);
							target.setEnd(target.getEnd() + preLength * 3);
						}
						
						if (!proteinMap.containsKey(target.getAccession())) {
							proteinMap.put(target.getAccession(), target);
						} else {
							target = proteinMap.get(
									target.getAccession());
						}
						
						target.setSequence(sequence);
						target.setDescription(target.getDescription() + 
								" elongation by " + accession);
						
						counter.put("elongation", counter.get("elongation") + 1);
						mapped = true;
					}
				} else if ((target.getSequence().contains(sequence.substring(1))) &&
						(target.getSequence().length() - sequence.length() < 100)) {
					// searching without first AA (this changes sometimes)
					// the target sequence is longer, keep it
					if (!proteinMap.containsKey(target.getAccession())) {
						proteinMap.put(target.getAccession(), target);
					} else {
						target = proteinMap.get(target.getAccession());
					}
					
					target.setDescription(target.getDescription() + 
							" elongation of " + accession);
					
					counter.put("longTarget", counter.get("longTarget") + 1);
					mapped = true;
				}
			}
		}
		
		if (!mapped) {
			// add the unmapped protein without start/stop etc.
			if (!proteinMap.containsKey(accession)) {
				GenericProtein unmappedProtein = new GenericProtein(
						accession,
						description,
						"unknown",
						sequence);
				proteinMap.put(accession, unmappedProtein);
			} else {
				proteinMap.get(accession).setDescription(
						proteinMap.get(accession).getDescription() + 
						"ref=" + accession);
			}
		}
		
		return mapped;
	}
	
	
	/**
	 * Tests the regular accessions for the reference FASTA file.
	 * 
	 * @param referenceFasta the FASTA file
	 * @param accessionRegex regular expression for the accession
	 * @param alternativeMappingRegex regular expression for the gene name or
	 * alternative mapping name
	 * @param descriptionRegex regular expression for  the protein description
	 * @param nrEntries maximal entries
	 * @return a {@link List} of String[], the first entry of the array is the
	 * whole protein header, the second the accession, the third the alternative
	 * mapping and the fourth the description
	 */
	public static List<String[]> testSettingsForReferenceProteinList(
			String referenceFasta, String accessionRegex,
			String alternativeMappingRegex,String descriptionRegex,
			int nrEntries) {
		List<String[]> entries = new ArrayList<String[]>(nrEntries);
		
		Pattern accessionPattern = Pattern.compile(accessionRegex);
		Matcher accessionMatcher;
		Pattern alternativeMappingPattern =
				Pattern.compile(alternativeMappingRegex);
		Matcher alternativeMappingMatcher;
		Pattern descriptionPattern =
				Pattern.compile(descriptionRegex);
		Matcher descriptionMatcher;
		
		BufferedReader fastaReader = null;
		try  {
			fastaReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(referenceFasta)));
			
			String line;
			while (((line = fastaReader.readLine()) != null) &&
					(entries.size() < nrEntries)) {
				if (line.startsWith(">")) {
					
					String[] entry = new String[4];
					entry[0] = line;
					
					accessionMatcher = accessionPattern.matcher(line);
					if (accessionMatcher.matches()) {
						entry[1] = accessionMatcher.group(1);
					} else {
						entry[1] = "NO MATCH";
					}
					
					alternativeMappingMatcher =
							alternativeMappingPattern.matcher(line);
					if (alternativeMappingMatcher.matches()) {
						entry[2] = alternativeMappingMatcher.group(1);
					} else {
						entry[2] = "NO MATCH";
					}
					
					descriptionMatcher = descriptionPattern.matcher(line);
					if (descriptionMatcher.matches()) {
						entry[3] = descriptionMatcher.group(1);
					} else {
						entry[3] = "NO MATCH";
					}
					
					entries.add(entry);
				}
			}
		} catch (IOException ex) {
			logger.error("Error parsing fasta file.", ex);
		} finally {
			if (fastaReader != null) {
				try {
					fastaReader.close();
				} catch (IOException ex) {
					logger.error("Error while closing fasta file.", ex);
				}
			}
		}
		
		return entries;
	}
	
	
	/**
	 * Write the mapped information to GFF file.
	 * 
	 * @param gffFileName
	 * @return
	 */
	public int writeToGFF(String gffFileName) {
		int writtenProteins = 0;
		
		BufferedWriter gffWriter = null;
		try {
			gffWriter = new BufferedWriter(new FileWriter(gffFileName));
			gffWriter.append("##gff-version 3");
			gffWriter.newLine();
			
			for (GenericProtein protein : mappedProteins.values()) {
				gffWriter.append(protein.buildGFFline());
				gffWriter.newLine();
				writtenProteins++;
			}
			
			for (GenericProtein target : sequencelessProteins.values()) {
				gffWriter.append(target.buildGFFline());
				gffWriter.newLine();
				writtenProteins++;
			}
		} catch (IOException ex) {
			logger.error("error writing GFF file", ex);
		} finally {
			if (gffWriter != null) {
				try {
					gffWriter.close();
				} catch (IOException ex) {
					logger.error("error closing GFF file", ex);
				}
			}
		}
		
		return writtenProteins;
	}
	
	
	/**
	 * Write the mapped proteins to FASTA file.
	 * 
	 * @param fastaFileName
	 * @return
	 */
	public int writeToFASTA(String fastaFileName) {
		int writtenProteins = 0;
		
		BufferedWriter fastaWriter = null;
		try {
			fastaWriter = new BufferedWriter(new FileWriter(fastaFileName));
			
			for (GenericProtein protein : mappedProteins.values()) {
				fastaWriter.append(protein.toFastaEntry());
				fastaWriter.newLine();
				writtenProteins++;
			}
		} catch (IOException ex) {
			logger.error("error writing FASTA file", ex);
		} finally {
			if (fastaWriter != null) {
				try {
					fastaWriter.close();
				} catch (IOException ex) {
					logger.error("error closing FASTA file", ex);
				}
			}
		}
		
		return writtenProteins;
	}
	
	
	public static void main(String[] args) {
		// TODO: include command line parser
		
		CompareAndCombineProteinInformation cacpl =
				new CompareAndCombineProteinInformation();
		
		int parsedTargetProteins = 
				cacpl.getDataForTargetProteinlist(
						"/mnt/data/uniNOBACKUP/cyanobacterium_coop/20140317-cyanobase/GFFs/genes.gff",
						"/mnt/data/uniNOBACKUP/cyanobacterium_coop/20140317-cyanobase/proteinDBs/proteins.fasta",
						">([^ ]*) .*");
		logger.info("parsed from target files: " + parsedTargetProteins);
		
		int parsedMappings = cacpl.parseAccessionMapping(
				"/mnt/data/uniNOBACKUP/cyanobacterium_coop/20140317-cyanobase/syny3.txt",
				"^\\S+\\s+\\S+\\s+(\\S+)\\s+\\d+\\s+.*",
				"^(\\S+)\\s+.*");
		logger.info("parsed mappings: " + parsedMappings);
		
		cacpl.mapTargetsToReference(
				"/mnt/data/uniNOBACKUP/cyanobacterium_coop/20140317-cyanobase/proteinDBs/uniprot_reference-c_synechocystis-20140318.fasta",
				"^>[sptr]{2}\\|(\\S+)\\|.*",
				"^>[sptr]{2}\\|\\S+\\|\\S+ .+ GN=(\\S+) PE=\\d+ SV=\\d+$",
				"^>[sptr]{2}\\|\\S+\\|(.+)$");
		
		int gffWritten = cacpl.writeToGFF(
				"/mnt/data/uniNOBACKUP/cyanobacterium_coop/20140317-cyanobase/GFFs/genes-with_uniprot.gff");
		logger.info(gffWritten + " proteins/genes written to GFF file");
		
		int fastaWritten = cacpl.writeToFASTA(
				"/mnt/data/uniNOBACKUP/cyanobacterium_coop/20140317-cyanobase/proteinDBs/proteins-with_uniprot.fasta");
		logger.info(fastaWritten + " proteins/genes written to FASTA file");
	}
}
