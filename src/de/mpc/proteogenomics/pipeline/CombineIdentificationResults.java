package de.mpc.proteogenomics.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.peptide.IdentifiedPeptide;
import de.mpc.proteogenomics.pipeline.protein.AbstractProtein;
import de.mpc.proteogenomics.pipeline.protein.GenericProtein;
import uk.ac.ebi.pride.jmztab.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab.model.Metadata;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab.utils.parser.MTDLineParser;
import uk.ac.ebi.pride.jmztab.utils.parser.PSHLineParser;
import uk.ac.ebi.pride.jmztab.utils.parser.PSMLineParser;
import uk.ac.ebi.pride.jmztab.utils.parser.PositionMapping;


/**
 * This Class is used to write the results from mzTab files to GFF files
 * 
 * @author julian
 *
 */
public class CombineIdentificationResults implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final static Logger logger = Logger.getLogger(CombineIdentificationResults.class);
	
	/** the occurring known proteins, mapping from the accession */
	private Map<String, GenericProtein> knownProteins;
	
	/** the occurring pseudo proteins, mapping from the accession */
	private Map<String, GenericProtein> pseudoProteins;
	
	/** the identified peptides, mapping from the sequence */
	private Map<String, IdentifiedPeptide> peptides;
	
	/** maps from the protein ID to the list of peptide IDs */
	private Map<String, Set<String>> proteinPeptides;
	
	/** the regular expression for decoys */
	private String decoyRegex;
	
	/** maps from the group to the corresponding identification file names */
	private Map<String, Set<String>> groupFileMap;
	
	/** maps from the filename to the number of identifications */
	private Map<String, Integer> fileNrIdentifications;
	
	/** whether the peptide counts should be normalized over the number of total counts per file */
	private boolean normalizeCounts;
	
	/** a scale factor for better readability */
	private float readabilityScaleFactor;
	
	
	/**
	 * Constructor
	 */
	public CombineIdentificationResults() {
		knownProteins = new HashMap<String, GenericProtein>(1000);
		pseudoProteins = new HashMap<String, GenericProtein>(1000);
		peptides = new HashMap<String, IdentifiedPeptide>(1000);
		proteinPeptides = new HashMap<String, Set<String>>(1000);
		decoyRegex = null;
		groupFileMap = new HashMap<String, Set<String>>();
		fileNrIdentifications = new HashMap<String, Integer>();
		setNormalizeCounts(false);
	}
	
	
	/**
	 * Reads in the known proteins from the given GFF file
	 * @param fileName
	 * @throws IOException
	 */
	public void parseKnownProteinsFromGFF(String fileName) throws IOException {
		logger.info("Parsing information of known proteins from " + fileName);
		
		int nr_parsed = GenericProtein.parseProteinsFromGFF(fileName,
				knownProteins, logger);
				
		logger.info("Parsing of " + fileName + " done, parsed " + nr_parsed +
				" proteins");
	}
	
	
	/**
	 * Reads in the pseudo proteins from the given GFF file
	 * @param fileName
	 * @throws IOException
	 */
	public void parsePseudoProteinsFromGFF(String fileName) throws IOException {
		logger.info("Parsing information of pseudo proteins from " + fileName);
		
		int nr_parsed = GenericProtein.parseProteinsFromGFF(fileName,
				pseudoProteins, logger);
				
		logger.info("Parsing of " + fileName + " done, parsed " + nr_parsed +
				" proteins");
	}
	
	
	/**
	 * Parses the sequence information from a FASTA file.
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void parseProteinSequencesFromFASTA(String fileName)
			throws IOException {
		CompareAndCombineProteinInformation.parseSequencesForProteins(fileName,
				"^>(\\S+)\\s.*", knownProteins);
		
		CompareAndCombineProteinInformation.parseSequencesForProteins(fileName,
				"^>(\\S+)\\s.*", pseudoProteins);
	}
	
	
	/**
	 * Sets the decoy regex.
	 * 
	 * @param regex
	 */
	public void setDecoyRegex(String regex) {
		this.decoyRegex = regex;
	}
	
	
	/**
	 * Reads in the data from the given mzTab file.
	 * 
	 * @param fileName
	 * @param groupName
	 * 
	 * @throws IOException
	 * @throws MZTabException 
	 */
	public void parseMzTab(String fileName, String groupName)
			throws IOException, MZTabException {
		logger.info("Parsing identifications from " + fileName);
		
		Set<String> fileNames = groupFileMap.get(groupName);
		if (fileNames == null) {
			fileNames = new HashSet<String>();
			groupFileMap.put(groupName, fileNames);
		}
		fileNames.add(fileName);
		
		MZTabErrorList errorList = new MZTabErrorList();
		
		BufferedReader fileReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName)));
		
		Metadata metadata = null;
		MTDLineParser mtdParser = new MTDLineParser();
		PSMLineParser psmParser = null;
		
		Pattern decoyPattern = null;
		if (decoyRegex != null) {
			decoyPattern = Pattern.compile(decoyRegex);
		}
		
		Set<String> psmIDs = new HashSet<String>(2000);
		int lineNumber = 0;
		String line;
		while ((line = fileReader.readLine()) != null) {
			lineNumber++;
			
			if (line.trim().length() == 0) {
                continue;
            } else if (line.startsWith("MTD")) {
				mtdParser.parse(lineNumber, line, errorList);
			} else if (line.startsWith("PSH")) {
				if (metadata == null) {
					// the metadate is complete, when PSH starts
					mtdParser.refineNormalMetadata();
					metadata = mtdParser.getMetadata();
				}
				
				PSHLineParser  pshParser = new PSHLineParser(metadata);
				pshParser.parse(1, line, errorList);
				
				psmParser = new PSMLineParser(pshParser.getFactory(),
						new PositionMapping(pshParser.getFactory(), line),
						metadata, errorList);
			} else if ((psmParser != null) && line.startsWith("PSM")) {
				psmParser.parse(lineNumber, line, errorList);
				PSM psm = psmParser.getRecord();
				
				boolean knownProtein = false;
				
				if ((decoyRegex != null) && 
						decoyPattern.matcher(psm.getAccession()).matches()) {
					continue;
				}
				
				GenericProtein protein = pseudoProteins.get(psm.getAccession());
				if (protein == null) {
					protein = knownProteins.get(psm.getAccession());
					knownProtein = true;
				}
				
				if (protein == null) {
					logger.error("could not parse PSM line, protein not found:\n\t" + line);
					continue;
				}
				
				Long start = protein.getStart();
				Long end = protein.getEnd();
				
				if ((protein.getIsComplement() != null) &&
						(start != null) && (end != null) && 
						(psm.getStart() != null) && (psm.getEnd() != null)) {
					if (!protein.getIsComplement()) {
						start += psm.getStart() * 3;
						end = start + psm.getSequence().length() * 3;
					} else {
						end -= psm.getStart() * 3;
						start = end - psm.getSequence().length() * 3;
					}
				}
				
				String peptideID = psm.getSequence();
				
				IdentifiedPeptide peptide = peptides.get(peptideID);
				if (peptide == null) {
					// create the peptide
					peptide = new IdentifiedPeptide(psm.getSequence());
					peptides.put(peptideID, peptide);
				}
				
				// workaround for defect psm.getPSM_ID() always returning null
				String psmID = psm.getPSM_ID();
				if (psmID == null) {
					String[] splittedLine = line.split(MZTabConstants.TAB + "");
					if ((splittedLine.length > 2) && (splittedLine[2] != null)) {
						psmID = splittedLine[2];
					}
				}
				
				peptide.addPSM(fileName, psmID, protein, start, end,
						!knownProtein, groupName);
				
				psmIDs.add(psmID);
				
				// add the peptide to the protein/peptide map
				Set<String> proteinPeptideSet = 
						proteinPeptides.get(psm.getAccession());
				if (proteinPeptideSet == null) {
					proteinPeptideSet = new HashSet<String>();
					proteinPeptides.put(psm.getAccession(), proteinPeptideSet);
				}
				proteinPeptideSet.add(peptideID);
			}
		}
		
		logger.debug("all lines done");
		
		fileReader.close();
		
		fileNrIdentifications.put(fileName, psmIDs.size());
		
		logger.info("Parsing of " + fileName + " done\n\t" +
				peptides.size() + " different peptides identified in all files");
	}
	
	
	/**
	 * Getter for the peptides. 
	 * <p>
	 * WARNING: returns the actual map, do not change these data, unless you
	 * know, what you are doing!
	 * 
	 * @return
	 */
	public Map<String, IdentifiedPeptide> getPeptides() {
		return peptides;
	}
	
	
	/**
	 * Returns the groups.
	 * 
	 * @return
	 */
	public Set<String> getGroups() {
		return groupFileMap.keySet();
	}
	
	
	/**
	 * Sets, whether counts should be normalized or not
	 * @param normalize
	 */
	public void setNormalizeCounts(boolean normalize) {
		normalizeCounts = normalize;
		
		if (normalizeCounts) {
			readabilityScaleFactor = -1;
			
			for (Integer fileCounts : fileNrIdentifications.values()) {
				if ((readabilityScaleFactor < 0) ||
						(readabilityScaleFactor > fileCounts)) {
					readabilityScaleFactor = fileCounts;
				}
			}
		} else {
			readabilityScaleFactor = 1;
		}
		
	}
	
	/**
	 * getter, whether counts are normalized or not
	 * @return
	 */
	public boolean getNormalizeCounts() {
		return normalizeCounts;
	}
	
	
	/**
	 * Returns the number of identified spectra for the given group and peptide
	 * 
	 * @param peptide
	 * @param groupName
	 * @return
	 */
	public float getNrIdentificationsInGroup(IdentifiedPeptide peptide,
			String groupName) {
		if (!groupFileMap.containsKey(groupName)) {
			return 0;
		}
		
		float count = 0;
		for (String fileName : groupFileMap.get(groupName)) {
			float increment = peptide.getNrIdentifiedSpectra(fileName);
			
			if (normalizeCounts) {
				increment = increment /
						(float)fileNrIdentifications.get(fileName);
			}
			
			count += increment * readabilityScaleFactor;
		}
		
		return count;
	}
	
	
	/**
	 * Getter for the mapping from the proteins to peptides.
	 * <p>
	 * WARNING: returns the actual map, do not change these data, unless you
	 * know, what you are doing!
	 * 
	 * @return
	 */
	public Map<String, Set<String>> getProteinPeptideMap() {
		return proteinPeptides;
	}
	
	
	/**
	 * Writes out the identified  peptides to GFF files
	 * 
	 * @param fileName
	 * @throws IOException 
	 */
	public void writeIdentifiedPeptidesToGFF(String onlyPseudoFilename, 
			String otherFilename) throws IOException {
		
		BufferedWriter pseudoWriter = null;
		if (onlyPseudoFilename != null) {
			pseudoWriter =
					new BufferedWriter(new FileWriter(onlyPseudoFilename));
			pseudoWriter.append("##gff-version 3\n");
			logger.info("writing pseudo peptide to " + onlyPseudoFilename);
		}
		
		BufferedWriter otherWriter = null;
		if (otherFilename != null) {
			otherWriter = new BufferedWriter(new FileWriter(otherFilename));
			otherWriter.append("##gff-version 3\n");
			logger.info("writing all other peptide to " + otherFilename);
		}
		
		if ((pseudoWriter == null) && (otherWriter == null)) {
			// nothing to do here
			logger.warn("Neither file for pseudo peptides nor for other " +
					"given, probably wrong call?");
			return;
		}
		
		for (IdentifiedPeptide peptide : peptides.values()) {
			
			for (Map.Entry<Long[], List<AbstractProtein>> protPosIt
					: peptide.getPositionsToProteins().entrySet()) {
				String genomeStr = null;
				StringBuilder gffAttributes = new StringBuilder();
				Boolean isComplement = null;
				String start = (protPosIt.getKey()[0] == null) ? "." :
					protPosIt.getKey()[0].toString();
				String end = (protPosIt.getKey()[1] == null) ? "." :
					protPosIt.getKey()[1].toString();
				
				for (AbstractProtein protein : protPosIt.getValue()) {
					// there are more than one protein, if one elongates the other
					genomeStr = protein.getGenomeName();
					isComplement = protein.getIsComplement();
					
					if (gffAttributes.length() == 0) {
						gffAttributes.append("ID=");
						gffAttributes.append(peptide.getSequence());
						gffAttributes.append("-");
						gffAttributes.append(start);
						gffAttributes.append("-");
						gffAttributes.append(end);
						gffAttributes.append(";Name=");
						gffAttributes.append(peptide.getSequence());
						gffAttributes.append("-");
						gffAttributes.append(start);
						gffAttributes.append("-");
						gffAttributes.append(end);
						
						if (protein.getIsComplement() != null) {
							gffAttributes.append(";Note=Frame");
							if (protein.getIsComplement()) {
								gffAttributes.append((protein.getStart()-1)%3+3);
							} else {
								gffAttributes.append((protein.getStart()-1)%3);
							}
						}
					}
					
					gffAttributes.append(";Note=");
					gffAttributes.append(protein.getAccession());
				}
				
				StringBuilder pepLine = new StringBuilder();
				
				// print the overview line
				pepLine.append(genomeStr);
				pepLine.append("\t");
				pepLine.append("ProteoGenomicsPipeline");
				pepLine.append("\t");
				pepLine.append("peptide");
				pepLine.append("\t");
				pepLine.append(start);
				pepLine.append("\t");
				pepLine.append(end);
				pepLine.append("\t");
				pepLine.append(peptide.getNrAllIdentifications());
				pepLine.append("\t");
				pepLine.append((isComplement == null) ? "." :
					isComplement ? "-" : "+");
				pepLine.append("\t");
				pepLine.append(".");
				pepLine.append("\t");
				pepLine.append(gffAttributes);
				pepLine.append("\n");
				
				
				// and the line for each group
				for (String groupName : groupFileMap.keySet()) {
					
					pepLine.append(genomeStr);
					pepLine.append("\t");
					pepLine.append(groupName);
					pepLine.append("\t");
					pepLine.append("peptide");
					pepLine.append("\t");
					pepLine.append(start);
					pepLine.append("\t");
					pepLine.append(end);
					pepLine.append("\t");
					pepLine.append(
							getNrIdentificationsInGroup(peptide, groupName));
					pepLine.append("\t");
					pepLine.append((isComplement == null) ? "." :
						isComplement ? "-" : "+");
					pepLine.append("\t");
					pepLine.append(".");
					pepLine.append("\t");
					pepLine.append(gffAttributes);
					pepLine.append("\n");
				}
				
				if (peptide.getHasOnlyGenomeTranslations()) {
					if (pseudoWriter != null) {
						pseudoWriter.append(pepLine);
					}
				} else if (otherWriter != null) {
					otherWriter.append(pepLine);
				}
			}
		}
		
		if (pseudoWriter != null) {
			pseudoWriter.close();
			logger.info("pseudo peptides written to " + onlyPseudoFilename);
		}
		
		if (otherWriter != null) {
			otherWriter.close();
			logger.info("all other peptides written to " + otherFilename);
		}
	}
	
	
	/**
	 * Writes out the identified  peptides to a TSV file
	 * 
	 * @param tsvFilename
	 * @throws IOException
	 */
	public void writeIdentifiedPeptidesToTSV(String tsvFilename)
			throws IOException {
		BufferedWriter peptideWriter =
				new BufferedWriter(new FileWriter(tsvFilename));
		
		logger.info("TSV export of peptides to " + tsvFilename);
		
		List<String> groupsList = new ArrayList<String>(groupFileMap.keySet());
		
		peptideWriter.append("\"sequence\"");
		peptideWriter.append("\t\"seqIDs\"");
		peptideWriter.append("\t\"accessions\"");
		peptideWriter.append("\t\"pseudo proteins only\"");
		peptideWriter.append("\t\"elongation of known\"");
		peptideWriter.append("\t\"standalone\"");
		peptideWriter.append("\t\"#identifications\"");
		for (String groupName : groupsList) {
			peptideWriter.append("\t\"");
			peptideWriter.append(groupName);
			peptideWriter.append("\"");
		}
		peptideWriter.append("\n");
		
		for (IdentifiedPeptide peptide : peptides.values()) {
			
			StringBuilder seqIDs = new StringBuilder();
			StringBuilder accessions = new StringBuilder();
			
			Set<String> seqIdsSet = new HashSet<String>();
			for (AbstractProtein prot : peptide.getProteins()) {
				seqIdsSet.add(prot.getGenomeName());
				
				if (accessions.length() > 0) {
					accessions.append(';');
				}
				
				accessions.append(prot.getAccession());
			}
			
			for (String seqID : seqIdsSet) {
				if (seqIDs.length() > 0) {
					seqIDs.append(';');
				}
				seqIDs.append(seqID);
			}
			
			peptideWriter.append('"');
			peptideWriter.append(peptide.getSequence());
			peptideWriter.append("\"\t\"");
			peptideWriter.append(seqIDs);
			peptideWriter.append("\"\t\"");
			peptideWriter.append(accessions);
			peptideWriter.append("\"\t\"");
			peptideWriter.append(peptide.getHasOnlyGenomeTranslations() ?
					"true" : "false");
			peptideWriter.append("\"\t\"");
			peptideWriter.append(peptide.getIsElongation() ? "true" : "false");
			peptideWriter.append("\"\t\"");
			peptideWriter.append((peptide.getHasOnlyGenomeTranslations() &&
					!peptide.getIsElongation()) ? "true" : "false");
			peptideWriter.append("\"\t\"");
			peptideWriter.append(
					(new Integer(peptide.getNrAllIdentifications())).toString());
			
			for (String groupName : groupsList) {
				peptideWriter.append("\"\t\"");
				peptideWriter.append(
						(new Float(getNrIdentificationsInGroup(peptide, groupName))).toString());
			}
			
			peptideWriter.append("\"\n");
		}
		
		peptideWriter.close();
		
		logger.info("TSV export finished");
	}
	
	
	/**
	 * Saves the data to the given file path in a SQLite database
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void saveToFile(String fileName) throws IOException {
		Connection connection = null;
		
		logger.info("Saving data to " + fileName);
		
		try {
			// load the sqlite-JDBC
			Class.forName("org.sqlite.JDBC");
			
			// delete the file, it it exists
			File oldFile = new File(fileName);
			if (oldFile.exists() && oldFile.isFile()) {
				logger.info("File exists, deleting.");
				oldFile.delete();
			}
			oldFile = null;
			
			// create a database connection to the (new) file
			connection = DriverManager.getConnection(
					"jdbc:sqlite:" + fileName);
			connection.setAutoCommit(false);
			
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			
			statement.executeUpdate("drop table if exists proteins");
			statement.executeUpdate("create table proteins"
					+ "(accession TEXT,"
					+ " description TEXT,"
					+ " genomeName TEXT,"
					+ " sequence TEXT,"
					+ " start NUMERIC,"
					+ " end NUMERIC,"
					+ " isComplement TEXT,"
					+ " isPseudoProtein TEXT)");
			
			// save the knownProteins data
		    PreparedStatement prepStmt =
		    		connection.prepareStatement("insert into proteins "
							+ "values(?, ?, ?, ?, ?, ?, ?, ?)");
			for (GenericProtein protein : knownProteins.values()) {
				prepStmt.setString(1, protein.getAccession());
				prepStmt.setString(2, protein.getDescription());
				prepStmt.setString(3, protein.getGenomeName());
				prepStmt.setString(4, protein.getSequence());
				
				if (protein.getStart() != null) {
					prepStmt.setLong(5, protein.getStart());
				} else {
					prepStmt.setString(5, null);
				}
				
				if (protein.getEnd() != null) {
					prepStmt.setLong(6, protein.getEnd());
				} else {
					prepStmt.setString(6, null);
				}
				
				prepStmt.setString(7, (protein.getIsComplement() != null) ? 
						protein.getIsComplement().toString() : null);
				prepStmt.setString(8, "false");
				
				prepStmt.addBatch();
			}
			
			// save the pseudoProteins data
			for (GenericProtein protein : pseudoProteins.values()) {
				prepStmt.setString(1, protein.getAccession());
				prepStmt.setString(2, protein.getDescription());
				prepStmt.setString(3, protein.getGenomeName());
				prepStmt.setString(4, protein.getSequence());
				
				if (protein.getStart() != null) {
					prepStmt.setLong(5, protein.getStart());
				} else {
					prepStmt.setString(5, null);
				}
				
				if (protein.getEnd() != null) {
					prepStmt.setLong(6, protein.getEnd());
				} else {
					prepStmt.setString(6, null);
				}
				
				prepStmt.setString(7, (protein.getIsComplement() != null) ? 
						protein.getIsComplement().toString() : null);
				prepStmt.setString(8, "true");
				
				prepStmt.addBatch();
			}
			prepStmt.executeBatch();
			logger.info("protein information saved");
			
			// the files
			Map<String, Long> fileNameToID = new HashMap<String, Long>();
			
			statement.executeUpdate("drop table if exists files");
			statement.executeUpdate("create table files"
					+ "(id NUMERIC,"					// ID of the file
					+ " filename TEXT,"					// filename
					+ " groupname TEXT,"				// the groupname
					+ " nrIDs NUMERIC)");				// number of identifications
			
			prepStmt = connection.prepareStatement("insert into files "
					+ "values(?, ?, ?, ?)");
			long tmpID = 1;
			for (Map.Entry<String, Set<String>> groupFileIt
					: groupFileMap.entrySet()) {
				prepStmt.setString(3, groupFileIt.getKey());
				
				for (String file : groupFileIt.getValue()) {
					prepStmt.setLong(1, tmpID);
					prepStmt.setString(2, file);
					prepStmt.setInt(4, fileNrIdentifications.get(file));
					prepStmt.addBatch();
					
					if (fileNameToID.put(file, tmpID) != null) {
						logger.warn("filename in multiple groups: " + file);
					}
					tmpID++;
				}
			}
			prepStmt.executeBatch();
			logger.info("file information saved");
			
			
			// save the peptides data
			statement.executeUpdate("drop table if exists peptides");
			statement.executeUpdate("create table peptides"
					+ "(id NUMERIC,"					// ID of the peptide, generated while iterating
					+ " sequence TEXT)");				// the sequence of the peptide
			
			statement.executeUpdate("drop table if exists proteinPositions");
			statement.executeUpdate("create table proteinPositions"
					+ "(peptideID NUMERIC,"				// the associated peptide
					+ " accession TEXT,"				// accession of the protein
					+ " start NUMERIC,"					// start position in genome
					+ " end NUMERIC)");					// end position in genome
			
			statement.executeUpdate("drop table if exists identifications");
			statement.executeUpdate("create table identifications"
					+ "(peptideID NUMERIC,"				// the associated peptide
					+ " fileID NUMERIC,"				// the fileID of the identification
					+ " psmID TEXT)");					// the psmID of the identification
			
			prepStmt = connection.prepareStatement("insert into peptides "
							+ "values(?, ?)");
			PreparedStatement positionStmt = connection.prepareStatement(
					"insert into proteinPositions values(?, ?, ?, ?)");
			
			PreparedStatement identificationStmt = connection.prepareStatement(
					"insert into identifications values(?, ?, ?)");
			
			tmpID = 1;
			for (IdentifiedPeptide peptide : peptides.values()) {
				
				prepStmt.setLong(1, tmpID);
				prepStmt.setString(2, peptide.getSequence());
				prepStmt.addBatch();
				
				for (AbstractProtein protein : peptide.getProteins()) {
					
					Map<Long, Long> positions = 
							peptide.getProteinPositions(protein.getAccession());
					if (positions.size() > 0) {
						for (Map.Entry<Long, Long> posIt
								: positions.entrySet()) {
							positionStmt.setLong(1, tmpID);
							positionStmt.setString(2, protein.getAccession());
							positionStmt.setLong(3,
									(posIt.getKey() == null) ? -1 : posIt.getKey());
							positionStmt.setLong(4,
									(posIt.getValue() == null) ? -1 : posIt.getValue());
							positionStmt.addBatch();
						}
					} else {
						// add at least one "empty" positions entry
						positionStmt.setLong(1, tmpID);
						positionStmt.setString(2, protein.getAccession());
						positionStmt.setLong(3, -1);
						positionStmt.setLong(4, -1);
						positionStmt.addBatch();
					}
				}
				
				for (Map.Entry<String, Set<String>> fileIdsIt
						: peptide.getAllIdentifications().entrySet()) {
					for (String psmID : fileIdsIt.getValue()) {
						identificationStmt.setLong(1, tmpID);
						identificationStmt.setLong(2, fileNameToID.get(fileIdsIt.getKey()));
						identificationStmt.setString(3, psmID);
						identificationStmt.addBatch();
					}
				}
				
				tmpID++;
			}
			prepStmt.executeBatch();
			positionStmt.executeBatch();
			identificationStmt.executeBatch();
			
			statement.executeBatch();
			logger.info("peptide information saved");
			
			// save additional data
			statement.executeUpdate("drop table if exists information");
			statement.executeUpdate("create table information"
								+ "(variable TEXT,"
								+ " value TEXT)");
			
			statement.executeUpdate("insert into information "
					+ "values('version', '0.1.0')");
			
			statement.executeUpdate("insert into information "
					+ "values('created', '" + (new Date()).toString() + "')");
			
			statement.executeUpdate("insert into information "
					+ "values('decoyRegex', '"
					+ ((decoyRegex != null) ? decoyRegex.replaceAll("'", "''") : "")
						+ "')");
			
			connection.commit();
			logger.info("done writing data");
		} catch (ClassNotFoundException e) {
			logger.error("Could not load sqlite-jdbc, "
					+ "make sure you have thelibrary in your classpath.", e);
		} catch (SQLException e) {
			logger.error("Error while executing SQL command.", e);
		} finally {
			try {
				if(connection != null) {
					connection.close();
				}
			} catch(SQLException e) {
				logger.error("Error while closing file " + fileName, e);
			}
		}
	}
	
	
	/**
	 * Unserializes a serialized {@link CombineIdentificationResults} object
	 * from a file.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public static CombineIdentificationResults loadFromFile(
			String fileName) {
		logger.info("Loading data from " + fileName);
		
		CombineIdentificationResults combiner;
		Connection connection = null;
		try {
			// load the sqlite-JDBC
			Class.forName("org.sqlite.JDBC");
			
			// create a database connection to the (new) file
			connection = DriverManager.getConnection(
					"jdbc:sqlite:" + fileName);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			
			combiner = new CombineIdentificationResults();
			
			// load some information
			ResultSet rs = statement.executeQuery("select * from information");
			while(rs.next()) {
				if (rs.getString("variable").equals("decoyRegex")) {
					combiner.setDecoyRegex(rs.getString("value"));
					logger.info("decoy regex set to " + combiner.decoyRegex);
				}
			}
			
			// load the file information
			rs = statement.executeQuery(
					"select id, filename, groupname, nrIDs from files");
			Map<Integer, String> fileIDtoNameMap =
					new HashMap<Integer, String>();
			Map<Integer, String> fileIDtoGroupMap =
					new HashMap<Integer, String>();
			while(rs.next()) {
				Integer fileID = rs.getInt("id");
				String filepath = rs.getString("filename");
				String groupName = rs.getString("groupname");
				Integer nrIDs = rs.getInt("nrIDs");
				
				fileIDtoNameMap.put(fileID, filepath);
				fileIDtoGroupMap.put(fileID, groupName);
				
				Set<String> files = combiner.groupFileMap.get(groupName);
				if (files == null) {
					files = new HashSet<String>();
					combiner.groupFileMap.put(groupName, files);
				}
				files.add(filepath);
				
				
				combiner.fileNrIdentifications.put(filepath, nrIDs);
			}
			
			// load the protein information
			rs = statement.executeQuery(
					"select accession, description, genomeName, sequence, "
					+ "start, end, isComplement, isPseudoProtein from proteins");
			while(rs.next()) {
				String accession = rs.getString("accession");
				Long start;
				try {
					start = Long.parseLong(rs.getString("start"));
				} catch (NumberFormatException e) {
					start = null;
				}
				
				Long end;
				try {
					end = Long.parseLong(rs.getString("end"));
				} catch (NumberFormatException e) {
					end = null;
				}
				
				Boolean isComplement;
				try {
					isComplement = Boolean.parseBoolean(rs.getString("isComplement"));
				} catch (NumberFormatException e) {
					isComplement = null;
				}
				
				GenericProtein protein = new GenericProtein(
						accession,
						rs.getString("description"),
						rs.getString("genomeName"),
						rs.getString("sequence"),
						start,
						end,
						isComplement);
				
				if (rs.getString("isPseudoProtein").equals("true")) {
					combiner.pseudoProteins.put(accession, protein);
				} else {
					combiner.knownProteins.put(accession, protein);
				}
			}
			logger.info("number knownProteins " + combiner.knownProteins.size());
			logger.info("number pseudoProteins " + combiner.pseudoProteins.size());
			
			
			// load the PSM information
			rs = statement.executeQuery(
					"select id, sequence, fileID, psmID, accession, start, end from peptides "
					+ "inner join identifications on identifications.peptideID=peptides.id "
					+ "inner join proteinPositions on proteinPositions.peptideID=peptides.id");
			
			while (rs.next()) {
				String sequence = rs.getString("sequence");
				IdentifiedPeptide peptide =
						combiner.peptides.get(sequence);
				
				if (peptide == null) {
					peptide = new IdentifiedPeptide(sequence);
					combiner.peptides.put(peptide.getSequence(), peptide);
				}
				
				int fileID = rs.getInt("fileID");
				String idFile = fileIDtoNameMap.get(fileID);
				String groupName = fileIDtoGroupMap.get(fileID);
				String psmID = rs.getString("psmID");
				
				
				boolean isGenomeTranslation = true;
				GenericProtein protein = combiner.pseudoProteins.get(
						rs.getString("accession"));
				
				if (protein == null) {
					protein = combiner.knownProteins.get(
							rs.getString("accession"));
					
					if (protein != null) {
						isGenomeTranslation = false;
					} else {
						logger.error("could not get protein " +
								rs.getString("accession"));
						continue;
					}
				}
				
				Long start = rs.getLong("start");
				Long end = rs.getLong("end");
				
				if ((start == -1) && (end == -1)) {
					start = null;
					end = null;
				}
				
				peptide.addPSM(idFile,
						psmID, protein, start, end,
						isGenomeTranslation,
						groupName);
			}
			logger.info("number peptides " + combiner.peptides.size());
			
			// create the mapping from accession -> set(peptides)
			for (Map.Entry<String, IdentifiedPeptide>  pepIt
					: combiner.peptides.entrySet()) {
				for (AbstractProtein protein : pepIt.getValue().getProteins()) {
					Set<String> peptides =
							combiner.proteinPeptides.get(protein.getAccession());
					if (peptides == null) {
						peptides = new HashSet<String>();
						combiner.proteinPeptides.put(
								protein.getAccession(), peptides);
					}
					peptides.add(pepIt.getKey());
				}
			}
			
			combiner.setNormalizeCounts(false);
			
			logger.info("Data loaded from " + fileName);
		} catch (ClassNotFoundException e) {
			logger.error("Could not load sqlite-jdbc, "
					+ "make sure you have the required library in your classpath.", e);
			combiner = null;
		} catch (SQLException e) {
			logger.error("Error while executing SQL command.", e);
			combiner = null;
		} finally {
			try {
				if(connection != null) {
					connection.close();
				}
			} catch(SQLException e) {
				logger.error("Error while closing file " + fileName, e);
			}
		}
		
		return combiner;
	}
	
	
	
	/**
	 * Read in the CSV files and generate a proteins.gff and a peptides.gff
	 * 
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {
		boolean showHelp = false;
		CommandLineParser cliParser = new GnuParser();
		
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("help")
                .withDescription("show help")
                .create("help"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg(true)
                .withDescription("the known proteins in GFF format (e.g. " +
                		"from prior call of" +
                		ParseProteinInformation.class.getCanonicalName() +
                		"), may be called more than once")
                .create("known"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg(true)
                .withDescription("the pseudo proteins in GFF format (e.g. " +
                		"from prior call of" +
                		GenomeParser.class.getCanonicalName() +"), may " +
                		"be called more than once")
                .create("pseudo"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
				.hasArg(true)
                .withDescription("a FASTA file to get the protein sequences")
                .create("fasta"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg(true)
                .withDescription("the identified peptides in mzTab format, may " +
                		"be called more than once, the group of the file's "
                		+ "identifications may be given before the filename, "
                		+ "separated by a colon ':', if no group is given, "
                		+ "'default' is assumed.")
                .create("identifications"));
		
		options.addOption(OptionBuilder
				.withArgName("regex")
				.hasArg(true)
                .withDescription("regular expression to identify decoy"
                		+ "accessions")
                .create("decoy"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg(true)
                .withDescription("path to the output file for " +
                		"identifications from pseudo proteins only")
                .create("outPseudo"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg(true)
                .withDescription("path to the output file for " +
                		"all other identifications")
                .create("outOther"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg(true)
                .withDescription("path to save all parsed data (may be analysed"
                		+ "in the GUI or loaded by inModel later)")
                .create("outModel"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg(true)
                .withDescription("path to a prior saved model to be loaded")
                .create("inModel"));
		
		try {
			CommandLine line = cliParser.parse( options, args );
			
			if ((line.getOptions().length == 0) || line.hasOption("help") ||
					(!line.hasOption("identifications") &&
							!line.hasOption("inModel"))) {
				showHelp = true;
			} else {
				CombineIdentificationResults combiner = null;
				
				if (line.hasOption("inModel")) {
					combiner = loadFromFile(line.getOptionValue("inModel"));  
				} else {
					combiner = new CombineIdentificationResults();
					
					// read in the GFFs of known and pseudo proteins
					if (line.hasOption("known")) {
						for (String fileName : line.getOptionValues("known")) {
							combiner.parseKnownProteinsFromGFF(fileName);
						}
					}
					
					if (line.hasOption("pseudo")) {
						for (String fileName : line.getOptionValues("pseudo")) {
							combiner.parsePseudoProteinsFromGFF(fileName);
						}
					}
				}
				
				// get protein information from fasta file
				if (line.hasOption("fasta")) {
					logger.debug("fastafile: " + line.getOptionValue("fasta"));
					
					combiner.parseProteinSequencesFromFASTA(
							line.getOptionValue("fasta"));
				}
				
				// parse the decoy regex, if given
				if (line.hasOption("decoy")) {
					combiner.setDecoyRegex(line.getOptionValue("decoy"));
				}
				
				
				// now read in the (additional) identification files
				for (String fileName : line.getOptionValues("identifications")) {
					String groupName = "default";
					
					if (fileName.contains(":")) {
						String split[] = fileName.split(":", 2);
						groupName = split[0];
						fileName = split[1];
					}
					
					combiner.parseMzTab(fileName, groupName);
				}
				
				if (combiner == null) {
					logger.error("Results object could not be created.");
				} else {
					if (line.hasOption("outModel")) {
						combiner.saveToFile(line.getOptionValue("outModel"));  
					}
					
					// finally write the information into new GFF files
					if (line.hasOption("outPseudo") || line.hasOption("outOther")) {
						combiner.writeIdentifiedPeptidesToGFF(
								line.getOptionValue("outPseudo"),
								line.getOptionValue("outOther"));
					}
				}
			}
		} catch (ParseException e) {
			logger.error("Error while parsing the command line: " + e.getMessage());
			showHelp = true;
		} catch (MZTabException e) {
			logger.error("Error while parsing mzTab file", e);
		}
		
		if (showHelp) {
			HelpFormatter formatter = new HelpFormatter();
			
			formatter.printHelp(GenomeParser.class.getSimpleName(),
					"This tool reads in the identifications processed by PIA " +
					"and writes the information into GFF files." +
					"\nOptions:",
					options,
					"\nCopyright (C) 2013-2014 Medizinisches Proteom-Center, " +
					"julian.uszkoreit@rub.de" +
					"\nThis is free software; see the source for copying " +
					"conditions. There is ABSOLUTELY NO warranty!",
					true);
		}
	}
}
