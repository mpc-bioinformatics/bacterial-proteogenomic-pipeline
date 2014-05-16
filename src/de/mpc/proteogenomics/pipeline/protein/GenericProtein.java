package de.mpc.proteogenomics.pipeline.protein;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class GenericProtein extends AbstractProtein {

	/** the accession of the protein */
	private String accession;
	
	/** the description of the protein */
	private String description;
	
	/** the genome name of the protein */
	private String genomeName;
	
	/** the sequence of the protein */
	private String sequence;
	
	/** the start position in the genome */
	private Long start;
	
	/** the end position in the genome */
	private Long end;
	
	/** whether the protein is translated from the direct or the complementary strand */
	private Boolean isComplement;
	
	
	public GenericProtein(String accession, String description,
			String genomeName, String sequence) {
		this.accession = accession;
		this.description = description;
		this.genomeName = genomeName;
		this.sequence = sequence;
		this.start = null;
		this.end = null;
		this.isComplement = null;
	}
	
	
	public GenericProtein(String accession, String description,
			String genomeName, String sequence, Long start, Long end,
			Boolean complement) {
		this.accession = accession;
		this.description = description;
		this.genomeName = genomeName;
		this.sequence = sequence;
		this.start = start;
		this.end = end;
		this.isComplement = complement;
	}
	
	
	/**
	 * Generates a {@link GenericProtein} from a GFF line, using as header
	 * either the matched pattern given, e.g. to only get the accession, or the
	 * whole ID entry in the line.
	 * 
	 * @param gffLine
	 * @param accessionPattern
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static GenericProtein parseFromGFFLine(String gffLine)
			throws UnsupportedEncodingException {
		String[] values = gffLine.split("\t");
		
		Pattern idPattern = Pattern.compile(".*ID=([^;]*);*.*");
		Pattern namePattern = Pattern.compile(".*Name=([^;]*);*.*");
		
		Matcher m;
		
		if (values.length >= 8) {
			m = idPattern.matcher(values[8]);
			if (m.matches()) {
				String genome = URLDecoder.decode(values[0], "UTF-8");
				
				String accession = URLDecoder.decode(m.group(1), "UTF-8");
				
				String description = null;
				m = namePattern.matcher(values[8]);
				if (m.matches()) {
					description = URLDecoder.decode(m.group(1), "UTF-8");
					
					if (description.startsWith(accession + " - ")) {
						description =
								description.substring(accession.length() + 3);
					}
				}
				
				Long start;
				try {
					start = Long.parseLong(values[3]);
				} catch (NumberFormatException e) {
					start = null;
				}
				
				Long end;
				try {
					end = Long.parseLong(values[4]);
				} catch (NumberFormatException e) {
					end = null;
				}
				
				Boolean complement = null;
				if (values[6].equals("+")) {
					complement = false;
				} else if (values[6].equals("-")) {
					complement = true;
				}
				
				GenericProtein protein = new GenericProtein(accession,
						description, genome, "", start, end, complement);
				
				return protein;
			}
		}
		
		return null;
	}
	
	
	@Override
	public String getAccession() {
		return accession;
	}
	
	
	public void setAccession(String accession) {
		this.accession = accession;
	}
	
	
	@Override
	public String getDescription() {
		return description;
	}
	
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	@Override
	public String getGenomeName() {
		return genomeName;
	}
	
	
	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}
	
	
	@Override
	public String getSequence() {
		return sequence;
	}
	
	
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	
	@Override
	public Long getStart() {
		return start;
	}
	
	
	public void setStart(Long start) {
		this.start = start;
	}
	
	
	@Override
	public Long getEnd() {
		return end;
	}
	
	
	public void setEnd(Long end) {
		this.end = end;
	}
	
	
	@Override
	public Boolean getIsComplement() {
		return isComplement;
	}
	
	
	/**
	 * Reads in the proteins from the given GFF file.
	 * 
	 * @param fileName name of the GFF file
	 * @param proteins map for the proteins (must be intialised)
	 * @param logger optional logger (may be null)
	 * @return number of parsed proteins
	 * @throws IOException
	 */
	public static int parseProteinsFromGFF(String fileName,
			Map<String, GenericProtein> proteins, Logger logger)
			throws IOException {
		BufferedReader gffReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName)));
		String line;
		int nr_parsed = 0;
		
		// check the first line
		line = gffReader.readLine();
		if ((logger != null) && 
				((line == null) ||
						!((line.startsWith("##") &&
								line.contains("gff-version"))))) {
			logger.info("Are you sure, this is a GFF file?");
		}
		
		while ((line = gffReader.readLine()) != null) {
			if (!line.startsWith("#")) {
				GenericProtein protein = parseFromGFFLine(line);
				
				if (protein != null) {
					proteins.put(protein.getAccession(), protein);
					nr_parsed++;
				} else {
					if (logger != null) {
						logger.warn("could not parse protein from " + line);
					}
				}
			}
		}
		gffReader.close();
		
		return nr_parsed;
	}
}