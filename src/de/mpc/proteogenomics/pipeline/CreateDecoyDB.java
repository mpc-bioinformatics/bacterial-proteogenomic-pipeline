package de.mpc.proteogenomics.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;


public class CreateDecoyDB {
	
	private final static Logger logger = Logger.getLogger(CreateDecoyDB.class);
	
	/**
	 * Creates a shuffled decoy entry from the protein given by the header and
	 * the sequence.
	 * <p>
	 * The randomization is seeded by the hash of the sequence.
	 * 
	 * @param header
	 * @param sequence
	 * @return
	 */
	public static String createDecoyEntry(String header, String sequence) {
		if (header.startsWith(">")) {
			header = header.substring(1).trim();
		}
		
		// shuffle the sequence
		List<Character> seqList =
				new ArrayList<Character>(sequence.length());
        for (char c : sequence.toCharArray()){
        	seqList.add(c);
        }
        Collections.shuffle(seqList, new Random(sequence.hashCode()));
		
        // shuffle the sequence
		StringBuilder seq = new StringBuilder(500);
        int pos;
		for (pos = 0; pos < seqList.size(); pos++) {
			seq.append(seqList.get(pos));
		}
		
		return createFormattedEntry("decoy_" + header, seq.toString());
	}
	
	
	/**
	 * Writes a nicely formatted FASTA entry
	 * 
	 * @param header
	 * @param sequence
	 * @return
	 */
	public static String createFormattedEntry(String header, String sequence) {
		StringBuilder entry = new StringBuilder(500);
		if (header.startsWith(">")) {
			header = header.substring(1).trim();
		}
		
		// the header
		entry.append(">");
		entry.append(header);
		
		// the sequence
		int pos = 0;
		while (pos < sequence.length() - 60) {
			entry.append("\n");
			entry.append(sequence.substring(pos, pos+60));
			pos += 60;
		}
		
		entry.append("\n");
		entry.append(sequence.substring(pos));
		return entry.toString();
	}
	
	
	
	public static void createDecoyDatabase(List<String> fastaInfiles,
			String fastaOutfile, String fastaDecoyOutfile) throws IOException {
		long nrProteins = 0;
		
		BufferedWriter fastaWriter = null;
		if (fastaOutfile != null) {
			fastaWriter =
					new BufferedWriter(new FileWriter(fastaOutfile));
		}
		
		BufferedWriter decoyWriter = null;
		if (fastaDecoyOutfile != null) {
			decoyWriter =
					new BufferedWriter(new FileWriter(fastaDecoyOutfile));
		}
		
		for (String inFile : fastaInfiles) {
			BufferedReader fastaReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(inFile)));
			
			logger.info("Reading in proteins from " + inFile);
			
			StringBuilder sequence = new StringBuilder(500);;
			String header = null;
			String line;
			while ((line = fastaReader.readLine()) != null) {
				if (line.startsWith(">")) {
					if (header != null) {
						String decoyEntry =
								createDecoyEntry(header, sequence.toString());
						
						nrProteins++;
						
						if (fastaWriter != null) {
							fastaWriter.append(createFormattedEntry(header,
									sequence.toString()));
							fastaWriter.append("\n");
							fastaWriter.append(decoyEntry);
							fastaWriter.append("\n");
						}
						if (decoyWriter != null) {
							decoyWriter.append(decoyEntry);
							decoyWriter.append("\n");
						}
					}
					
					header = line.substring(1);
					sequence = new StringBuilder(500);
				} else {
					sequence.append(line.trim());
				}
			}
			
			// the last entry in the file
			if (header != null) {
				String decoyEntry =
						createDecoyEntry(header, sequence.toString());
				
				nrProteins++;
				
				if (fastaWriter != null) {
					fastaWriter.append(createFormattedEntry(header,
							sequence.toString()));
					fastaWriter.append("\n");
					fastaWriter.append(decoyEntry);
					fastaWriter.append("\n");
				}
				if (decoyWriter != null) {
					decoyWriter.append(decoyEntry);
					decoyWriter.append("\n");
				}
			}
			
			fastaReader.close();
		}
		
		logger.info("Read " + nrProteins + " proteins.");
		
		if (fastaWriter != null) {
			logger.info("Targets and decoys written to  " + fastaOutfile);
			fastaWriter.close();
		}
		if (decoyWriter != null) {
			logger.info("Only decoys written to  " + fastaDecoyOutfile);
			decoyWriter.close();
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
                .hasArgs()
                .withDescription("input FASTA file to parse, may be called " +
                		"several times to cerate a concatenated output decoy " +
                		"FASTA")
                .create("infile"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription("output FASTA file containing the target " +
                		"and decoy entries")
                .create("outfile"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription("output FASTA file containing only the " +
                		"decoy files")
                .create("outDecoy"));
		
		
		try {
			CommandLine line = cliParser.parse( options, args );
			
			if ((line.getOptions().length == 0) || line.hasOption("help") ||
					!line.hasOption("infile")) {
				showHelp = true;
			} else {
				List<String> inFiles = new ArrayList<String>();
				for (String file : line.getOptionValues("infile")) {
					inFiles.add(file);
				}
				
				CreateDecoyDB.createDecoyDatabase(inFiles,
						line.getOptionValue("outfile"),
						line.getOptionValue("outDecoy"));
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
					"This tool creates a decoy database from one or more " +
					"target FASTA databases." +
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
