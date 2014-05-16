package de.mpc.proteogenomics.pipeline;


public class GeneticCode {
	
	/*
	11. The Bacterial, Archaeal and Plant Plastid Code (transl_table=11)

	TTT F Phe      TCT S Ser      TAT Y Tyr      TGT C Cys  
	TTC F Phe      TCC S Ser      TAC Y Tyr      TGC C Cys  
	TTA L Leu      TCA S Ser      TAA * Ter      TGA * Ter  
	TTG L Leu i    TCG S Ser      TAG * Ter      TGG W Trp  

	CTT L Leu      CCT P Pro      CAT H His      CGT R Arg  
	CTC L Leu      CCC P Pro      CAC H His      CGC R Arg  
	CTA L Leu      CCA P Pro      CAA Q Gln      CGA R Arg  
	CTG L Leu i    CCG P Pro      CAG Q Gln      CGG R Arg  

	ATT I Ile i    ACT T Thr      AAT N Asn      AGT S Ser  
	ATC I Ile i    ACC T Thr      AAC N Asn      AGC S Ser  
	ATA I Ile i    ACA T Thr      AAA K Lys      AGA R Arg  
	ATG M Met i    ACG T Thr      AAG K Lys      AGG R Arg  

	GTT V Val      GCT A Ala      GAT D Asp      GGT G Gly  
	GTC V Val      GCC A Ala      GAC D Asp      GGC G Gly  
	GTA V Val      GCA A Ala      GAA E Glu      GGA G Gly  
	GTG V Val i    GCG A Ala      GAG E Glu      GGG G Gly

	http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/index.cgi?chapter=tgencodes#SG11
	*/
	
	/** the potential start codons in bacteria*/
	static final String startCodons[] =
		{"ATG", "TTG", "CTG", "ATT", "ATC", "ATA", "GTG"};
	
	
	/**
	 * Returns the amino acid one letter code, given a codon triplet.<br/>
	 * 
	 * @param triplet the nucleotide triplet (may consist of A, G, C, T and U)
	 * @return the one letter amino acid code, * for stop or null, if no valid triplet
	 */
	public static Character nt2aa(String triplet) {
		
		if (triplet.length() == 3) {
			Character charOne = triplet.charAt(0);
			Character charTwo = triplet.charAt(1);
			Character charThree = triplet.charAt(2);
			
			switch (charOne) {
			case 'T':
			case 'U':
				switch (charTwo) {
				case 'T':
				case 'U':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
						return 'F';
						
					case 'A':
					case 'G':
						return 'L';
						
					default:
						return null;
					}
					
				case 'C':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'S';
						
					default:
						return null;
					}
					
				case 'A':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
						return 'Y';
						
					case 'A':
					case 'G':
						return '*';
						
					default:
						return null;
					}
					
				case 'G':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
						return 'C';
						
					case 'A':
						return '*';
						
					case 'G':
						return 'W';
						
					default:
						return null;
					}
					
				default:
					return null;
					
				}
			
			case 'C':
				switch (charTwo) {
				case 'T':
				case 'U':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'L';
						
					default:
						return null;
					}
					
				case 'C':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'P';
						
					default:
						return null;
					}
				
				case 'A':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
						return 'H';
						
					case 'A':
					case 'G':
						return 'Q';
						
					default:
						return null;
					}
					
				case 'G':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'R';
						
					default:
						return null;
					}
					
				default:
					return null;
				}
				
			case 'A':
				switch (charTwo) {
				case 'T':
				case 'U':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
						return 'I';
						
					case 'G':
						return 'M';
						
					default:
						return null;
					}
					
				case 'C':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'T';
						
					default:
						return null;
					}
				
				case 'A':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
						return 'N';
						
					case 'A':
					case 'G':
						return 'K';
						
					default:
						return null;
					}
					
				case 'G':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
						return 'S';
						
					case 'A':
					case 'G':
						return 'R';
						
					default:
						return null;
					}
					
				default:
					return null;
				}
				
			case 'G':
				switch (charTwo) {
				case 'T':
				case 'U':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'V';
						
					default:
						return null;
					}
					
				case 'C':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'A';
						
					default:
						return null;
					}
				
				case 'A':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
						return 'D';
						
					case 'A':
					case 'G':
						return 'E';
						
					default:
						return null;
					}
					
				case 'G':
					switch (charThree) {
					case 'T':
					case 'U':
					case 'C':
					case 'A':
					case 'G':
						return 'G';
						
					default:
						return null;
					}
					
				default:
					return null;
				}
				
			default:
				return null;
			}
			
		}
		
		
		return null;
	}
	
	
	/**
	 * Returns the complementary nucleotide of the given nucleotide
	 * @return
	 */
	public static Character getComplementaryNucleotide(Character nucleotide) {
		
		switch (nucleotide) {
		case 'A':
			return 'T';
		case 'C':
			return 'G';
		case 'G':
			return 'C';
		case 'T':
		case 'U':
			return 'A';

		default:
			return null;
		}
	}
	
	
	/**
	 * Potential start-codons in bacteria:
	 * ATG M Met i (the default)
	 * TTG L Leu i
	 * CTG L Leu i
	 * ATT I Ile i
	 * ATC I Ile i
	 * ATA I Ile i
	 * GTG V Val i
	 * 
	 * @param triplet
	 * @return
	 */
	public static Boolean isStartCodon(String triplet) {
		for (String codon : startCodons) {
			if (codon.equalsIgnoreCase(triplet)) {
				return true;
			}
		}
		
		return false;
	}
}
