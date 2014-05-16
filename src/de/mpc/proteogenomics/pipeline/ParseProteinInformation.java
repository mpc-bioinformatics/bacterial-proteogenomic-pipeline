package de.mpc.proteogenomics.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.protein.GenericProtein;
import au.com.bytecode.opencsv.CSVReader;


/**
 * This class is used to parse the track information from a protein FASTA or a
 * TXT/CSV/TSV file into the GFF3 format.
 * 
 * @author julian
 *
 */
public class ParseProteinInformation {
	
	private final static Logger logger = Logger.getLogger(GenomeParser.class);
	
	/** the regular expression to parse the start position from FASTA file */
	private Pattern startRegex;
	
	/** the regular expression to parse the end position from FASTA file */
	private Pattern endRegex;
	
	/** the regular expression to identify a forward read from FASTA file */
	private Pattern forwardRegex;
	
	/** the regular expression to identify a complement read from FASTA file */
	private Pattern complementRegex;
	
	/** the regular expression to identify the accession from FASTA file */
	private Pattern accessionRegex;
	
	/** the regular expression to identify the description from FASTA file */
	private Pattern descriptionRegex;
	
	/** the row for CSV/TSV parsing with the accession */
	private Integer rowNr_accession;
	
	/** the row for CSV/TSV parsing with the description */
	private Integer rowNr_description;
	
	/** the row for CSV/TSV parsing with the genome name */
	private Integer rowNr_genomeName;
	
	/** the row for CSV/TSV parsing with the start position */
	private Integer rowNr_start;
	
	/** the row for CSV/TSV parsing with the end position */
	private Integer rowNr_end;
	
	/** the row for CSV/TSV parsing with the strand */
	private Integer rowNr_strand;
	
	/** the separator for CSV/TSV parsing, defaults to TAB */
	private Character separator;
	
	
	public ParseProteinInformation() {
		this.startRegex = null;
		this.endRegex = null;
		this.forwardRegex = null;
		this.complementRegex = null;
		this.accessionRegex = null;
		this.descriptionRegex = null;
		
		this.rowNr_accession = null;
		this.rowNr_description = null;
		this.rowNr_genomeName = null;
		this.rowNr_start = null;
		this.rowNr_end = null;
		this.rowNr_strand = null;
		this.separator = '\t';
	}
	
	
	/**
	 * Constructor for a parser, which should read a FASTA file
	 * @param start
	 * @param end
	 * @param forward
	 * @param complement
	 * @param accession
	 * @param description
	 */
	public ParseProteinInformation(String start, String end,
			String forward, String complement, String accession,
			String description) {
		this();
		
		this.startRegex = Pattern.compile(start);
		this.endRegex = Pattern.compile(end);
		this.forwardRegex = Pattern.compile(forward);
		this.complementRegex = Pattern.compile(complement);
		this.accessionRegex = Pattern.compile(accession);
		this.descriptionRegex = Pattern.compile(description);
	}
	
	/**
	 * Constructor for a parser, which should read a genome mapping file
	 * 
	 * @param start
	 * @param end
	 * @param direct
	 * @param complement
	 * @param accession
	 * @param description
	 */
	public ParseProteinInformation(Integer accessionRow,
			Integer descriptionRow, Integer genomeRow, Integer startRow,
			Integer endRow, Integer strandRow) {
		this();
		
		this.rowNr_accession = accessionRow;
		this.rowNr_description = descriptionRow;
		this.rowNr_genomeName = genomeRow;
		this.rowNr_start = startRow;
		this.rowNr_end = endRow;
		this.rowNr_strand = strandRow;
	}
	
	
	/**
	 * Parses the protein information from a FASTA file.
	 * 
	 * @param fastaInfile
	 * @param gffOutFile
	 * @param genomeName
	 * @throws IOException
	 */
	public void parseFASTAFile(String fastaInfile, String gffOutFile,
			String genomeName)
			throws IOException{
		BufferedReader fastaReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fastaInfile)));
		
		BufferedWriter gffWriter =
				new BufferedWriter(new FileWriter(gffOutFile));
		gffWriter.append("##gff-version 3");
		gffWriter.newLine();
		
		logger.info("starting to parse FASTA: " + fastaInfile);
		
		int nr_proteins = 0;
		int nr_error_proteins = 0;
		String line;
		while ((line = fastaReader.readLine()) != null) {
			if (line.startsWith(">")) {
				// a new header, process the full protein now
				GenericProtein protein =
						createProteinFromFASTA(line, genomeName, false);
				if (protein != null) {
					gffWriter.append(protein.buildGFFline());
					gffWriter.newLine();
					nr_proteins++;
				} else {
					nr_error_proteins++;
				}
			}
		}
		
		logger.info("parsed " + nr_proteins + " proteins");
		if (nr_error_proteins > 0) {
			logger.info("could not parse " + nr_error_proteins + " proteins");
		}
		
		logger.info("protein information in GFF format written to " +
				gffOutFile);
		gffWriter.close();
		
		fastaReader.close();
	}
	
	
	/**
	 * Tests the parsing of the first lines in the txtInfile and returns a list
	 * of created proteins.
	 * 
	 * @param txtInfile
	 * @param lines
	 * @return
	 */
	public List<GenericProtein> testFASTAFile(String fastaInfile,
			String genomeName, int nrEntries)
			throws IOException {
		List<GenericProtein> proteinList =
				new ArrayList<GenericProtein>(nrEntries);
		
		BufferedReader fastaReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fastaInfile)));
		
		String line;
		while (((line = fastaReader.readLine()) != null) &&
				(proteinList.size() < nrEntries)) {
			if (line.startsWith(">")) {
				GenericProtein protein = 
						createProteinFromFASTA(line, genomeName, true);
				
				if (protein != null) {
					proteinList.add(protein);
				}
			}
		}
		
		fastaReader.close();
		return proteinList;
	}
	
	
	public void setRowNrAccession(Integer row) {
		rowNr_accession = row;
	}
	
	
	public void setRowNrDescription(Integer row) {
		rowNr_description = row;
	}
	
	
	public void setRowNrGenomeName(Integer row) {
		rowNr_genomeName = row;
	}
	
	
	public void setRowNrStart(Integer row) {
		rowNr_start = row;
	}
	
	
	public void setRowNrEnd(Integer row) {
		rowNr_end = row;
	}
	
	
	public void setRowNrStrand(Integer row) {
		rowNr_strand = row;
	}
	
	
	public void setSeparator(Character separator) {
		this.separator = separator;
	}
	
	
	/**
	 * Parses the protein information from a CSV/TSV file.
	 * 
	 * @param fastaInfile
	 * @param gffOutFile
	 * @param decoyFilename
	 * @throws IOException 
	 */
	public void parseTXTFile(String txtInfile, String gffOutFile)
			throws IOException {
		BufferedWriter gffWriter =
				new BufferedWriter(new FileWriter(gffOutFile));
		gffWriter.append("##gff-version 3");
		gffWriter.newLine();
		
		logger.info("starting to parse TXT (CSV/TSV) file: " + txtInfile);
		
		int nr_proteins = 0;
		int nr_error_proteins = 0;
		CSVReader reader = new CSVReader(new FileReader(txtInfile), separator,
				'"', 0);
		
		for (String[] line = reader.readNext();
				line != null; line = reader.readNext()) {
			if (line.length > 1) {
				GenericProtein protein = createProteinFromTXTLine(line);
				
				if (protein != null) {
					gffWriter.append(protein.buildGFFline());
					gffWriter.newLine();
					nr_proteins++;
				} else {
					nr_error_proteins++;
				}
			}
		}
		
		logger.info("parsed " + nr_proteins + " proteins");
		if (nr_error_proteins > 0) {
			logger.info("could not parse " + nr_error_proteins + " proteins");
		}
		logger.info("protein information in GFF format written to " +
				gffOutFile);
		gffWriter.close();
		
		reader.close();
	}
	
	
	
	/**
	 * Tests the parsing of the first lines in the txtInfile and returns a list
	 * of created proteins.
	 * 
	 * @param txtInfile
	 * @param lines
	 * @return
	 */
	public List<GenericProtein> testTXTFile(String txtInfile, int lines)
			throws IOException, UnsupportedOperationException {
		List<GenericProtein> proteinList = new ArrayList<GenericProtein>(lines);
		
		CSVReader reader =
				new CSVReader(new FileReader(txtInfile), separator, '"', 0);
		
		int lineCount = 0;
		for (String[] line = reader.readNext();
				(line != null) && (lineCount < lines);
				line = reader.readNext(), lineCount++) {
			
			GenericProtein protein = createProteinFromTXTLine(line);
			
			if (protein != null) {
				proteinList.add(protein);
			}
		}
		
		reader.close();
		return proteinList;
	}
	
	
	
	/**
	 * Creates a {@link GenericProtein} from the tokenized line of a TXT file.
	 * @param line
	 * @return
	 */
	private GenericProtein createProteinFromTXTLine(String[] line) {
		if (line.length < 2) {
			return null;
		}
		
		String accession = null;
		if (line.length >= rowNr_accession) {
			accession = line[rowNr_accession-1];
		} else {
			logger.error("not enough elements (" + rowNr_accession + "/" +
					line.length + ") to parse accession");
			return null;
		}
		
		String description = null;
		if (line.length >= rowNr_description) {
			description = line[rowNr_description-1];
		} else {
			logger.error("not enough elements (" + rowNr_description + "/" +
					line.length + ") to parse description");
			return null;
		}
		
		String genomeName = null;
		if (line.length >= rowNr_genomeName) {
			genomeName = line[rowNr_genomeName-1];
			
			if (genomeName.trim().length() < 1) {
				genomeName = "Chr";
			}
		} else {
			logger.error("not enough elements (" + rowNr_genomeName + "/" +
					line.length + ") to parse genome name");
			return null;
		}
		
		Long start = null;
		if (line.length >= rowNr_start) {
			try {
				start = Long.parseLong(line[rowNr_start-1]);
			} catch (NumberFormatException e) {
				start = null;
				logger.error("could not parse start: " + line[rowNr_start-1]);
				return null;
			}
		} else {
			logger.error("not enough elements (" + rowNr_start + "/" +
					line.length + ") to parse start");
			return null;
		}
		
		Long end = null;
		if (line.length >= rowNr_end) {
			try {
				end = Long.parseLong(line[rowNr_end-1]);
			} catch (NumberFormatException e) {
				end = null;
				logger.error("could not parse end: " + line[rowNr_end-1]);
				return null;
			}
		} else {
			logger.error("not enough elements (" + rowNr_end + "/" +
					line.length + ") to parse end");
			return null;
		}
		
		Boolean isComplement = null;
		if (line.length >= rowNr_strand) {
			if (line[rowNr_strand-1].equals("+") ||
					line[rowNr_strand-1].equals("plus")) {
				isComplement = false;
			} else if (line[rowNr_strand-1].equals("-") ||
					line[rowNr_strand-1].equals("minus")) {
				isComplement = true;
			}
		} else {
			logger.error("not enough elements (" + rowNr_strand + "/" +
					line.length + ") to parse strand");
			return null;
		}
		
		GenericProtein protein = new GenericProtein(accession, description,
				genomeName, "", start, end, isComplement);
		
		return protein;
	}
	
	
	/**
	 * Creates a protein from a FASTA entry
	 * 
	 * @param header the FASTA header
	 * @param sequence the sequence
	 * @param genomeName the genome name, i.e. the seqid of the reference genome
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private GenericProtein createProteinFromFASTA(String header,
			String genomeName, boolean allowErronous)
			throws UnsupportedEncodingException, IOException {
		// only the headers are needed
		boolean ok = true;
		
		Long start = null;
		Long end = null;
		Boolean isComplement = null;
		String accession = null;
		String description = null;
		
		Matcher matcher;
		
		matcher = startRegex.matcher(header);
		if (matcher.matches()) {
			try {
				start = Long.parseLong(matcher.group(1));
			} catch (NumberFormatException e) {
				ok = false;
				logger.error("Could not parse the start position in " +
						header);
			}
		} else {
			ok = false;
			logger.error("Could not match the start position in " + 
					header);
		}

		matcher = endRegex.matcher(header);
		if (matcher.matches()) {
			try {
				end = Long.parseLong(matcher.group(1));
			} catch (NumberFormatException e) {
				ok = false;
				logger.error("Could not parse the end position in " +
						header);
			}
		} else {
			ok = false;
			logger.error("Could not match the end position in " + 
					header);
		}
		
		matcher = forwardRegex.matcher(header);
		if (matcher.matches()) {
			isComplement = false;
		} else {
			// it must be complement then
			matcher = complementRegex.matcher(header);
			
			if (matcher.matches()) {
				isComplement = true;
			} else {
				logger.info("Could not match the reading direction in " +
						header);
			}
		}
		
		matcher = accessionRegex.matcher(header);
		if (matcher.matches()) {
			accession = matcher.group(1);
		} else {
			ok = false;
			logger.error("Could not match the protein's accession " + 
					header);
		}
		
		matcher = descriptionRegex.matcher(header);
		if (matcher.matches()) {
			description = matcher.group(1);
		} else {
			ok = false;
			logger.error("Could not match the protein's description " + 
					header);
		}
		
		if (ok || allowErronous) {
			return new GenericProtein(accession, description, genomeName,
					"", start, end, isComplement);
		} else {
			return null;
		}
	}
	
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		boolean showHelp = false;
		CommandLineParser cliParser = new GnuParser();
		
		Options options = new Options();
		options.addOption(OptionBuilder
                .withDescription("show help")
                .create("help"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription("the file to parse")
                .create("infile"));
		
		options.addOption(OptionBuilder
				.withArgName("string")
                .hasArg()
                .withDescription("the name of the genome")
                .create("genomename"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription("the GFF file to write")
                .create("outfile"));
		
		options.addOption(OptionBuilder
				.withArgName("regex")
                .hasArg()
                .withDescription("the regular expression for the start " +
                		"position")
                .create("start"));
		
		options.addOption(OptionBuilder
				.withArgName("regex")
                .hasArg()
                .withDescription("the regular expression for the end position")
                .create("end"));
		
		options.addOption(OptionBuilder
				.withArgName("regex")
                .hasArg()
                .withDescription("the regular expression to identify an " +
                		"forward read")
                .create("forward"));
		
		options.addOption(OptionBuilder
				.withArgName("regex")
                .hasArg()
                .withDescription("the regular expression to identify an " +
                		"complement read")
                .create("complement"));
		
		options.addOption(OptionBuilder
				.withArgName("regex")
                .hasArg()
                .withDescription("the regular expression to identify the " +
                		"protein accession")
                .create("accession"));
		
		options.addOption(OptionBuilder
				.withArgName("regex")
                .hasArg()
                .withDescription("the regular expression to identify the " +
                		"protein description")
                .create("description"));
		
		options.addOption(OptionBuilder
				.withArgName("colNr")
                .hasArg()
                .withDescription("the column in a CSV/TSV file which contains " +
                		"the accession")
                .create("accessionCol"));
		
		options.addOption(OptionBuilder
				.withArgName("colNr")
                .hasArg()
                .withDescription("the column in a CSV/TSV file which contains " +
                		"the description")
                .create("descriptionCol"));
		
		options.addOption(OptionBuilder
				.withArgName("colNr")
                .hasArg()
                .withDescription("the column in a CSV/TSV file which contains " +
                		"the genome")
                .create("genomeCol"));
		
		options.addOption(OptionBuilder
				.withArgName("colNr")
                .hasArg()
                .withDescription("the column in a CSV/TSV file which contains " +
                		"the protein start")
                .create("startCol"));
		
		options.addOption(OptionBuilder
				.withArgName("colNr")
                .hasArg()
                .withDescription("the column in a CSV/TSV file which contains " +
                		"the protein end")
                .create("endCol"));
		
		options.addOption(OptionBuilder
				.withArgName("colNr")
                .hasArg()
                .withDescription("the column in a CSV/TSV file which contains " +
                		"the strand")
                .create("strandCol"));
		
		options.addOption(OptionBuilder
				.withArgName("sep")
                .hasArg()
                .withDescription("the separator-character for the TSV/CSV "
                		+ "parsing, defaults to TAB")
                .create("separator"));
		
		try {
			CommandLine line = cliParser.parse( options, args );
			
			if (line.hasOption("infile") && line.hasOption("outfile") &&
					line.hasOption("genomename") &&
					line.hasOption("start") && 
					line.hasOption("end") &&
					line.hasOption("forward") && 
					line.hasOption("complement") &&
					line.hasOption("accession") &&
					line.hasOption("description")) {
				
				ParseProteinInformation parser = new ParseProteinInformation(
						line.getOptionValue("start"),
						line.getOptionValue("end"),
						line.getOptionValue("forward"),
						line.getOptionValue("complement"),
						line.getOptionValue("accession"),
						line.getOptionValue("description"));
				
				parser.parseFASTAFile(line.getOptionValue("infile"),
						line.getOptionValue("outfile"),
						line.getOptionValue("genomename"));
			} else if (line.hasOption("infile") && line.hasOption("outfile") &&
					line.hasOption("accessionCol") &&
					line.hasOption("descriptionCol") && 
					line.hasOption("genomeCol") && 
					line.hasOption("startCol") && 
					line.hasOption("endCol") && 
					line.hasOption("strandCol")) {
				
				ParseProteinInformation parser = new ParseProteinInformation(
						Integer.parseInt(line.getOptionValue("accessionCol")),
						Integer.parseInt(line.getOptionValue("descriptionCol")),
						Integer.parseInt(line.getOptionValue("genomeCol")),
						Integer.parseInt(line.getOptionValue("startCol")),
						Integer.parseInt(line.getOptionValue("endCol")),
						Integer.parseInt(line.getOptionValue("strandCol")));
				
				if (line.hasOption("separator")) {
					parser.setSeparator(line.getOptionValue("separator").charAt(0));
				}
				
				parser.parseTXTFile(line.getOptionValue("infile"),
						line.getOptionValue("outfile"));
			} else {
				showHelp = true;
			}
		} catch (ParseException e) {
			logger.error("Error while parsing the command line: " + e.getMessage());
			showHelp = true;
		} catch (IOException e) {
			logger.error("Error while parsing: " + e.getMessage());
		}
		
		
		if (showHelp) {
			HelpFormatter formatter = new HelpFormatter();
			
			formatter.printHelp(GenomeParser.class.getSimpleName(),
					"Reads in protein information either from a TSV/CSV " +
					"file or a database in FASTA format, which should " +
					"contain the reading frame positions of the " +
					"proteins in the header. With this information, a GFF3 " +
					"file for the \"known proteins\" is created." +
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
