deprecated / no longer maintained
=================================

The Bacterial Proteogenomic Pipeline is currently no longer maintained. You can still download and use it, but we will not be able to enhance its usage right now.


bacterial-proteogenomic-pipeline
================================

The Bacterial Proteogenomic Pipeline consists of several modules, which assist
in a proteogenomics analysis. Each module can either be called by the command
line or via a Java Swing GUI.

For an example walkthrough, please see the wiki page (https://github.com/mpc-bioinformatics/bacterial-proteogenomic-pipeline/wiki/Example-Walkthrough)

## Download
Download the latest released version [here](https://github.com/mpc-bioinformatics/bacterial-proteogenomic-pipeline/releases/latest)

## Publication / How to cite it:
The bacterial proteogenomic pipeline. Julian Uszkoreit, Nicole Plohnke, Sascha Rexroth, Katrin Marcus and Martin Eisenacher. BMC Genomics 2014, 15(Suppl 9):S19. doi:10.1186/1471-2164-15-S9-S19
http://www.biomedcentral.com/1471-2164/15/S9/S19


## The Pipeline

### Parse Protein Information
Reads in a protein database in FASTA format, which should contain the reading
frame positions of the proteins in the header. Alternatively a TSV/CSV file
may be used, which holds all information for the annotated genes/proteins.
With this information, a GFF3 file for the "known proteins" is created.

### Compare And Combine (optional)
This class helps to get further protein information from another FASTA file.
To do this, take the information from the GFF file created by
ParseProteinInformationToGFF with the corresponding FASTA file as target and
another FASTA database as reference set. The enriched information will be
written into a new GFF and FASTA file.
 
### Genome Parser
Reads in a FASTA containing the genome of a specific species in all six
reading frames and translates it into all possible "pseudoproteins".
Only the translation code for bacteria is used at the moment. In Bacteria,
there are codons which signal a start codon but are usually NOT coding for
methionine. These are still translated into M, if they start the protein.
A pseudo protein marked as ORF is the first (i.e. automatically longest)
sub-sequence of a pseudo protein with a start codon as sequence start.
  
If a GFF file for the known proteins (from ParseProteinInformationToGFF and/or
CompareAndCombineProteinList) is given, pseudoproteins with exactly the same
translation start and end sites as a known protein will not be reported.
  
Reported is a FASTA file containing all the pseudo proteins and for each
frame (0-5) a GFF file with the corresponding information.

### Create Decoy DB (optional)
Generate combined and decoy database and search by search engines.

### Combine Identifications
In this step, the search results of an external search are combined.
The identified PSM may be validated and FDR-filtered. The module takes mzTab
files as input.

### Analysis
The final step allows analysis of the identified peptides and visualizes the
number of distinctly identified PSMs for each peptide.
