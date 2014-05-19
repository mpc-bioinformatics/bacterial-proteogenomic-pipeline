package de.mpc.proteogenomics.pipeline.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JButton;

import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.GenomeParser;


public class GenomeParserPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = Logger.getLogger(GenomeParserPanel.class);
	
	private JTextField textGenomeFASTA;
	private JButton btnBrowseGenomeFASTA;
	
	private JTextField textGenomeName;
	private JButton btnParseGenome;
	
	private JTextField textKnownProteins;
	private JButton btnBrowseKnownGFF;
	
	private JTextField textOutputBase;
	private JButton btnBrowseOutputBasename;
	
	
	private JFileChooser fileChooser;
	
	
	
	public GenomeParserPanel(JFileChooser fc) {
		this.fileChooser = fc;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblGenomeParser = new JLabel("Genome Parser");
		GridBagConstraints gbc_lblGenomeParser = new GridBagConstraints();
		gbc_lblGenomeParser.gridwidth = 3;
		gbc_lblGenomeParser.insets = new Insets(0, 0, 5, 0);
		gbc_lblGenomeParser.gridx = 0;
		gbc_lblGenomeParser.gridy = 0;
		add(lblGenomeParser, gbc_lblGenomeParser);
		
		JLabel lblGenomeFASTA = new JLabel("Genome FASTA");
		GridBagConstraints gbc_lblGenomeFASTA = new GridBagConstraints();
		gbc_lblGenomeFASTA.anchor = GridBagConstraints.EAST;
		gbc_lblGenomeFASTA.insets = new Insets(0, 0, 5, 5);
		gbc_lblGenomeFASTA.gridx = 0;
		gbc_lblGenomeFASTA.gridy = 1;
		add(lblGenomeFASTA, gbc_lblGenomeFASTA);
		
		textGenomeFASTA = new JTextField();
		GridBagConstraints gbc_textGenomeFASTA = new GridBagConstraints();
		gbc_textGenomeFASTA.insets = new Insets(0, 0, 5, 5);
		gbc_textGenomeFASTA.fill = GridBagConstraints.HORIZONTAL;
		gbc_textGenomeFASTA.gridx = 1;
		gbc_textGenomeFASTA.gridy = 1;
		add(textGenomeFASTA, gbc_textGenomeFASTA);
		textGenomeFASTA.setColumns(10);
		
		btnBrowseGenomeFASTA = new JButton("Browse...");
		btnBrowseGenomeFASTA.addActionListener(this);
		GridBagConstraints gbc_btnBrowseGenomeFASTA = new GridBagConstraints();
		gbc_btnBrowseGenomeFASTA.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseGenomeFASTA.gridx = 2;
		gbc_btnBrowseGenomeFASTA.gridy = 1;
		add(btnBrowseGenomeFASTA, gbc_btnBrowseGenomeFASTA);
		
		JLabel lblGenomeName = new JLabel("Genome Name");
		GridBagConstraints gbc_lblGenomeName = new GridBagConstraints();
		gbc_lblGenomeName.anchor = GridBagConstraints.EAST;
		gbc_lblGenomeName.insets = new Insets(0, 0, 5, 5);
		gbc_lblGenomeName.gridx = 0;
		gbc_lblGenomeName.gridy = 2;
		add(lblGenomeName, gbc_lblGenomeName);
		
		textGenomeName = new JTextField();
		textGenomeName.setText("Chr");
		GridBagConstraints gbc_textGenomeName = new GridBagConstraints();
		gbc_textGenomeName.insets = new Insets(0, 0, 5, 5);
		gbc_textGenomeName.fill = GridBagConstraints.HORIZONTAL;
		gbc_textGenomeName.gridx = 1;
		gbc_textGenomeName.gridy = 2;
		add(textGenomeName, gbc_textGenomeName);
		textGenomeName.setColumns(10);
		
		JLabel lblKnownProteins = new JLabel("Known Proteins GFF");
		GridBagConstraints gbc_lblKnownProteins = new GridBagConstraints();
		gbc_lblKnownProteins.anchor = GridBagConstraints.EAST;
		gbc_lblKnownProteins.insets = new Insets(0, 0, 5, 5);
		gbc_lblKnownProteins.gridx = 0;
		gbc_lblKnownProteins.gridy = 3;
		add(lblKnownProteins, gbc_lblKnownProteins);
		
		textKnownProteins = new JTextField();
		GridBagConstraints gbc_textKnownProteins = new GridBagConstraints();
		gbc_textKnownProteins.insets = new Insets(0, 0, 5, 5);
		gbc_textKnownProteins.fill = GridBagConstraints.HORIZONTAL;
		gbc_textKnownProteins.gridx = 1;
		gbc_textKnownProteins.gridy = 3;
		add(textKnownProteins, gbc_textKnownProteins);
		textKnownProteins.setColumns(10);
		
		btnBrowseKnownGFF = new JButton("Browse...");
		btnBrowseKnownGFF.addActionListener(this);
		GridBagConstraints gbc_btnBrowseKnownGFF = new GridBagConstraints();
		gbc_btnBrowseKnownGFF.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseKnownGFF.gridx = 2;
		gbc_btnBrowseKnownGFF.gridy = 3;
		add(btnBrowseKnownGFF, gbc_btnBrowseKnownGFF);
		
		JLabel lblOutputBasename = new JLabel("Output basename");
		GridBagConstraints gbc_lblOutputBasename = new GridBagConstraints();
		gbc_lblOutputBasename.anchor = GridBagConstraints.EAST;
		gbc_lblOutputBasename.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputBasename.gridx = 0;
		gbc_lblOutputBasename.gridy = 4;
		add(lblOutputBasename, gbc_lblOutputBasename);
		
		textOutputBase = new JTextField();
		GridBagConstraints gbc_textOutputBase = new GridBagConstraints();
		gbc_textOutputBase.insets = new Insets(0, 0, 5, 5);
		gbc_textOutputBase.fill = GridBagConstraints.HORIZONTAL;
		gbc_textOutputBase.gridx = 1;
		gbc_textOutputBase.gridy = 4;
		add(textOutputBase, gbc_textOutputBase);
		textOutputBase.setColumns(10);
		
		btnBrowseOutputBasename = new JButton("Browse...");
		btnBrowseOutputBasename.addActionListener(this);
		GridBagConstraints gbc_btnBrowseOutputBasename = new GridBagConstraints();
		gbc_btnBrowseOutputBasename.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseOutputBasename.gridx = 2;
		gbc_btnBrowseOutputBasename.gridy = 4;
		add(btnBrowseOutputBasename, gbc_btnBrowseOutputBasename);
		
		JPanel panelParse = new JPanel();
		GridBagConstraints gbc_panelParse = new GridBagConstraints();
		gbc_panelParse.gridwidth = 3;
		gbc_panelParse.fill = GridBagConstraints.VERTICAL;
		gbc_panelParse.insets = new Insets(0, 0, 0, 5);
		gbc_panelParse.gridx = 0;
		gbc_panelParse.gridy = 5;
		add(panelParse, gbc_panelParse);
		GridBagLayout gbl_panelParse = new GridBagLayout();
		gbl_panelParse.columnWidths = new int[]{137, 0};
		gbl_panelParse.rowHeights = new int[]{25, 0};
		gbl_panelParse.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panelParse.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelParse.setLayout(gbl_panelParse);
		
		btnParseGenome = new JButton("Parse Genome");
		btnParseGenome.addActionListener(this);
		GridBagConstraints gbc_btnParseGenome = new GridBagConstraints();
		gbc_btnParseGenome.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnParseGenome.gridx = 0;
		gbc_btnParseGenome.gridy = 0;
		panelParse.add(btnParseGenome, gbc_btnParseGenome);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(btnBrowseGenomeFASTA)) {
			GUIHelper.browseFileForField(textGenomeFASTA, fileChooser,
					GenomeParserPanel.this);
		} else if (e.getSource().equals(btnBrowseKnownGFF)) {
			GUIHelper.browseFileForField(textKnownProteins, fileChooser,
					GenomeParserPanel.this);
		} else if (e.getSource().equals(btnBrowseOutputBasename)) {
			GUIHelper.browseFileForField(textOutputBase, fileChooser,
					GenomeParserPanel.this);
		} else if (e.getSource().equals(btnParseGenome)) {
			
			// TODO: add thread here
			btnParseGenome.setEnabled(false);
			parseGenome();
			btnParseGenome.setEnabled(true);
		}
	}
	
	
	/**
	 * Parses the genome file with the given settings.
	 */
	private void parseGenome() {
		if (textGenomeFASTA.getText().trim().length() < 1) {
			logger.error("Please give a genome file.");
			return;
		}
		
		if (textGenomeName.getText().trim().length() < 1) {
			textGenomeName.setText("Chr");
			logger.info("Genome name set to default " +
					textGenomeName.getText());
		}
		
		GenomeParser parser = new GenomeParser(textGenomeFASTA.getText(), 5,
						textGenomeName.getText());
		
		if (textKnownProteins.getText().trim().length() < 1) {
			logger.warn("No known proteins file given! This is not critical, "
					+ "but probably you want to enter the file from the first "
					+ "and/or second step.");
		}
		
		if (textOutputBase.getText().trim().length() < 1) {
			logger.error("No output basename given!");
			return;
		}
		
		String outFasta = textOutputBase.getText() + ".fasta";
		String outGFF = textOutputBase.getText();
		
		try {
			parser.parseGenome(outFasta, outGFF,
					(textKnownProteins.getText().trim().length() < 1) ?
							null : textKnownProteins.getText());
		} catch (IOException e) {
			logger.error("error while parsing genome file", e);
		}
	}
}
