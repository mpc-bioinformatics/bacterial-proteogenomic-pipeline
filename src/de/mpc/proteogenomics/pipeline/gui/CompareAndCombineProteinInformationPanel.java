package de.mpc.proteogenomics.pipeline.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.CompareAndCombineProteinInformation;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;

public class CompareAndCombineProteinInformationPanel extends JPanel
		implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	private final static Logger logger =
			Logger.getLogger(CompareAndCombineProteinInformationPanel.class);
	
	private JFileChooser fileChooser;
	
	private JTextField fieldTargetGFFFile;
	private JButton btnBrowseTargetGFF;
	private JTextField fieldTargetFASTAFile;
	private JButton btnBrowseTargetFASTA;
	private JTextField fieldTargetAccessionRegex;
	private JButton btnTestTargetSettings;
	
	private JCheckBox chckbxParseMappingInformation;
	private JTextField fieldMappingFile;
	private JButton btnBrowseMappingFile;
	private JTextField fieldMappingRefAccessionRegex;
	private JTextField fieldMappingTargetAccessionRegex;
	private JButton btnTestMappingSettings;
	
	private JTextField fieldReferenceFastaFile;
	private JButton btnBrowseReferenceFile;
	private JTextField fieldReferenceAccessionRegex;
	private JTextField fieldReferenceAlternativeRegex;
	private JTextField fieldReferenceDescriptionRegex;
	private JButton btnTestReferenceSettings;
	
	private JTable tableTesting;
	
	private JButton btnBrowseOutputGffFile;
	private JTextField fieldOutputGffFile;
	
	private JButton btnProcess;
	
	private JLabel lblOutputFastaFile;
	private JTextField fieldOutputFastaFile;
	private JButton btnBrowseOutputFastaFile;
	
	
	/**
	 * Create the panel.
	 */
	public CompareAndCombineProteinInformationPanel(JFileChooser fc) {
		this.fileChooser = fc;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 150, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 10.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblCompareAndCombine = new JLabel("Compare and Combine");
		GridBagConstraints gbc_lblCompareAndCombine = new GridBagConstraints();
		gbc_lblCompareAndCombine.gridwidth = 2;
		gbc_lblCompareAndCombine.insets = new Insets(0, 0, 5, 0);
		gbc_lblCompareAndCombine.gridx = 0;
		gbc_lblCompareAndCombine.gridy = 0;
		add(lblCompareAndCombine, gbc_lblCompareAndCombine);
		
		JPanel targetInfoPanel = new JPanel();
		targetInfoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_targetInfoPanel = new GridBagConstraints();
		gbc_targetInfoPanel.insets = new Insets(0, 0, 5, 5);
		gbc_targetInfoPanel.fill = GridBagConstraints.BOTH;
		gbc_targetInfoPanel.gridx = 0;
		gbc_targetInfoPanel.gridy = 1;
		add(targetInfoPanel, gbc_targetInfoPanel);
		GridBagLayout gbl_targetInfoPanel = new GridBagLayout();
		gbl_targetInfoPanel.columnWidths = new int[]{0, 150, 0, 0};
		gbl_targetInfoPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_targetInfoPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_targetInfoPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		targetInfoPanel.setLayout(gbl_targetInfoPanel);
		
		JLabel lblTarget = new JLabel("Target Proteins Information");
		GridBagConstraints gbc_lblTarget = new GridBagConstraints();
		gbc_lblTarget.gridwidth = 3;
		gbc_lblTarget.insets = new Insets(0, 0, 5, 0);
		gbc_lblTarget.gridx = 0;
		gbc_lblTarget.gridy = 0;
		targetInfoPanel.add(lblTarget, gbc_lblTarget);
		
		JLabel lblGffFile = new JLabel("GFF file");
		GridBagConstraints gbc_lblGffFile = new GridBagConstraints();
		gbc_lblGffFile.anchor = GridBagConstraints.EAST;
		gbc_lblGffFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblGffFile.gridx = 0;
		gbc_lblGffFile.gridy = 1;
		targetInfoPanel.add(lblGffFile, gbc_lblGffFile);
		
		fieldTargetGFFFile = new JTextField();
		GridBagConstraints gbc_fieldTargetGFFFile = new GridBagConstraints();
		gbc_fieldTargetGFFFile.insets = new Insets(0, 0, 5, 5);
		gbc_fieldTargetGFFFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldTargetGFFFile.gridx = 1;
		gbc_fieldTargetGFFFile.gridy = 1;
		targetInfoPanel.add(fieldTargetGFFFile, gbc_fieldTargetGFFFile);
		fieldTargetGFFFile.setColumns(10);
		
		btnBrowseTargetGFF = new JButton("Browse...");
		btnBrowseTargetGFF.addActionListener(this);
		GridBagConstraints gbc_btnBrowseTargetGFF = new GridBagConstraints();
		gbc_btnBrowseTargetGFF.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseTargetGFF.gridx = 2;
		gbc_btnBrowseTargetGFF.gridy = 1;
		targetInfoPanel.add(btnBrowseTargetGFF, gbc_btnBrowseTargetGFF);
		
		JLabel lblFastaFile = new JLabel("FASTA file");
		GridBagConstraints gbc_lblFastaFile = new GridBagConstraints();
		gbc_lblFastaFile.anchor = GridBagConstraints.EAST;
		gbc_lblFastaFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblFastaFile.gridx = 0;
		gbc_lblFastaFile.gridy = 2;
		targetInfoPanel.add(lblFastaFile, gbc_lblFastaFile);
		
		fieldTargetFASTAFile = new JTextField();
		GridBagConstraints gbc_fieldTargetFASTAFile = new GridBagConstraints();
		gbc_fieldTargetFASTAFile.insets = new Insets(0, 0, 5, 5);
		gbc_fieldTargetFASTAFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldTargetFASTAFile.gridx = 1;
		gbc_fieldTargetFASTAFile.gridy = 2;
		targetInfoPanel.add(fieldTargetFASTAFile, gbc_fieldTargetFASTAFile);
		fieldTargetFASTAFile.setColumns(10);
		
		btnBrowseTargetFASTA = new JButton("Browse...");
		btnBrowseTargetFASTA.addActionListener(this);
		GridBagConstraints gbc_btnBrowseTargetFASTA = new GridBagConstraints();
		gbc_btnBrowseTargetFASTA.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseTargetFASTA.gridx = 2;
		gbc_btnBrowseTargetFASTA.gridy = 2;
		targetInfoPanel.add(btnBrowseTargetFASTA, gbc_btnBrowseTargetFASTA);
		
		JLabel lblAccessionRegex = new JLabel("Accession Regex");
		GridBagConstraints gbc_lblAccessionRegex = new GridBagConstraints();
		gbc_lblAccessionRegex.anchor = GridBagConstraints.EAST;
		gbc_lblAccessionRegex.insets = new Insets(0, 0, 0, 5);
		gbc_lblAccessionRegex.gridx = 0;
		gbc_lblAccessionRegex.gridy = 3;
		targetInfoPanel.add(lblAccessionRegex, gbc_lblAccessionRegex);
		
		fieldTargetAccessionRegex = new JTextField();
		fieldTargetAccessionRegex.setText(">([^ ]*) .*");
		GridBagConstraints gbc_fieldTargetAccessionRegex = new GridBagConstraints();
		gbc_fieldTargetAccessionRegex.insets = new Insets(0, 0, 0, 5);
		gbc_fieldTargetAccessionRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldTargetAccessionRegex.gridx = 1;
		gbc_fieldTargetAccessionRegex.gridy = 3;
		targetInfoPanel.add(fieldTargetAccessionRegex, gbc_fieldTargetAccessionRegex);
		fieldTargetAccessionRegex.setColumns(10);
		
		btnTestTargetSettings = new JButton("Test");
		btnTestTargetSettings.addActionListener(this);
		GridBagConstraints gbc_btnTestTargetSettings = new GridBagConstraints();
		gbc_btnTestTargetSettings.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTestTargetSettings.gridx = 2;
		gbc_btnTestTargetSettings.gridy = 3;
		targetInfoPanel.add(btnTestTargetSettings, gbc_btnTestTargetSettings);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		tableTesting = new JTable();
		tableTesting.setFillsViewportHeight(true);
		scrollPane.setViewportView(tableTesting);
		
		JPanel mappingInfoPanel = new JPanel();
		mappingInfoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_mappingInfoPanel = new GridBagConstraints();
		gbc_mappingInfoPanel.insets = new Insets(0, 0, 5, 5);
		gbc_mappingInfoPanel.fill = GridBagConstraints.BOTH;
		gbc_mappingInfoPanel.gridx = 0;
		gbc_mappingInfoPanel.gridy = 2;
		add(mappingInfoPanel, gbc_mappingInfoPanel);
		GridBagLayout gbl_mappingInfoPanel = new GridBagLayout();
		gbl_mappingInfoPanel.columnWidths = new int[]{0, 150, 0, 0};
		gbl_mappingInfoPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_mappingInfoPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_mappingInfoPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		mappingInfoPanel.setLayout(gbl_mappingInfoPanel);
		
		JLabel lblMappingInformation = new JLabel("Mapping Information");
		GridBagConstraints gbc_lblMappingInformation = new GridBagConstraints();
		gbc_lblMappingInformation.gridwidth = 3;
		gbc_lblMappingInformation.insets = new Insets(0, 0, 5, 0);
		gbc_lblMappingInformation.gridx = 0;
		gbc_lblMappingInformation.gridy = 0;
		mappingInfoPanel.add(lblMappingInformation, gbc_lblMappingInformation);
		
		chckbxParseMappingInformation = new JCheckBox("Parse mapping information");
		chckbxParseMappingInformation.addActionListener(this);
		GridBagConstraints gbc_chckbxParseMappingInformation = new GridBagConstraints();
		gbc_chckbxParseMappingInformation.anchor = GridBagConstraints.WEST;
		gbc_chckbxParseMappingInformation.gridwidth = 3;
		gbc_chckbxParseMappingInformation.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxParseMappingInformation.gridx = 0;
		gbc_chckbxParseMappingInformation.gridy = 1;
		mappingInfoPanel.add(chckbxParseMappingInformation, gbc_chckbxParseMappingInformation);
		
		JLabel lblMappingFile = new JLabel("Mapping file");
		GridBagConstraints gbc_lblMappingFile = new GridBagConstraints();
		gbc_lblMappingFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblMappingFile.anchor = GridBagConstraints.EAST;
		gbc_lblMappingFile.gridx = 0;
		gbc_lblMappingFile.gridy = 2;
		mappingInfoPanel.add(lblMappingFile, gbc_lblMappingFile);
		
		fieldMappingFile = new JTextField();
		GridBagConstraints gbc_fieldMappingFile = new GridBagConstraints();
		gbc_fieldMappingFile.insets = new Insets(0, 0, 5, 5);
		gbc_fieldMappingFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldMappingFile.gridx = 1;
		gbc_fieldMappingFile.gridy = 2;
		mappingInfoPanel.add(fieldMappingFile, gbc_fieldMappingFile);
		fieldMappingFile.setColumns(10);
		
		btnBrowseMappingFile = new JButton("Browse...");
		btnBrowseMappingFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseMappingFile = new GridBagConstraints();
		gbc_btnBrowseMappingFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseMappingFile.gridx = 2;
		gbc_btnBrowseMappingFile.gridy = 2;
		mappingInfoPanel.add(btnBrowseMappingFile, gbc_btnBrowseMappingFile);
		
		JLabel lblReferenceAccessionRegex = new JLabel("Reference accession regex");
		GridBagConstraints gbc_lblReferenceAccessionRegex = new GridBagConstraints();
		gbc_lblReferenceAccessionRegex.anchor = GridBagConstraints.EAST;
		gbc_lblReferenceAccessionRegex.insets = new Insets(0, 0, 5, 5);
		gbc_lblReferenceAccessionRegex.gridx = 0;
		gbc_lblReferenceAccessionRegex.gridy = 3;
		mappingInfoPanel.add(lblReferenceAccessionRegex, gbc_lblReferenceAccessionRegex);
		
		fieldMappingRefAccessionRegex = new JTextField();
		fieldMappingRefAccessionRegex.setText("^(\\S+)\\s.*");
		GridBagConstraints gbc_fieldMappingRefAccessionRegex = new GridBagConstraints();
		gbc_fieldMappingRefAccessionRegex.insets = new Insets(0, 0, 5, 5);
		gbc_fieldMappingRefAccessionRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldMappingRefAccessionRegex.gridx = 1;
		gbc_fieldMappingRefAccessionRegex.gridy = 3;
		mappingInfoPanel.add(fieldMappingRefAccessionRegex, gbc_fieldMappingRefAccessionRegex);
		fieldMappingRefAccessionRegex.setColumns(10);
		
		JLabel lblTargetAccessionRegex = new JLabel("Target accession regex");
		GridBagConstraints gbc_lblTargetAccessionRegex = new GridBagConstraints();
		gbc_lblTargetAccessionRegex.anchor = GridBagConstraints.EAST;
		gbc_lblTargetAccessionRegex.insets = new Insets(0, 0, 0, 5);
		gbc_lblTargetAccessionRegex.gridx = 0;
		gbc_lblTargetAccessionRegex.gridy = 4;
		mappingInfoPanel.add(lblTargetAccessionRegex, gbc_lblTargetAccessionRegex);
		
		fieldMappingTargetAccessionRegex = new JTextField();
		fieldMappingTargetAccessionRegex.setText("^\\S+\\s+(\\S+)");
		GridBagConstraints gbc_fieldMappingTargetAccessionRegex = new GridBagConstraints();
		gbc_fieldMappingTargetAccessionRegex.insets = new Insets(0, 0, 0, 5);
		gbc_fieldMappingTargetAccessionRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldMappingTargetAccessionRegex.gridx = 1;
		gbc_fieldMappingTargetAccessionRegex.gridy = 4;
		mappingInfoPanel.add(fieldMappingTargetAccessionRegex, gbc_fieldMappingTargetAccessionRegex);
		fieldMappingTargetAccessionRegex.setColumns(10);
		
		btnTestMappingSettings = new JButton("Test");
		btnTestMappingSettings.addActionListener(this);
		GridBagConstraints gbc_btnTestMappingSettings = new GridBagConstraints();
		gbc_btnTestMappingSettings.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTestMappingSettings.gridx = 2;
		gbc_btnTestMappingSettings.gridy = 4;
		mappingInfoPanel.add(btnTestMappingSettings, gbc_btnTestMappingSettings);
		
		JPanel referenceInfoPanel = new JPanel();
		referenceInfoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_referenceInfoPanel = new GridBagConstraints();
		gbc_referenceInfoPanel.insets = new Insets(0, 0, 5, 5);
		gbc_referenceInfoPanel.fill = GridBagConstraints.BOTH;
		gbc_referenceInfoPanel.gridx = 0;
		gbc_referenceInfoPanel.gridy = 3;
		add(referenceInfoPanel, gbc_referenceInfoPanel);
		GridBagLayout gbl_referenceInfoPanel = new GridBagLayout();
		gbl_referenceInfoPanel.columnWidths = new int[]{0, 150, 0, 0};
		gbl_referenceInfoPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_referenceInfoPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_referenceInfoPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		referenceInfoPanel.setLayout(gbl_referenceInfoPanel);
		
		JLabel lblReferenceFile = new JLabel("Reference Proteins Information");
		GridBagConstraints gbc_lblReferenceFile = new GridBagConstraints();
		gbc_lblReferenceFile.gridwidth = 3;
		gbc_lblReferenceFile.insets = new Insets(0, 0, 5, 0);
		gbc_lblReferenceFile.gridx = 0;
		gbc_lblReferenceFile.gridy = 0;
		referenceInfoPanel.add(lblReferenceFile, gbc_lblReferenceFile);
		
		JLabel lblReferenceFastaFile = new JLabel("Reference FASTA file");
		GridBagConstraints gbc_lblReferenceFastaFile = new GridBagConstraints();
		gbc_lblReferenceFastaFile.anchor = GridBagConstraints.EAST;
		gbc_lblReferenceFastaFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblReferenceFastaFile.gridx = 0;
		gbc_lblReferenceFastaFile.gridy = 1;
		referenceInfoPanel.add(lblReferenceFastaFile, gbc_lblReferenceFastaFile);
		
		fieldReferenceFastaFile = new JTextField();
		GridBagConstraints gbc_fieldReferenceFastaFile = new GridBagConstraints();
		gbc_fieldReferenceFastaFile.insets = new Insets(0, 0, 5, 5);
		gbc_fieldReferenceFastaFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldReferenceFastaFile.gridx = 1;
		gbc_fieldReferenceFastaFile.gridy = 1;
		referenceInfoPanel.add(fieldReferenceFastaFile, gbc_fieldReferenceFastaFile);
		fieldReferenceFastaFile.setColumns(10);
		
		btnBrowseReferenceFile = new JButton("Browse...");
		btnBrowseReferenceFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseReferenceFile = new GridBagConstraints();
		gbc_btnBrowseReferenceFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseReferenceFile.gridx = 2;
		gbc_btnBrowseReferenceFile.gridy = 1;
		referenceInfoPanel.add(btnBrowseReferenceFile, gbc_btnBrowseReferenceFile);
		
		JLabel lblAccessionRegex_1 = new JLabel("Accession Regex");
		GridBagConstraints gbc_lblAccessionRegex_1 = new GridBagConstraints();
		gbc_lblAccessionRegex_1.anchor = GridBagConstraints.EAST;
		gbc_lblAccessionRegex_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblAccessionRegex_1.gridx = 0;
		gbc_lblAccessionRegex_1.gridy = 2;
		referenceInfoPanel.add(lblAccessionRegex_1, gbc_lblAccessionRegex_1);
		
		fieldReferenceAccessionRegex = new JTextField();
		fieldReferenceAccessionRegex.setText("^>[sptr]{2}\\|(\\S+)\\|.*");
		GridBagConstraints gbc_fieldReferenceAccessionRegex = new GridBagConstraints();
		gbc_fieldReferenceAccessionRegex.insets = new Insets(0, 0, 5, 5);
		gbc_fieldReferenceAccessionRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldReferenceAccessionRegex.gridx = 1;
		gbc_fieldReferenceAccessionRegex.gridy = 2;
		referenceInfoPanel.add(fieldReferenceAccessionRegex, gbc_fieldReferenceAccessionRegex);
		fieldReferenceAccessionRegex.setColumns(10);
		
		JLabel lblAlternativeAccession = new JLabel("Gene name regex");
		GridBagConstraints gbc_lblAlternativeAccession = new GridBagConstraints();
		gbc_lblAlternativeAccession.anchor = GridBagConstraints.EAST;
		gbc_lblAlternativeAccession.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlternativeAccession.gridx = 0;
		gbc_lblAlternativeAccession.gridy = 3;
		referenceInfoPanel.add(lblAlternativeAccession, gbc_lblAlternativeAccession);
		
		fieldReferenceAlternativeRegex = new JTextField();
		fieldReferenceAlternativeRegex.setText("^>[sptr]{2}\\|\\S+\\|\\S+ .+ GN=(\\S+) PE=\\d+ SV=\\d+$");
		GridBagConstraints gbc_fieldReferenceAlternativeRegex = new GridBagConstraints();
		gbc_fieldReferenceAlternativeRegex.insets = new Insets(0, 0, 5, 5);
		gbc_fieldReferenceAlternativeRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldReferenceAlternativeRegex.gridx = 1;
		gbc_fieldReferenceAlternativeRegex.gridy = 3;
		referenceInfoPanel.add(fieldReferenceAlternativeRegex, gbc_fieldReferenceAlternativeRegex);
		fieldReferenceAlternativeRegex.setColumns(10);
		
		JLabel lblDescriptionRegex = new JLabel("Description regex");
		GridBagConstraints gbc_lblDescriptionRegex = new GridBagConstraints();
		gbc_lblDescriptionRegex.anchor = GridBagConstraints.EAST;
		gbc_lblDescriptionRegex.insets = new Insets(0, 0, 0, 5);
		gbc_lblDescriptionRegex.gridx = 0;
		gbc_lblDescriptionRegex.gridy = 4;
		referenceInfoPanel.add(lblDescriptionRegex, gbc_lblDescriptionRegex);
		
		fieldReferenceDescriptionRegex = new JTextField();
		fieldReferenceDescriptionRegex.setText("^>[sptr]{2}\\|\\S+\\|(.+)$");
		GridBagConstraints gbc_fieldReferenceDescriptionRegex = new GridBagConstraints();
		gbc_fieldReferenceDescriptionRegex.insets = new Insets(0, 0, 0, 5);
		gbc_fieldReferenceDescriptionRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldReferenceDescriptionRegex.gridx = 1;
		gbc_fieldReferenceDescriptionRegex.gridy = 4;
		referenceInfoPanel.add(fieldReferenceDescriptionRegex, gbc_fieldReferenceDescriptionRegex);
		fieldReferenceDescriptionRegex.setColumns(10);
		
		btnTestReferenceSettings = new JButton("Test");
		btnTestReferenceSettings.addActionListener(this);
		GridBagConstraints gbc_btnTestReferenceSettings = new GridBagConstraints();
		gbc_btnTestReferenceSettings.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTestReferenceSettings.gridx = 2;
		gbc_btnTestReferenceSettings.gridy = 4;
		referenceInfoPanel.add(btnTestReferenceSettings, gbc_btnTestReferenceSettings);
		
		JPanel processPanel = new JPanel();
		GridBagConstraints gbc_processPanel = new GridBagConstraints();
		gbc_processPanel.gridwidth = 2;
		gbc_processPanel.insets = new Insets(0, 0, 0, 5);
		gbc_processPanel.fill = GridBagConstraints.BOTH;
		gbc_processPanel.gridx = 0;
		gbc_processPanel.gridy = 4;
		add(processPanel, gbc_processPanel);
		GridBagLayout gbl_processPanel = new GridBagLayout();
		gbl_processPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_processPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_processPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_processPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		processPanel.setLayout(gbl_processPanel);
		
		JLabel lblOutputGffFile = new JLabel("Output GFF file");
		GridBagConstraints gbc_lblOutputGffFile = new GridBagConstraints();
		gbc_lblOutputGffFile.anchor = GridBagConstraints.EAST;
		gbc_lblOutputGffFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputGffFile.gridx = 0;
		gbc_lblOutputGffFile.gridy = 0;
		processPanel.add(lblOutputGffFile, gbc_lblOutputGffFile);
		
		fieldOutputGffFile = new JTextField();
		GridBagConstraints gbc_fieldOutputGffFile = new GridBagConstraints();
		gbc_fieldOutputGffFile.insets = new Insets(0, 0, 5, 5);
		gbc_fieldOutputGffFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldOutputGffFile.gridx = 1;
		gbc_fieldOutputGffFile.gridy = 0;
		processPanel.add(fieldOutputGffFile, gbc_fieldOutputGffFile);
		fieldOutputGffFile.setColumns(10);
		
		btnBrowseOutputGffFile = new JButton("Browse...");
		btnBrowseOutputGffFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseOutputGffFile = new GridBagConstraints();
		gbc_btnBrowseOutputGffFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseOutputGffFile.gridx = 2;
		gbc_btnBrowseOutputGffFile.gridy = 0;
		processPanel.add(btnBrowseOutputGffFile, gbc_btnBrowseOutputGffFile);
		
		lblOutputFastaFile = new JLabel("Output FASTA file");
		GridBagConstraints gbc_lblOutputFastaFile = new GridBagConstraints();
		gbc_lblOutputFastaFile.anchor = GridBagConstraints.EAST;
		gbc_lblOutputFastaFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputFastaFile.gridx = 0;
		gbc_lblOutputFastaFile.gridy = 1;
		processPanel.add(lblOutputFastaFile, gbc_lblOutputFastaFile);
		
		fieldOutputFastaFile = new JTextField();
		GridBagConstraints gbc_fieldOutputFastaFile = new GridBagConstraints();
		gbc_fieldOutputFastaFile.insets = new Insets(0, 0, 5, 5);
		gbc_fieldOutputFastaFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldOutputFastaFile.gridx = 1;
		gbc_fieldOutputFastaFile.gridy = 1;
		processPanel.add(fieldOutputFastaFile, gbc_fieldOutputFastaFile);
		fieldOutputFastaFile.setColumns(10);
		
		btnBrowseOutputFastaFile = new JButton("Browse...");
		btnBrowseOutputFastaFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseOutputFastaFile = new GridBagConstraints();
		gbc_btnBrowseOutputFastaFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseOutputFastaFile.gridx = 2;
		gbc_btnBrowseOutputFastaFile.gridy = 1;
		processPanel.add(btnBrowseOutputFastaFile, gbc_btnBrowseOutputFastaFile);
		
		btnProcess = new JButton("Process");
		btnProcess.addActionListener(this);
		GridBagConstraints gbc_btnProcess = new GridBagConstraints();
		gbc_btnProcess.gridwidth = 3;
		gbc_btnProcess.gridx = 0;
		gbc_btnProcess.gridy = 2;
		processPanel.add(btnProcess, gbc_btnProcess);
		
		
		// initialize states of some fields
		fieldMappingFile.setEnabled(
				chckbxParseMappingInformation.isSelected());
		btnBrowseMappingFile.setEnabled(
				chckbxParseMappingInformation.isSelected());
		fieldMappingRefAccessionRegex.setEnabled(
				chckbxParseMappingInformation.isSelected());
		fieldMappingTargetAccessionRegex.setEnabled(
				chckbxParseMappingInformation.isSelected());
		btnTestMappingSettings.setEnabled(
				chckbxParseMappingInformation.isSelected());
	}
	
	
	/**
	 * Action listener
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnBrowseTargetGFF) {
			GUIHelper.browseFileForField(fieldTargetGFFFile, fileChooser,
					CompareAndCombineProteinInformationPanel.this);
		} else if (e.getSource() == btnBrowseTargetFASTA) {
			GUIHelper.browseFileForField(fieldTargetFASTAFile, fileChooser,
					CompareAndCombineProteinInformationPanel.this);
		} else if (e.getSource() == btnTestTargetSettings) {
			testTargetSettings();
		} else if (e.getSource() == chckbxParseMappingInformation) {
			fieldMappingFile.setEnabled(
					chckbxParseMappingInformation.isSelected());
			btnBrowseMappingFile.setEnabled(
					chckbxParseMappingInformation.isSelected());
			fieldMappingRefAccessionRegex.setEnabled(
					chckbxParseMappingInformation.isSelected());
			fieldMappingTargetAccessionRegex.setEnabled(
					chckbxParseMappingInformation.isSelected());
			btnTestMappingSettings.setEnabled(
					chckbxParseMappingInformation.isSelected());
		} else if (e.getSource() == btnBrowseMappingFile) {
			GUIHelper.browseFileForField(fieldMappingFile, fileChooser,
					CompareAndCombineProteinInformationPanel.this);
		} else if (e.getSource() == btnTestMappingSettings) {
			testMappingSettings();
		} else if (e.getSource() == btnBrowseReferenceFile) {
			GUIHelper.browseFileForField(fieldReferenceFastaFile, fileChooser,
					CompareAndCombineProteinInformationPanel.this);
		} else if (e.getSource() == btnTestReferenceSettings) {
			testReferenceSettings();
		} else if (e.getSource() == btnBrowseOutputGffFile) {
			GUIHelper.browseFileForField(fieldOutputGffFile, fileChooser,
					CompareAndCombineProteinInformationPanel.this);
		} else if (e.getSource() == btnBrowseOutputFastaFile) {
			GUIHelper.browseFileForField(fieldOutputFastaFile, fileChooser,
					CompareAndCombineProteinInformationPanel.this);
		} else if (e.getSource() == btnProcess) {
			// TODO: put a thread here, which handles the processing
			
			processData();
		}
	}
	
	
	/**
	 * Tests the target settings and fills the test table.
	 */
	private void testTargetSettings() {
		Map<String, String> headerToAccessions =
				new HashMap<String, String>(25);
		
		headerToAccessions = 
				CompareAndCombineProteinInformation.testSettingsForTargetProteinlist(
						fieldTargetFASTAFile.getText(),
						fieldTargetAccessionRegex.getText(),
						25);
		
		Object[] columnNames = {"Header", "Accession"};
		Object[][] dataArray = new Object[headerToAccessions.size()][2];
		
		int rowCount = 0;
		for (Map.Entry<String, String> accIt
				: headerToAccessions.entrySet()) {
			dataArray[rowCount][0] = accIt.getKey();
			dataArray[rowCount][1] = accIt.getValue();
			
			rowCount++;
		}
		
		tableTesting.setModel(new DefaultTableModel(dataArray, columnNames));
	}
	
	
	/**
	 * Tests the mapping and fills the test table.
	 */
	private void testMappingSettings() {
		List<String[]> mappings =
				CompareAndCombineProteinInformation.testAccessionMapping(
						fieldMappingFile.getText(),
						fieldMappingRefAccessionRegex.getText(), 
						fieldMappingTargetAccessionRegex.getText(),
						100);
		
		Object[] columnNames = {"Line", "Reference", "Target"};
		Object[][] dataArray = new Object[mappings.size()][3];
		
		int rowCount = 0;
		for (String[] entry : mappings) {
			dataArray[rowCount] = entry;
			rowCount++;
		}
		
		tableTesting.setModel(new DefaultTableModel(dataArray, columnNames));
	}
	
	
	/**
	 * Tests the reference settings and fills the test table.
	 */
	private void testReferenceSettings() {
		List<String[]> referenceData =
				CompareAndCombineProteinInformation.testSettingsForReferenceProteinList(
						fieldReferenceFastaFile.getText(),
						fieldReferenceAccessionRegex.getText(),
						fieldReferenceAlternativeRegex.getText(),
						fieldReferenceDescriptionRegex.getText(),
						25);
		
		Object[] columnNames =
			{"Header", "Accession", "Alternative", "Description"};
		Object[][] dataArray = new Object[referenceData.size()][4];
		
		int rowCount = 0;
		for (String[] entry : referenceData) {
			dataArray[rowCount] = entry;
			rowCount++;
		}
		
		tableTesting.setModel(new DefaultTableModel(dataArray, columnNames));
	}
	
	
	/**
	 * processes the data
	 */
	private void processData() {
		CompareAndCombineProteinInformation cacpl = new CompareAndCombineProteinInformation();
		
		int parsedTargetProteins = cacpl.getDataForTargetProteinlist(
				fieldTargetGFFFile.getText(),
				fieldTargetFASTAFile.getText(),
				fieldTargetAccessionRegex.getText());
		if (parsedTargetProteins > 0) {
			logger.info("parsed from target files: " + parsedTargetProteins);
		} else {
			logger.error("No target proteins parsed from GFF and FASTA file");
			return;
		}
		
		
		if (chckbxParseMappingInformation.isSelected()) {
			int parsedMappings = cacpl.parseAccessionMapping(
					fieldMappingFile.getText(),
					fieldMappingRefAccessionRegex.getText(),
					fieldMappingTargetAccessionRegex.getText());
			if (parsedMappings > 0) {
				logger.info("parsed mappings: " + parsedMappings);
			} else {
				logger.error("No mappings could be parsed.");
				return;
			}
		}
		
		cacpl.mapTargetsToReference(
				fieldReferenceFastaFile.getText(),
				fieldReferenceAccessionRegex.getText(),
				fieldReferenceAlternativeRegex.getText(),
				fieldReferenceDescriptionRegex.getText());
		
		if (fieldOutputGffFile.getText().trim().length() > 0) {
			int gffWritten = cacpl.writeToGFF(fieldOutputGffFile.getText());
			logger.info(gffWritten + " proteins/genes written to " +
					fieldOutputGffFile.getText());
		}
		
		if (fieldOutputFastaFile.getText().trim().length() > 0) {
			int fastaWritten =
					cacpl.writeToFASTA(fieldOutputFastaFile.getText());
			logger.info(fastaWritten + " proteins/genes written to " +
					fieldOutputFastaFile.getText());
		}
	}
}
