package de.mpc.proteogenomics.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import de.mpc.proteogenomics.pipeline.protein.GenomeTranslatedProtein;



public class GenomeParser {
	
	private final static Logger logger = Logger.getLogger(GenomeParser.class);
	
	/** the name of the FASTA file, containing the whole genome */
	private String genomeFileName;
	
	/** the minimal reported protein length */
	int min_protein_length;
	
	/** the name of the genome, either given by command line or the header up to the first space*/
	String genomeName;
	
	/** width of the protein position hashes */
	public static Long default_hashWidth = 100000L;
	
	
	/**
	 * Basic constructor
	 * @param genomeFileName
	 * @param minimalProteinLength
	 * @param genomeName
	 */
	public GenomeParser(String genomeFileName, int minimalProteinLength, String genomeName) {
		this.genomeFileName = genomeFileName;
		this.min_protein_length = minimalProteinLength;
		this.genomeName = genomeName;
	}
	
	
	/**
	 * Go through the genome FASTA file and parse the proteins.
	 * 
	 * @return
	 */
	public long parseGenome(String outputFileName, String outputGFFBaseName,
			String proteinsFile) throws IOException {
		long nrParsedProteins = 0;
		long position = 0;	// current position in the genome
		StringBuffer codon[];					/*< the current codon, in its frame */
		GenomeTranslatedProtein protein[];		/*< the currently parsed protein, in its frame*/
		GenomeTranslatedProtein foundORF[];		/*< whether an ORF was found in the frame and the resulting protein (first AA translates to M) */
		Character firstNucleotides[];
		GenomeTranslatedProtein firstProtein[];	/*< the first protein in the genome, may be extended due to the circular genome */
		GenomeTranslatedProtein firstORF[];		/*< the first ORF in the genome, may be extended due to the circular genome */
		
		long extra_short_proteins = 0;
		long extra_long_proteins = 0;
		long length_longest_protein = 0;
		long proteins_with_orf = 0;
		long known_proteins = 0;
		long nr_entries = 0;
		
		// get the known proteins, if a file is given
		Map<Long, List<GenericProtein>> knownProteins;
		if (proteinsFile != null) {
			knownProteins = parseProteinsFromGFF(proteinsFile, default_hashWidth);
		} else {
			knownProteins = new HashMap<Long, List<GenericProtein>>();
		}
		
		codon = new StringBuffer[6];
		protein = new GenomeTranslatedProtein[6];
		foundORF = new GenomeTranslatedProtein[6];
		firstProtein = new GenomeTranslatedProtein[6];
		firstORF = new GenomeTranslatedProtein[6];
		for (int frame=0; frame < 6; frame++) {
			codon[frame] = null;
			protein[frame] = null;
			foundORF[frame] = null;
			firstProtein[frame] = null;
			firstORF[frame] = null;
		}
		firstNucleotides = new Character[3];
		
		BufferedReader  br = new BufferedReader(
				new InputStreamReader(new FileInputStream(genomeFileName)));
		
		BufferedWriter fastaWriter = null;
		if (outputFileName != null) {
			fastaWriter =
					new BufferedWriter(new FileWriter(outputFileName));
		}
		
		BufferedWriter gffFrameWriters[] = new BufferedWriter[6];
		if (outputGFFBaseName != null) {
			for (int frame=0; frame < 6; frame++) {
				gffFrameWriters[frame] = 
						new BufferedWriter(new FileWriter(outputGFFBaseName +
								"_frame" + frame + ".gff"));
				gffFrameWriters[frame].append("##gff-version 3");
				gffFrameWriters[frame].newLine();
			}
		}
		
		if (genomeName != null) {
			logger.info("Using '" + genomeName + "' as genome name");
		}
		
		logger.info("start parsing " + genomeFileName);
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith(">")) {
				// this is the FASTA header
				if (genomeName == null) {
					Matcher matcher =
							Pattern.compile("^>\\s*([^\\s]+).*?").matcher(line);
					
					if (matcher.matches()) {
						genomeName = matcher.group(1);
						logger.info("Assuming '" + genomeName +
								"' as genome name");
					}
				}
				
				continue;
			}
			
			// go through the line's characters
			for (int idx=0; idx < line.length(); idx++) {
				Character nucleotide = line.charAt(idx);
				if (position < 3) {
					firstNucleotides[(int)position] = nucleotide;
				}
				position++;
				
				for (int frame=0; frame < 6; frame++) {
					
					if ((codon[frame] == null) && (position-1 >= frame % 3)) {
						// initialise the codon for the frame shift and the first protein
						codon[frame] = new StringBuffer(3);
						
						protein[frame] = new GenomeTranslatedProtein(genomeName,
								position, frame);
					}
					
					if ((codon[frame] != null) && (codon[frame].length() < 3)) {
						if (frame < 3) {
							// the forward frames
							codon[frame].append(nucleotide);
						} else {
							// the backward frames
							codon[frame].insert(0, 
									GeneticCode.getComplementaryNucleotide(nucleotide));
						}
					}
					
					if ((codon[frame] != null) &&
							(codon[frame].length() == 3)) {
						Character aminoacid = GeneticCode.nt2aa(codon[frame].toString());
						
						if ((aminoacid != null) && !aminoacid.equals('*')) {
							// the found amino acid is not a stop codon
							//   -> append it
							protein[frame].appendAminoAcid(aminoacid);
							if ((frame < 3) && (foundORF[frame] != null)) {
								// grow the forward-ORF
								foundORF[frame].appendAminoAcid(aminoacid);
							}
							
							if (GeneticCode.isStartCodon(codon[frame].toString())) {
								// a start codon is found 
								
								if ((frame < 3) && (foundORF[frame] == null)) {
									// for a forward strand: start new protein
									// here with sequence M
									foundORF[frame] =
											new GenomeTranslatedProtein(
													genomeName,
													position-2,
													frame);
									foundORF[frame].setIsORFProtein(true);
									foundORF[frame].appendAminoAcid('M');
								} else if (frame >= 3) {
									// for a backward strand, we need the
									// sequence up to the last amino acid
									foundORF[frame] =
											new GenomeTranslatedProtein(
													genomeName,
													protein[frame].getStart(),
													frame);
									foundORF[frame].setIsORFProtein(true);
									for (Character aa
											: protein[frame].getSequence().substring(0, protein[frame].length()-1).toCharArray()) {
										foundORF[frame].appendAminoAcid(aa);
									}
									foundORF[frame].appendAminoAcid('M');
									foundORF[frame].setEnd(position);
								}
							}
						} else if ((aminoacid != null) && aminoacid.equals('*')) {
							int proteinLength = protein[frame].length();
							
							if (frame < 3) {
								// the protein ends with the stop codon
								protein[frame].setEnd(position);
								
								if (foundORF[frame] != null) {
									foundORF[frame].setEnd(position);
								}
							} else {
								// the protein started before the stop codon
								protein[frame].setEnd(position-3);
								
								protein[frame].reverseProtein();
								if (foundORF[frame] != null) {
									foundORF[frame].reverseProtein();
								}
							}
							
							if (firstProtein[frame] != null) {
								// this is not the first protein / stop codon
								if (proteinLength > 500) {
									extra_long_proteins++;
								} else if (proteinLength < min_protein_length) {
									extra_short_proteins++;
								}
								
								if (proteinLength > length_longest_protein) {
									length_longest_protein = proteinLength;
								}
								
								nrParsedProteins++;
								
								if (proteinLength >= min_protein_length) {
									if ((foundORF[frame] == null) ||
											!protein[frame].getSequence().equals(foundORF[frame].getSequence())) {
										if (checkAndWriteProtein(protein[frame],
												knownProteins, default_hashWidth,
												fastaWriter, gffFrameWriters[frame])) {
											nr_entries++;
										} else {
											known_proteins++;
										}
									}
									
									if ((foundORF[frame] != null) && 
											foundORF[frame].length() >= min_protein_length) {
										if (checkAndWriteProtein(foundORF[frame],
												knownProteins, default_hashWidth,
												fastaWriter, gffFrameWriters[frame])) {
											nr_entries++;
											proteins_with_orf++;
										} else {
											known_proteins++;
										}
									}
								}
							} else {
								// the first protein is cached for later
								// processing due to the circular genome
								firstProtein[frame] = protein[frame];
								firstORF[frame] = foundORF[frame];
							}
							
							foundORF[frame] = null;
							if (frame < 3) {
								protein[frame] = new GenomeTranslatedProtein(
										genomeName, position + 1, frame);
							} else {
								// the protein should later stop with the stop codon
								protein[frame] = new GenomeTranslatedProtein(
										genomeName, position - 2, frame);
							}
						} else {
							logger.error("could not generate amino acid for " +
									codon[frame].toString());
							br.close();
							return -1;
						}
						
						// clear the codon
						codon[frame].delete(0, 3);
					}
				}
			}
		}
		br.close();
		
		for (int frame=0; frame < 6; frame++) {
			// finalize the last and first proteins
			
			int combineFrame  = (3 - codon[frame].length()) % 3;
			if (frame >= 3) {
				combineFrame += 3;
			}
			
			if (codon[frame].length() > 0) {
				// fill the codon to get the overlap-aminoacid
				long overlapPosition;
				for (overlapPosition = 0;
						codon[frame].length() < 3;
						overlapPosition++) {
					if (frame < 3) {
						// the forward frames
						codon[frame].append(firstNucleotides[(int)overlapPosition]);
					} else {
						// the backward frames
						codon[frame].insert(0, 
								GeneticCode.getComplementaryNucleotide(firstNucleotides[(int)overlapPosition]));
					}
				}
				
				Character overlapAminoacid =
						GeneticCode.nt2aa(codon[frame].toString());
				
				if ((overlapAminoacid != null) && !overlapAminoacid.equals('*')) {
					// the found amino acid is not a stop codon
					//   -> append the amino acid and the firstProtein
					protein[frame].appendAminoAcid(overlapAminoacid);
					
					if ((frame < 3) && (foundORF[frame] != null)) {
						// grow the forward-ORF
						foundORF[frame].appendAminoAcid(overlapAminoacid);
					}
					
					if (GeneticCode.isStartCodon(codon[frame].toString())) {
						// a start codon is found 
						
						if ((frame < 3) && (foundORF[frame] == null)) {
							// for a forward strand: start new ORF protein
							// here with sequence M
							foundORF[frame] =
									new GenomeTranslatedProtein(
											genomeName,
											overlapPosition-2,
											combineFrame);
							foundORF[frame].setIsORFProtein(true);
							foundORF[frame].appendAminoAcid('M');
						} else if (frame >= 3) {
							// for a backward strand, we need the sequence up to
							// the last amino acid
							foundORF[frame] =
									new GenomeTranslatedProtein(
											genomeName,
											protein[frame].getStart(),
											combineFrame);
							foundORF[frame].setIsORFProtein(true);
							for (Character aa
									: protein[frame].getSequence().substring(0, protein[frame].length()-1).toCharArray()) {
								foundORF[frame].appendAminoAcid(aa);
							}
							foundORF[frame].appendAminoAcid('M');
							foundORF[frame].setEnd(overlapPosition);
						}
					}
				} else if ((overlapAminoacid != null) &&
						overlapAminoacid.equals('*')) {
					// the protein ends in the overlap
					int proteinLength = protein[frame].length();
					
					if (frame < 3) {
						// the protein ends with the stop codon
						protein[frame].setEnd(position + overlapPosition);
						
						if (foundORF[frame] != null) {
							foundORF[frame].setEnd(position + overlapPosition);
						}
					} else {
						// the pseudo protein started before the stop codon
						protein[frame].setEnd(position + frame - 6 );
						
						protein[frame].reverseProtein();
						if (foundORF[frame] != null) {
							foundORF[frame].reverseProtein();
						}
					}
					
					if (proteinLength > 500) {
						extra_long_proteins++;
					} else if (proteinLength < min_protein_length) {
						extra_short_proteins++;
					}
					
					if (proteinLength > length_longest_protein) {
						length_longest_protein = proteinLength;
					}
					
					nrParsedProteins++;
					
					if (proteinLength >= min_protein_length) {
						if ((foundORF[frame] == null) ||
								!protein[frame].getSequence().equals(foundORF[frame].getSequence())) {
							if (checkAndWriteProtein(protein[frame],
									knownProteins, default_hashWidth,
									fastaWriter, gffFrameWriters[frame])) {
								nr_entries++;
							} else {
								known_proteins++;
							}
						}
						
						if ((foundORF[frame] != null) && 
								foundORF[frame].length() >= min_protein_length) {
							if (checkAndWriteProtein(foundORF[frame],
									knownProteins, default_hashWidth,
									fastaWriter, gffFrameWriters[frame])) {
								nr_entries++;
								proteins_with_orf++;
							} else {
								known_proteins++;
							}
						}
					}
					
					// the protein ended, do not concatenate it with the firstProtein
					protein[frame] = null;
					foundORF[frame] = null;
				} else {
					logger.error("could not generate amino acid for " +
							codon[frame].toString());
					br.close();
					return -1;
				}
			}
			
			if (protein[frame] != null) {
				// the protein did not end in the overlap, concatenate it
				if (frame >= 3) {
					firstProtein[combineFrame].reverseProtein();
					if (firstORF[combineFrame] != null) {
						firstORF[combineFrame].reverseProtein();
					}
				}
				
				for (Character aa
						: firstProtein[combineFrame].getSequence().toCharArray()) {
					protein[frame].appendAminoAcid(aa);
					
					if ((foundORF[frame] != null) && (frame < 3)) {
						foundORF[frame].appendAminoAcid(aa);
					}
				}
				
				protein[frame].setEnd(firstProtein[combineFrame].getEnd());
				protein[frame].recalculateStartForOverlap();
				if (foundORF[frame] != null) {
					foundORF[frame].setEnd(firstProtein[combineFrame].getEnd());
					foundORF[frame].recalculateStartForOverlap();
				} else if (firstORF[combineFrame] != null) {
					foundORF[frame] = firstORF[combineFrame]; 
				}
				
				if ((frame >= 3) && (firstORF[combineFrame] != null)) {
					// there is a big reverse strand ORF
					
					foundORF[frame] =
							new GenomeTranslatedProtein(
									genomeName,
									protein[frame].getStart(),
									combineFrame);
					foundORF[frame].setIsORFProtein(true);
					
					
					
					for (Character aa
							: protein[frame].getSequence().substring(
									0, protein[frame].length() - firstProtein[combineFrame].length()).
									toCharArray()) {
						foundORF[frame].appendAminoAcid(aa);
					}
					for (Character aa
							: firstORF[combineFrame].getSequence().toCharArray()) {
						foundORF[frame].appendAminoAcid(aa);
					}
					
					foundORF[frame].setEnd(firstORF[combineFrame].getEnd());
					foundORF[frame].recalculateStartForOverlap();
				}
				
				if (frame >= 3) {
					protein[frame].reverseProtein();
					if (foundORF[frame] != null) {
						foundORF[frame].reverseProtein();
					}
				}
			} else {
				// the protein was finished in the overlap -> write the combineFrame's protein/ORF now
				protein[frame] = firstProtein[combineFrame]; 
				foundORF[frame] = firstORF[combineFrame];
			}
			
			
			int proteinLength = protein[frame].length();
			if (proteinLength > 0) {
				if (proteinLength > 500) {
					extra_long_proteins++;
				} else if (proteinLength < min_protein_length) {
					extra_short_proteins++;
				}
				
				if (proteinLength > length_longest_protein) {
					length_longest_protein = proteinLength;
				}
				
				nrParsedProteins++;
				
				if (proteinLength >= min_protein_length) {
					if ((foundORF[frame] == null) ||
							!protein[frame].getSequence().equals(foundORF[frame].getSequence())) {
						if (checkAndWriteProtein(protein[frame],
								knownProteins, default_hashWidth,
								fastaWriter, gffFrameWriters[frame])) {
							nr_entries++;
						} else {
							known_proteins++;
						}
					}
					
					if ((foundORF[frame] != null) && 
							foundORF[frame].length() >= min_protein_length) {
						if (checkAndWriteProtein(foundORF[frame],
								knownProteins, default_hashWidth,
								fastaWriter, gffFrameWriters[frame])) {
							nr_entries++;
							proteins_with_orf++;
						} else {
							known_proteins++;
						}
					}
				}
			}
		}
		
		if (fastaWriter != null) {
			fastaWriter.close();
			logger.info("pseudo proteins written to " + outputFileName +
					" in FASTA format");
		}
		for (BufferedWriter bw : gffFrameWriters) {
			if (bw != null) {
				bw.close();
			}
		}
		
		logger.info("#parsed pseudo-proteins: " + nrParsedProteins);
		logger.info("#proteins longer 500 aa: " + extra_long_proteins);
		logger.info("#proteins shorter " + min_protein_length + " aa (NOT written to files): " +
				extra_short_proteins);
		logger.info("longest protein: " + length_longest_protein);
		logger.info("#proteins with ORF: " + proteins_with_orf);
		logger.info("#proteins left out, because they are known: " + known_proteins);
		logger.info("#processed FASTA entries (without decoys): " + nr_entries);
		
		return nrParsedProteins;
	}
	
	
	/**
	 * Parses the proteins from a file and returns a hash map, mapping from a
	 * hash of the genome position to the proteins in this hash-area. The
	 * hash-areas are the genome positions spanned divided by the hashWidth.
	 * 
	 * @param proteinsFile
	 * @return
	 * @throws IOException 
	 */
	private Map<Long, List<GenericProtein>> parseProteinsFromGFF(
			String proteinsFile, Long hashWidth)
			throws IOException, NumberFormatException {
		
		//  100k base-hash of the proteins
		Map<Long, List<GenericProtein>> hashProteins =
				new HashMap<Long, List<GenericProtein>>();
		
		BufferedReader gffReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(proteinsFile)));
		
		logger.info("start parsing " + genomeFileName);
		String line;
		int nr_proteins = 0;
		
		// check the first line
		line = gffReader.readLine();
		if ((line == null) ||
				!((line.startsWith("##") && line.contains("gff-version")))) {
			logger.info("Are you sure, this is a GFF file?");
		}
		
		while ((line = gffReader.readLine()) != null) {
			if (!line.startsWith("#")) {
				GenericProtein protein =
						GenericProtein.parseFromGFFLine(line);
				
				if ((protein != null) &&
						(protein.getStart() != null) && (protein.getEnd() != null)) {
					Long startHash = protein.getStart() / hashWidth;
					Long endHash = protein.getEnd() / hashWidth;
					
					for (Long h = startHash; h <= endHash; h++) {
						List<GenericProtein> protList = hashProteins.get(h);
						if (protList == null) {
							protList = new ArrayList<GenericProtein>();
							hashProteins.put(h, protList);
						}
						protList.add(protein);
					}
					
					nr_proteins++;
				}
			}
		}
		gffReader.close();
		logger.info("got " + nr_proteins + " known proteins");
		
		return hashProteins;
	}
	
	
	/**
	 * Checks whether the protein is a known database protein in the
	 * knownProteins hash map. If it is not, write the protein to the FASTA, 
	 * GFF and FASTA-decoy file.<br/>
	 * 
	 * @param protein
	 * @param knownProteins
	 * @param hashWidth
	 * @param fastaWriter
	 * @param gffFrameWriter
	 * @param decoyWriter
	 * @return true, if the protein was valid to be written to the files
	 * @throws IOException
	 */
	private boolean checkAndWriteProtein(GenomeTranslatedProtein protein,
			Map<Long, List<GenericProtein>> knownProteins, Long hashWidth,
			BufferedWriter fastaWriter, BufferedWriter gffFrameWriter)
			throws IOException {
		
		protein.checkProteinRelations(knownProteins, hashWidth);
		
		if (!protein.isInKnownProtein()) {
			if (fastaWriter != null) {
				fastaWriter.write(protein.toFastaEntry());
				fastaWriter.newLine();
			}
			
			if (gffFrameWriter != null) {
				gffFrameWriter.append(
						protein.buildGFFline());
				gffFrameWriter.newLine();
			}
						
			return true;
		} else {
			return false;
		}
	}
	
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {	
		boolean showHelp = false;
		CommandLineParser cliParser = new GnuParser();
		
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("help")
                .withDescription("show help")
                .create("help"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription("the genomic sequence in FASTA format")
                .create("infile"));
		
		options.addOption(OptionBuilder
				.withArgName("string")
                .hasArg()
                .withDescription("the genome name (if left blank, it will be " +
                		"guessed)")
                .create("genomeName"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription( "output FASTA file for the pseudo proteins" )
                .create("outfasta"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription( "the base name for output GFF files, each " +
                		"frame gets one file, if not given, the GFF files " +
                		"will not be created" )
                .create("outgff"));
		
		options.addOption(OptionBuilder
				.withArgName("filename")
                .hasArg()
                .withDescription( "path to a gff file containing the known " +
                		"proteins (could be generated by ProteinFASTAtoGFF), " +
                		"proteins with these start and end positions will " +
                		"not be listed in the new FASTA and GFF files." )
                .create("proteins"));
		
		try {
			CommandLine line = cliParser.parse( options, args );
			
			if ((line.getOptions().length == 0) ||  line.hasOption("help") ||
					!line.hasOption("infile")) {
				showHelp = true;
			} else {
				String genomeName = null;
				if (line.hasOption("genomeName")) {
					genomeName = line.getOptionValue("genomeName");
				}
				
				GenomeParser parser =
						new GenomeParser(line.getOptionValue("infile"), 5,
								genomeName);
				
				parser.parseGenome(
						line.getOptionValue("outfasta"),
						line.getOptionValue("outgff"),
						line.getOptionValue("proteins"));
			}
		} catch (ParseException e) {
			logger.error("Error while parsing the command line: " + e.getMessage());
			showHelp = true;
		} catch (IOException e) {
			logger.error("Error while parsing: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unhandled exception", e);
			System.exit(-1);
		}
		
		if (showHelp) {
			HelpFormatter formatter = new HelpFormatter();
			
			formatter.printHelp(GenomeParser.class.getSimpleName(),
					"This tool parses a complete genome given in FASTA " +
					"format and writes its direct protein translation into a " +
					"FASTA file for database searches and into a GFF file " +
					"for track annotations and later usage with the analysis " +
					"of identified proteins." +
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