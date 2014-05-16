package de.mpc.proteogenomics.pipeline.protein;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * An abstract protein class.
 * 
 * @author julian
 *
 */
public abstract class AbstractProtein {
	
	/** the separator for accession and description */
	public final static String headerSeparator = " "; 
	
	
	/**
	 * Returns the accession of the protein
	 * @return
	 */
	public abstract String getAccession();
	
	
	/**
	 * Returns the description of the protein (might be null)
	 * @return
	 */
	public abstract String getDescription();
	
	
	/**
	 * Returns the genome name of the protein
	 * @return
	 */
	public abstract String getGenomeName();
	
	
	/**
	 * Returns the sequence of the accession
	 * @return
	 */
	public abstract String getSequence();
	
	
	/**
	 * Returns the start position.
	 * @return
	 */
	public abstract Long getStart();
	
	
	/**
	 * Returns the end position.
	 * @return
	 */
	public abstract Long getEnd();
	
	
	/**
	 * Returns true if the protein is translated from the complementary strand.
	 * @return
	 */
	public abstract Boolean getIsComplement();
	
	

	/**
	 * Build an GFF entry line for this protein.
	 * @param protein
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String buildGFFline()
			throws UnsupportedEncodingException  {
		StringBuilder line = new StringBuilder();
		
		line.append(URLEncoder.encode(getGenomeName(), "UTF-8").replace("+", "%20"));
		line.append("\tProteoGenomicsPipeline");
		line.append("\tprotein_coding_primary_transcript\t");
		line.append(getStart());
		line.append('\t');
		line.append(getEnd());
		line.append("\t.\t");	// no score
		
		Boolean isComplement = getIsComplement();
		if (isComplement != null) {
			line.append((!isComplement) ? '+' : '-');
		} else {
			// not known, but relevant
			line.append('.');
		}
		
		line.append('\t');
		line.append('0');	// the phase is 0, as it is already given by the start and end
		line.append('\t');
		
		String name = (getDescription() != null) ?
				getAccession() + " - " + getDescription() :
				getAccession();
		
		line.append("ID=" +
				URLEncoder.encode(getAccession(), "UTF-8").replace("+", "%20") +
				";Name=" +
				URLEncoder.encode(name, "UTF-8").replace("+", "%20"));
		
		return line.toString();
	}
	
	
	/**
	 * Returns a FASTA representation of the protein
	 * @return
	 */
	public String toFastaEntry() {
		StringBuilder entry = new StringBuilder(200);
		
		// the header
		entry.append(">");
		entry.append(getAccession());
		
		entry.append(headerSeparator);
		if (getDescription() != null) {
			entry.append(getDescription());
		}
		
		// the sequence
		int pos = 0;
		String sequence = getSequence();
		while (pos < sequence.length() - 60) {
			entry.append("\n");
			entry.append(sequence.substring(pos, pos+60));
			pos += 60;
		}
		
		entry.append("\n");
		entry.append(sequence.substring(pos));
		return entry.toString();
	}
	
	
	/**
	 * Returns the FASTA entry for a decoy version of this Protein.<br/>
	 * The decoy has a shuffled amino acid sequence of the original sequence
	 * and the header is labelled by "_decoy".<br/>
	 * The shuffling of the sequence is seeded by the hash code of the seqeunce,
	 * leading to same shuffled sequences for same sequences.
	 * @return
	 */
	public String toDecoyEntry() {
		StringBuilder entry = new StringBuilder(200);
		
		// the header
		entry.append(">decoy_");
		entry.append(getAccession());
		entry.append(headerSeparator);
		entry.append("this is a decoy protein");
		entry.append("\n");
		
		// shuffle the sequence
		List<Character> seqList =
				new ArrayList<Character>(getSequence().length());
        for (char c : getSequence().toCharArray()){
        	seqList.add(c);
        }
        Collections.shuffle(seqList, new Random(getSequence().hashCode()));
		
        // write the shuffled sequence
        int pos;
		for (pos = 0; pos < seqList.size(); pos++) {
			entry.append(seqList.get(pos));
			
			if ((pos%60 == 0) && (pos > 0)) {
				entry.append("\n");
			}
		}
		
		return entry.toString();
	}
}
