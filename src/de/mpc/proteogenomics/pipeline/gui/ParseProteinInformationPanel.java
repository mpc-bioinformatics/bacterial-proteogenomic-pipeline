package de.mpc.proteogenomics.pipeline.gui;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTable;
import javax.swing.JTextField;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.ParseProteinInformation;
import de.mpc.proteogenomics.pipeline.protein.GenericProtein;

import javax.swing.JScrollPane;
import javax.swing.JFormattedTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.BevelBorder;


public class ParseProteinInformationPanel extends JPanel
		implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	private final static Logger logger = Logger.getLogger(ParseProteinInformationPanel.class);
	
	private JTextField fieldFilename;
	private JButton btnBrowseInputFile;
	
	private JRadioButton rdbtnTsvCsvFile;
	private JRadioButton rdbtnFastaFile;
	
	private JTextField fieldOutFilename;
	private JButton btnBrowseOutputFile;
	
	private NumberFormat numberFormat;
	private JFormattedTextField fieldAccessionCol;
	private JFormattedTextField fieldDescriptionCol;
	private JFormattedTextField fieldGenomeNameCol;
	private JFormattedTextField fieldStartCol;
	private JFormattedTextField fieldEndCol;
	private JFormattedTextField fieldStrandCol;
	private JTextField fieldSeparator;
	
	private JTextField fieldGenomeName;
	private JTextField fieldAccessionRegex;
	private JTextField fieldDescriptionRegex;
	private JTextField fieldStartRegex;
	private JTextField fieldEndRegex;
	private JTextField fieldComplementRegex;
	private JTextField fieldForwardRegex;
	
	private JButton btnTestSettings;
	private JTable tableTesting;
	
	private JButton btnProcess;
	
	private JFileChooser fileChooser;
	
	
	/**
	 * Create the panel.
	 */
	public ParseProteinInformationPanel(JFileChooser fc) {
		this.fileChooser = fc;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 2};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel labelHeader = new JLabel("Parse protein information to GFF file");
		GridBagConstraints gbc_labelHeader = new GridBagConstraints();
		gbc_labelHeader.gridwidth = 3;
		gbc_labelHeader.insets = new Insets(0, 0, 5, 0);
		gbc_labelHeader.gridx = 0;
		gbc_labelHeader.gridy = 0;
		add(labelHeader, gbc_labelHeader);
		
		JLabel lblFilename = new JLabel("Filename:");
		GridBagConstraints gbc_lblFilename = new GridBagConstraints();
		gbc_lblFilename.anchor = GridBagConstraints.EAST;
		gbc_lblFilename.insets = new Insets(0, 0, 5, 5);
		gbc_lblFilename.gridx = 0;
		gbc_lblFilename.gridy = 1;
		add(lblFilename, gbc_lblFilename);
		
		fieldFilename = new JTextField();
		GridBagConstraints gbc_fieldFilename = new GridBagConstraints();
		gbc_fieldFilename.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldFilename.insets = new Insets(0, 0, 5, 5);
		gbc_fieldFilename.gridx = 1;
		gbc_fieldFilename.gridy = 1;
		add(fieldFilename, gbc_fieldFilename);
		fieldFilename.setColumns(10);
		
		btnBrowseInputFile = new JButton("Browse...");
		btnBrowseInputFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseInputFile = new GridBagConstraints();
		gbc_btnBrowseInputFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseInputFile.gridx = 2;
		gbc_btnBrowseInputFile.gridy = 1;
		add(btnBrowseInputFile, gbc_btnBrowseInputFile);
		
		JPanel panelFileButtons = new JPanel();
		GridBagConstraints gbc_panelFileButtons = new GridBagConstraints();
		gbc_panelFileButtons.insets = new Insets(0, 0, 5, 5);
		gbc_panelFileButtons.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelFileButtons.gridx = 1;
		gbc_panelFileButtons.gridy = 2;
		add(panelFileButtons, gbc_panelFileButtons);
		panelFileButtons.setLayout(new GridLayout(2, 0, 0, 0));
		
	    ButtonGroup fileTypeGroup = new ButtonGroup();
	    
		rdbtnTsvCsvFile = new JRadioButton("TSV/CSV file");
		rdbtnTsvCsvFile.addActionListener(this);
		panelFileButtons.add(rdbtnTsvCsvFile);
		rdbtnTsvCsvFile.setSelected(true);
		fileTypeGroup.add(rdbtnTsvCsvFile);
		
		rdbtnFastaFile = new JRadioButton("FASTA file");
		rdbtnFastaFile.addActionListener(this);
		panelFileButtons.add(rdbtnFastaFile);
	    fileTypeGroup.add(rdbtnFastaFile);
	    
	    JPanel panelLeftRight = new JPanel();
	    GridBagConstraints gbc_panelLeftRight = new GridBagConstraints();
	    gbc_panelLeftRight.gridwidth = 3;
	    gbc_panelLeftRight.insets = new Insets(0, 0, 5, 0);
	    gbc_panelLeftRight.fill = GridBagConstraints.BOTH;
	    gbc_panelLeftRight.gridx = 0;
	    gbc_panelLeftRight.gridy = 3;
	    add(panelLeftRight, gbc_panelLeftRight);
	    GridBagLayout gbl_panelLeftRight = new GridBagLayout();
	    gbl_panelLeftRight.columnWidths = new int[]{200, 200, 0};
	    gbl_panelLeftRight.rowHeights = new int[]{0, 0, 0};
	    gbl_panelLeftRight.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
	    gbl_panelLeftRight.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
	    panelLeftRight.setLayout(gbl_panelLeftRight);
	    
	    JPanel panelTsvCsvSettings = new JPanel();
	    panelTsvCsvSettings.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
	    GridBagConstraints gbc_panelTsvCsvSettings = new GridBagConstraints();
	    gbc_panelTsvCsvSettings.anchor = GridBagConstraints.NORTHEAST;
	    gbc_panelTsvCsvSettings.fill = GridBagConstraints.HORIZONTAL;
	    gbc_panelTsvCsvSettings.insets = new Insets(0, 0, 5, 5);
	    gbc_panelTsvCsvSettings.gridx = 0;
	    gbc_panelTsvCsvSettings.gridy = 0;
	    panelLeftRight.add(panelTsvCsvSettings, gbc_panelTsvCsvSettings);
	    GridBagLayout gbl_panelTsvCsvSettings = new GridBagLayout();
	    gbl_panelTsvCsvSettings.columnWidths = new int[]{170, 30, 0};
	    gbl_panelTsvCsvSettings.rowHeights = new int[]{0, 0, 15, 19, 15, 15, 15, 15, 15, 0};
	    gbl_panelTsvCsvSettings.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
	    gbl_panelTsvCsvSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
	    panelTsvCsvSettings.setLayout(gbl_panelTsvCsvSettings);
	    
	    
	    JLabel lblTsvcsvFileOptions = new JLabel("TSV/CSV file options");
	    GridBagConstraints gbc_lblTsvcsvFileOptions = new GridBagConstraints();
	    gbc_lblTsvcsvFileOptions.gridwidth = 2;
	    gbc_lblTsvcsvFileOptions.insets = new Insets(0, 0, 5, 0);
	    gbc_lblTsvcsvFileOptions.gridx = 0;
	    gbc_lblTsvcsvFileOptions.gridy = 0;
	    panelTsvCsvSettings.add(lblTsvcsvFileOptions, gbc_lblTsvcsvFileOptions);
	    
	    JLabel lblSeparatorChar = new JLabel("Separator Character:");
	    GridBagConstraints gbc_lblSeparatorChar = new GridBagConstraints();
	    gbc_lblSeparatorChar.anchor = GridBagConstraints.EAST;
	    gbc_lblSeparatorChar.insets = new Insets(0, 0, 5, 5);
	    gbc_lblSeparatorChar.gridx = 0;
	    gbc_lblSeparatorChar.gridy = 1;
	    panelTsvCsvSettings.add(lblSeparatorChar, gbc_lblSeparatorChar);
	    
	    fieldSeparator = new JTextField();
	    fieldSeparator.setText("\\t");
	    GridBagConstraints gbc_fieldSeparator = new GridBagConstraints();
	    gbc_fieldSeparator.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldSeparator.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldSeparator.gridx = 1;
	    gbc_fieldSeparator.gridy = 1;
	    panelTsvCsvSettings.add(fieldSeparator, gbc_fieldSeparator);
	    fieldSeparator.setColumns(10);
	    fieldSeparator.setEnabled(rdbtnTsvCsvFile.isSelected());
	    
	    JLabel lblColumnNumberHeader = new JLabel("Column number for");
	    GridBagConstraints gbc_lblColumnNumberHeader = new GridBagConstraints();
	    gbc_lblColumnNumberHeader.anchor = GridBagConstraints.WEST;
	    gbc_lblColumnNumberHeader.insets = new Insets(0, 0, 5, 0);
	    gbc_lblColumnNumberHeader.gridwidth = 2;
	    gbc_lblColumnNumberHeader.gridx = 0;
	    gbc_lblColumnNumberHeader.gridy = 2;
	    panelTsvCsvSettings.add(lblColumnNumberHeader, gbc_lblColumnNumberHeader);
	    
	    JLabel lblAccessionCol = new JLabel("Accession");
	    lblAccessionCol.setToolTipText("Column number for the protein accession");
	    GridBagConstraints gbc_lblAccessionCol = new GridBagConstraints();
	    gbc_lblAccessionCol.anchor = GridBagConstraints.EAST;
	    gbc_lblAccessionCol.insets = new Insets(0, 0, 5, 5);
	    gbc_lblAccessionCol.gridx = 0;
	    gbc_lblAccessionCol.gridy = 3;
	    panelTsvCsvSettings.add(lblAccessionCol, gbc_lblAccessionCol);
	    
	    
	    numberFormat = NumberFormat.getNumberInstance();
	    
	    fieldAccessionCol = new JFormattedTextField(numberFormat);
	    fieldAccessionCol.setText("1");
	    GridBagConstraints gbc_fieldAccessionCol = new GridBagConstraints();
	    gbc_fieldAccessionCol.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldAccessionCol.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldAccessionCol.gridx = 1;
	    gbc_fieldAccessionCol.gridy = 3;
	    panelTsvCsvSettings.add(fieldAccessionCol, gbc_fieldAccessionCol);
	    fieldAccessionCol.setColumns(10);
	    
	    JLabel lblDescriptionCol = new JLabel("Description");
	    lblDescriptionCol.setToolTipText("Column number for the protein description");
	    GridBagConstraints gbc_lblDescriptionCol = new GridBagConstraints();
	    gbc_lblDescriptionCol.anchor = GridBagConstraints.EAST;
	    gbc_lblDescriptionCol.insets = new Insets(0, 0, 5, 5);
	    gbc_lblDescriptionCol.gridx = 0;
	    gbc_lblDescriptionCol.gridy = 4;
	    panelTsvCsvSettings.add(lblDescriptionCol, gbc_lblDescriptionCol);
	    
	    fieldDescriptionCol = new JFormattedTextField(numberFormat);
	    fieldDescriptionCol.setText("2");
	    GridBagConstraints gbc_fieldDescriptionCol = new GridBagConstraints();
	    gbc_fieldDescriptionCol.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldDescriptionCol.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldDescriptionCol.gridx = 1;
	    gbc_fieldDescriptionCol.gridy = 4;
	    panelTsvCsvSettings.add(fieldDescriptionCol, gbc_fieldDescriptionCol);
	    fieldDescriptionCol.setColumns(10);
	    
	    JLabel lblGenomeNameCol = new JLabel("Genome Name");
	    lblGenomeNameCol.setToolTipText("Column number for the genome name");
	    GridBagConstraints gbc_lblGenomeNameCol = new GridBagConstraints();
	    gbc_lblGenomeNameCol.anchor = GridBagConstraints.EAST;
	    gbc_lblGenomeNameCol.insets = new Insets(0, 0, 5, 5);
	    gbc_lblGenomeNameCol.gridx = 0;
	    gbc_lblGenomeNameCol.gridy = 5;
	    panelTsvCsvSettings.add(lblGenomeNameCol, gbc_lblGenomeNameCol);
	    
	    fieldGenomeNameCol = new JFormattedTextField(numberFormat);
	    fieldGenomeNameCol.setText("3");
	    GridBagConstraints gbc_fieldGenomeNameCol = new GridBagConstraints();
	    gbc_fieldGenomeNameCol.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldGenomeNameCol.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldGenomeNameCol.gridx = 1;
	    gbc_fieldGenomeNameCol.gridy = 5;
	    panelTsvCsvSettings.add(fieldGenomeNameCol, gbc_fieldGenomeNameCol);
	    fieldGenomeNameCol.setColumns(10);
	    
	    JLabel lblStartCol = new JLabel("Start");
	    lblStartCol.setToolTipText("Column number for the start position in the genome");
	    GridBagConstraints gbc_lblStartCol = new GridBagConstraints();
	    gbc_lblStartCol.anchor = GridBagConstraints.EAST;
	    gbc_lblStartCol.insets = new Insets(0, 0, 5, 5);
	    gbc_lblStartCol.gridx = 0;
	    gbc_lblStartCol.gridy = 6;
	    panelTsvCsvSettings.add(lblStartCol, gbc_lblStartCol);
	    
	    fieldStartCol = new JFormattedTextField(numberFormat);
	    fieldStartCol.setText("4");
	    GridBagConstraints gbc_fieldStartCol = new GridBagConstraints();
	    gbc_fieldStartCol.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldStartCol.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldStartCol.gridx = 1;
	    gbc_fieldStartCol.gridy = 6;
	    panelTsvCsvSettings.add(fieldStartCol, gbc_fieldStartCol);
	    fieldStartCol.setColumns(10);
	    
	    JLabel lblEndCol = new JLabel("End");
	    lblEndCol.setToolTipText("Column number for the end position in the genome");
	    GridBagConstraints gbc_lblEndCol = new GridBagConstraints();
	    gbc_lblEndCol.anchor = GridBagConstraints.EAST;
	    gbc_lblEndCol.insets = new Insets(0, 0, 5, 5);
	    gbc_lblEndCol.gridx = 0;
	    gbc_lblEndCol.gridy = 7;
	    panelTsvCsvSettings.add(lblEndCol, gbc_lblEndCol);
	    
	    fieldEndCol = new JFormattedTextField(numberFormat);
	    fieldEndCol.setText("5");
	    GridBagConstraints gbc_fieldEndCol = new GridBagConstraints();
	    gbc_fieldEndCol.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldEndCol.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldEndCol.gridx = 1;
	    gbc_fieldEndCol.gridy = 7;
	    panelTsvCsvSettings.add(fieldEndCol, gbc_fieldEndCol);
	    fieldEndCol.setColumns(10);
	    
	    JLabel lblStrandCol = new JLabel("Strand");
	    lblStrandCol.setToolTipText("Column number for the strand information (+ or -)");
	    GridBagConstraints gbc_lblStrandCol = new GridBagConstraints();
	    gbc_lblStrandCol.anchor = GridBagConstraints.EAST;
	    gbc_lblStrandCol.insets = new Insets(0, 0, 0, 5);
	    gbc_lblStrandCol.gridx = 0;
	    gbc_lblStrandCol.gridy = 8;
	    panelTsvCsvSettings.add(lblStrandCol, gbc_lblStrandCol);
	    
	    fieldStrandCol = new JFormattedTextField(numberFormat);
	    fieldStrandCol.setText("6");
	    GridBagConstraints gbc_fieldStrandCol = new GridBagConstraints();
	    gbc_fieldStrandCol.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldStrandCol.gridx = 1;
	    gbc_fieldStrandCol.gridy = 8;
	    panelTsvCsvSettings.add(fieldStrandCol, gbc_fieldStrandCol);
	    fieldStrandCol.setColumns(10);
	    
	    JPanel panelTesting = new JPanel();
	    GridBagConstraints gbc_panelTesting = new GridBagConstraints();
	    gbc_panelTesting.gridheight = 2;
	    gbc_panelTesting.insets = new Insets(0, 0, 5, 0);
	    gbc_panelTesting.fill = GridBagConstraints.BOTH;
	    gbc_panelTesting.gridx = 1;
	    gbc_panelTesting.gridy = 0;
	    panelLeftRight.add(panelTesting, gbc_panelTesting);
	    GridBagLayout gbl_panelTesting = new GridBagLayout();
	    gbl_panelTesting.columnWidths = new int[]{31, 0};
	    gbl_panelTesting.rowHeights = new int[]{25, 0, 0};
	    gbl_panelTesting.columnWeights = new double[]{1.0, Double.MIN_VALUE};
	    gbl_panelTesting.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
	    panelTesting.setLayout(gbl_panelTesting);
	    
	    JScrollPane scrollPaneTesting = new JScrollPane();
	    GridBagConstraints gbc_scrollPaneTesting = new GridBagConstraints();
	    gbc_scrollPaneTesting.fill = GridBagConstraints.BOTH;
	    gbc_scrollPaneTesting.insets = new Insets(0, 0, 5, 0);
	    gbc_scrollPaneTesting.gridx = 0;
	    gbc_scrollPaneTesting.gridy = 0;
	    panelTesting.add(scrollPaneTesting, gbc_scrollPaneTesting);
	    
	    tableTesting = new JTable();
	    tableTesting.setFillsViewportHeight(true);
		String[] columnNames = {"Accession",
				"Description",
				"Genome name",
				"Start",
				"End",
				"strand"};
	    tableTesting.setModel(new DefaultTableModel(columnNames, 0));
	    scrollPaneTesting.setViewportView(tableTesting);
	    
	    btnTestSettings = new JButton("Test settings");
	    btnTestSettings.addActionListener(this);
	    GridBagConstraints gbc_btnTestSettings = new GridBagConstraints();
	    gbc_btnTestSettings.anchor = GridBagConstraints.NORTH;
	    gbc_btnTestSettings.gridx = 0;
	    gbc_btnTestSettings.gridy = 1;
	    panelTesting.add(btnTestSettings, gbc_btnTestSettings);
	    
	    JPanel panelFastaOptions = new JPanel();
	    panelFastaOptions.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
	    GridBagConstraints gbc_panelFastaOptions = new GridBagConstraints();
	    gbc_panelFastaOptions.anchor = GridBagConstraints.NORTHEAST;
	    gbc_panelFastaOptions.fill = GridBagConstraints.HORIZONTAL;
	    gbc_panelFastaOptions.insets = new Insets(0, 0, 5, 5);
	    gbc_panelFastaOptions.gridx = 0;
	    gbc_panelFastaOptions.gridy = 1;
	    panelLeftRight.add(panelFastaOptions, gbc_panelFastaOptions);
	    GridBagLayout gbl_panelFastaOptions = new GridBagLayout();
	    gbl_panelFastaOptions.columnWidths = new int[]{170, 150, 0};
	    gbl_panelFastaOptions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	    gbl_panelFastaOptions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
	    gbl_panelFastaOptions.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
	    panelFastaOptions.setLayout(gbl_panelFastaOptions);
	    
	    JLabel lblFastaFileOptions = new JLabel("FASTA file options");
	    GridBagConstraints gbc_lblFastaFileOptions = new GridBagConstraints();
	    gbc_lblFastaFileOptions.gridwidth = 2;
	    gbc_lblFastaFileOptions.insets = new Insets(0, 0, 5, 0);
	    gbc_lblFastaFileOptions.gridx = 0;
	    gbc_lblFastaFileOptions.gridy = 0;
	    panelFastaOptions.add(lblFastaFileOptions, gbc_lblFastaFileOptions);
	    
	    JLabel lblGenomeName = new JLabel("Genome Name:");
	    GridBagConstraints gbc_lblGenomeName = new GridBagConstraints();
	    gbc_lblGenomeName.anchor = GridBagConstraints.EAST;
	    gbc_lblGenomeName.insets = new Insets(0, 0, 5, 5);
	    gbc_lblGenomeName.gridx = 0;
	    gbc_lblGenomeName.gridy = 1;
	    panelFastaOptions.add(lblGenomeName, gbc_lblGenomeName);
	    
	    fieldGenomeName = new JTextField();
	    fieldGenomeName.setText("Chr");
	    GridBagConstraints gbc_fieldGenomeName = new GridBagConstraints();
	    gbc_fieldGenomeName.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldGenomeName.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldGenomeName.gridx = 1;
	    gbc_fieldGenomeName.gridy = 1;
	    panelFastaOptions.add(fieldGenomeName, gbc_fieldGenomeName);
	    fieldGenomeName.setColumns(10);
	    
	    JLabel lblRegularExpressionForHeader = new JLabel("Regular expression for");
	    GridBagConstraints gbc_lblRegularExpressionForHeader = new GridBagConstraints();
	    gbc_lblRegularExpressionForHeader.gridwidth = 2;
	    gbc_lblRegularExpressionForHeader.anchor = GridBagConstraints.WEST;
	    gbc_lblRegularExpressionForHeader.insets = new Insets(0, 0, 5, 0);
	    gbc_lblRegularExpressionForHeader.gridx = 0;
	    gbc_lblRegularExpressionForHeader.gridy = 2;
	    panelFastaOptions.add(lblRegularExpressionForHeader, gbc_lblRegularExpressionForHeader);
	    
	    JLabel lblAccessionRegex = new JLabel("Accession");
	    lblAccessionRegex.setToolTipText("Regular expression for the protein accession");
	    GridBagConstraints gbc_lblAccessionRegex = new GridBagConstraints();
	    gbc_lblAccessionRegex.anchor = GridBagConstraints.EAST;
	    gbc_lblAccessionRegex.insets = new Insets(0, 0, 5, 5);
	    gbc_lblAccessionRegex.gridx = 0;
	    gbc_lblAccessionRegex.gridy = 3;
	    panelFastaOptions.add(lblAccessionRegex, gbc_lblAccessionRegex);
	    
	    fieldAccessionRegex = new JTextField();
	    fieldAccessionRegex.setText(">([^ ]+) .*$");
	    GridBagConstraints gbc_fieldAccessionRegex = new GridBagConstraints();
	    gbc_fieldAccessionRegex.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldAccessionRegex.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldAccessionRegex.gridx = 1;
	    gbc_fieldAccessionRegex.gridy = 3;
	    panelFastaOptions.add(fieldAccessionRegex, gbc_fieldAccessionRegex);
	    fieldAccessionRegex.setColumns(10);
	    
	    JLabel lblDescriptionRegex = new JLabel("Description");
	    lblDescriptionRegex.setToolTipText("Regular expression for the protein description");
	    GridBagConstraints gbc_lblDescriptionRegex = new GridBagConstraints();
	    gbc_lblDescriptionRegex.anchor = GridBagConstraints.EAST;
	    gbc_lblDescriptionRegex.insets = new Insets(0, 0, 5, 5);
	    gbc_lblDescriptionRegex.gridx = 0;
	    gbc_lblDescriptionRegex.gridy = 4;
	    panelFastaOptions.add(lblDescriptionRegex, gbc_lblDescriptionRegex);
	    
	    fieldDescriptionRegex = new JTextField();
	    fieldDescriptionRegex.setText(">[^{]*\\{[^}]*} (.*)$");
	    GridBagConstraints gbc_fieldDescriptionRegex = new GridBagConstraints();
	    gbc_fieldDescriptionRegex.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldDescriptionRegex.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldDescriptionRegex.gridx = 1;
	    gbc_fieldDescriptionRegex.gridy = 4;
	    panelFastaOptions.add(fieldDescriptionRegex, gbc_fieldDescriptionRegex);
	    fieldDescriptionRegex.setColumns(10);
	    
	    JLabel lblStartRegex = new JLabel("Start");
	    lblStartRegex.setToolTipText("Regular expression for the start in the genome");
	    GridBagConstraints gbc_lblStartRegex = new GridBagConstraints();
	    gbc_lblStartRegex.anchor = GridBagConstraints.EAST;
	    gbc_lblStartRegex.insets = new Insets(0, 0, 5, 5);
	    gbc_lblStartRegex.gridx = 0;
	    gbc_lblStartRegex.gridy = 5;
	    panelFastaOptions.add(lblStartRegex, gbc_lblStartRegex);
	    
	    fieldStartRegex = new JTextField();
	    fieldStartRegex.setText(">[^{]*\\{(-*\\d+) .*$");
	    GridBagConstraints gbc_fieldStartRegex = new GridBagConstraints();
	    gbc_fieldStartRegex.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldStartRegex.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldStartRegex.gridx = 1;
	    gbc_fieldStartRegex.gridy = 5;
	    panelFastaOptions.add(fieldStartRegex, gbc_fieldStartRegex);
	    fieldStartRegex.setColumns(10);
	    
	    JLabel lblEndRegex = new JLabel("End");
	    lblEndRegex.setToolTipText("Regular expression for the end of the protein in the genome");
	    GridBagConstraints gbc_lblEndRegex = new GridBagConstraints();
	    gbc_lblEndRegex.anchor = GridBagConstraints.EAST;
	    gbc_lblEndRegex.insets = new Insets(0, 0, 5, 5);
	    gbc_lblEndRegex.gridx = 0;
	    gbc_lblEndRegex.gridy = 6;
	    panelFastaOptions.add(lblEndRegex, gbc_lblEndRegex);
	    
	    fieldEndRegex = new JTextField();
	    fieldEndRegex.setText(">[^{]*\\{-*\\d+ - (\\d+) .*$");
	    GridBagConstraints gbc_fieldEndRegex = new GridBagConstraints();
	    gbc_fieldEndRegex.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldEndRegex.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldEndRegex.gridx = 1;
	    gbc_fieldEndRegex.gridy = 6;
	    panelFastaOptions.add(fieldEndRegex, gbc_fieldEndRegex);
	    fieldEndRegex.setColumns(10);
	    
	    JLabel lblForwardReadRegex = new JLabel("Forward");
	    lblForwardReadRegex.setToolTipText("Regular expression which must match, if the entry is on the forward strand");
	    GridBagConstraints gbc_lblDirectReadRegex = new GridBagConstraints();
	    gbc_lblDirectReadRegex.anchor = GridBagConstraints.EAST;
	    gbc_lblDirectReadRegex.insets = new Insets(0, 0, 5, 5);
	    gbc_lblDirectReadRegex.gridx = 0;
	    gbc_lblDirectReadRegex.gridy = 7;
	    panelFastaOptions.add(lblForwardReadRegex, gbc_lblDirectReadRegex);
	    
	    fieldForwardRegex = new JTextField();
	    fieldForwardRegex.setText(">[^{]*\\{-*\\d+ - (\\d+) direct\\}.*$");
	    GridBagConstraints gbc_fieldDirectRegex = new GridBagConstraints();
	    gbc_fieldDirectRegex.insets = new Insets(0, 0, 5, 0);
	    gbc_fieldDirectRegex.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldDirectRegex.gridx = 1;
	    gbc_fieldDirectRegex.gridy = 7;
	    panelFastaOptions.add(fieldForwardRegex, gbc_fieldDirectRegex);
	    fieldForwardRegex.setColumns(10);
	    fieldForwardRegex.setEnabled(rdbtnFastaFile.isSelected());
	    
	    JLabel lblComplementRegex = new JLabel("Complement");
	    lblComplementRegex.setToolTipText("Regular expression which must match, if the entry is on the complementary strand");
	    GridBagConstraints gbc_lblComplementRegex = new GridBagConstraints();
	    gbc_lblComplementRegex.anchor = GridBagConstraints.EAST;
	    gbc_lblComplementRegex.insets = new Insets(0, 0, 0, 5);
	    gbc_lblComplementRegex.gridx = 0;
	    gbc_lblComplementRegex.gridy = 8;
	    panelFastaOptions.add(lblComplementRegex, gbc_lblComplementRegex);
	    
	    JLabel lblOutputGffFile = new JLabel("Output GFF file:");
	    GridBagConstraints gbc_lblOutputGffFile = new GridBagConstraints();
	    gbc_lblOutputGffFile.anchor = GridBagConstraints.EAST;
	    gbc_lblOutputGffFile.insets = new Insets(0, 0, 5, 5);
	    gbc_lblOutputGffFile.gridx = 0;
	    gbc_lblOutputGffFile.gridy = 4;
	    add(lblOutputGffFile, gbc_lblOutputGffFile);
	    
	    fieldOutFilename = new JTextField();
	    GridBagConstraints gbc_fieldOutFilename = new GridBagConstraints();
	    gbc_fieldOutFilename.insets = new Insets(0, 0, 5, 5);
	    gbc_fieldOutFilename.fill = GridBagConstraints.HORIZONTAL;
	    gbc_fieldOutFilename.gridx = 1;
	    gbc_fieldOutFilename.gridy = 4;
	    add(fieldOutFilename, gbc_fieldOutFilename);
	    fieldOutFilename.setColumns(10);
	    
	    btnBrowseOutputFile = new JButton("Browse...");
	    btnBrowseOutputFile.addActionListener(this);
	    GridBagConstraints gbc_btnBrowseOutputFile = new GridBagConstraints();
	    gbc_btnBrowseOutputFile.insets = new Insets(0, 0, 5, 0);
	    gbc_btnBrowseOutputFile.gridx = 2;
	    gbc_btnBrowseOutputFile.gridy = 4;
	    add(btnBrowseOutputFile, gbc_btnBrowseOutputFile);
	    
		btnProcess = new JButton("Process");
		btnProcess.addActionListener(this);
		GridBagConstraints gbc_btnProcess = new GridBagConstraints();
		gbc_btnProcess.gridwidth = 3;
		gbc_btnProcess.insets = new Insets(0, 0, 0, 5);
		gbc_btnProcess.gridx = 0;
		gbc_btnProcess.gridy = 5;
		add(btnProcess, gbc_btnProcess);
	    
		
	    // enable / disable specific fields
	    fieldSeparator.setEnabled(rdbtnTsvCsvFile.isSelected());
	    fieldAccessionCol.setEnabled(rdbtnTsvCsvFile.isSelected());
		fieldDescriptionCol.setEnabled(rdbtnTsvCsvFile.isSelected());
		fieldGenomeNameCol.setEnabled(rdbtnTsvCsvFile.isSelected());
		fieldStartCol.setEnabled(rdbtnTsvCsvFile.isSelected());
		fieldEndCol.setEnabled(rdbtnTsvCsvFile.isSelected());
		fieldStrandCol.setEnabled(rdbtnTsvCsvFile.isSelected());
		
		fieldGenomeName.setEnabled(rdbtnFastaFile.isSelected());
		fieldAccessionRegex.setEnabled(rdbtnFastaFile.isSelected());
		fieldDescriptionRegex.setEnabled(rdbtnFastaFile.isSelected());
		fieldStartRegex.setEnabled(rdbtnFastaFile.isSelected());
		fieldEndRegex.setEnabled(rdbtnFastaFile.isSelected());
		
		fieldComplementRegex = new JTextField();
		fieldComplementRegex.setText(">[^{]*\\{-*\\d+ - (\\d+) complement\\}.*$");
		GridBagConstraints gbc_fieldComplementRegex = new GridBagConstraints();
		gbc_fieldComplementRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldComplementRegex.gridx = 1;
		gbc_fieldComplementRegex.gridy = 8;
		panelFastaOptions.add(fieldComplementRegex, gbc_fieldComplementRegex);
		fieldComplementRegex.setColumns(10);
		fieldComplementRegex.setEnabled(rdbtnFastaFile.isSelected());
	}
	
	
	/**
	 * Get the ToolTip text for this panel.
	 * 
	 * @return
	 */
	public static String getToolTip() {
		return "Reads in protein information either from a TSV/CSV " +
		"file or a database in FASTA format, which should " +
		"contain the reading frame positions of the " +
		"proteins in the header. With this information, a GFF3 " +
		"file for the \"known proteins\" is created.";
	}
	
	
	/**
	 * Action listener 
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == btnBrowseInputFile) {
			// Handle browse input file
			fileChooser.setMultiSelectionEnabled(false);
			int returnVal = fileChooser.showOpenDialog(
					ParseProteinInformationPanel.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				
				fieldFilename.setText(file.getAbsolutePath());
			}
		} else if (e.getSource() == btnBrowseOutputFile) {
			// Handle browse output file
			fileChooser.setMultiSelectionEnabled(false);
			int returnVal = fileChooser.showSaveDialog(
					ParseProteinInformationPanel.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				
				fieldOutFilename.setText(file.getAbsolutePath());
			}
		} else if (e.getSource() == btnTestSettings) {
			// handle test settings pressed
			testSettings();
		} else if (e.getSource() == btnProcess) {
			// handle process pressed
			
			// TODO: put a thread here, which handles the processing
			
			btnProcess.setEnabled(false);
			if (rdbtnTsvCsvFile.isSelected()) {
				processTsvCsvFile();
			} else if (rdbtnFastaFile.isSelected()) {
				processFASTAFile();
			}
			btnProcess.setEnabled(true);
		} else if ((e.getSource() == rdbtnFastaFile) ||
				(e.getSource() == rdbtnTsvCsvFile)) {
			fieldSeparator.setEnabled(rdbtnTsvCsvFile.isSelected());
			fieldAccessionCol.setEnabled(rdbtnTsvCsvFile.isSelected());
			fieldDescriptionCol.setEnabled(rdbtnTsvCsvFile.isSelected());
			fieldGenomeNameCol.setEnabled(rdbtnTsvCsvFile.isSelected());
			fieldStartCol.setEnabled(rdbtnTsvCsvFile.isSelected());
			fieldEndCol.setEnabled(rdbtnTsvCsvFile.isSelected());
			fieldStrandCol.setEnabled(rdbtnTsvCsvFile.isSelected());
			
			fieldGenomeName.setEnabled(rdbtnFastaFile.isSelected());
			fieldAccessionRegex.setEnabled(rdbtnFastaFile.isSelected());
			fieldDescriptionRegex.setEnabled(rdbtnFastaFile.isSelected());
			fieldStartRegex.setEnabled(rdbtnFastaFile.isSelected());
			fieldEndRegex.setEnabled(rdbtnFastaFile.isSelected());
			fieldForwardRegex.setEnabled(rdbtnFastaFile.isSelected());
			fieldComplementRegex.setEnabled(rdbtnFastaFile.isSelected());
		}
	}
	
	
	/**
	 * Test the current settings
	 */
	private void testSettings() {
		logger.info("start testing the settings");
		
		List<GenericProtein> testProteins = null;
		
		if (rdbtnTsvCsvFile.isSelected()) {
			testProteins = testTsvCsvSettings();
		} else if (rdbtnFastaFile.isSelected()) {
			testProteins = testFASTASettings();
		}
		
		for (int i=tableTesting.getModel().getRowCount()-1; i >= 0; i--) {
			((DefaultTableModel)tableTesting.getModel()).removeRow(i);
		}
		
		if (testProteins != null) {
			logger.info("Parsed " + testProteins.size() + " proteins");
			
			for (GenericProtein protein : testProteins) {
				Object[] proteinData = new Object[6];
				
				proteinData[0] = protein.getAccession();
				proteinData[1] = protein.getDescription();
				proteinData[2] = protein.getGenomeName();
				proteinData[3] = protein.getStart();
				proteinData[4] = protein.getEnd();
				proteinData[5] = (protein.getIsComplement() != null) ?
						(protein.getIsComplement() ? "-" : "+") : "null";
				
				((DefaultTableModel)tableTesting.getModel()).addRow(proteinData);
			}
		}
	}
	
	
	/**
	 * Test the TSV/CSV settings.
	 * 
	 * @return
	 */
	private List<GenericProtein> testTsvCsvSettings() {
		logger.info("testing TSV/CSV file parsing");
		
		try {
			Integer accessionCol;
			Integer descriptionCol;
			Integer genomeCol;
			Integer startCol;
			Integer endCol;
			Integer strandCol;
			
			accessionCol = Integer.parseInt(fieldAccessionCol.getText());
			descriptionCol = Integer.parseInt(fieldDescriptionCol.getText());
			genomeCol = Integer.parseInt(fieldGenomeNameCol.getText());
			startCol = Integer.parseInt(fieldStartCol.getText());
			endCol = Integer.parseInt(fieldEndCol.getText());
			strandCol = Integer.parseInt(fieldStrandCol.getText());
			
			ParseProteinInformation parser =
					new ParseProteinInformation(
							accessionCol, descriptionCol, genomeCol, startCol,
							endCol, strandCol);
			
			
			String separatorString = fieldSeparator.getText();
			if (separatorString.startsWith("\\")) {
				if (separatorString.startsWith("\\t")) {
					separatorString = "\t";
				} else if (separatorString.startsWith("\\b")) {
					separatorString = "\b";
				} else if (separatorString.startsWith("\\n")) {
					separatorString = "\n";
				} else if (separatorString.startsWith("\\r")) {
					separatorString = "\r";
				} else if (separatorString.startsWith("\\f")) {
					separatorString = "\f";
				}
			}
			parser.setSeparator(separatorString.charAt(0));
			
			return parser.testTXTFile(fieldFilename.getText(), 25);
		} catch (NumberFormatException e) {
			logger.error("Could not parse setting", e);
		} catch (IOException e) {
			logger.error("Could not parse file", e);
		} catch (Exception e) {
			logger.error("Could not parse file", e);
		}
		
		return null;
	}
	
	
	/**
	 * Test the FASTA settings.
	 * 
	 * @return
	 */
	private List<GenericProtein> testFASTASettings() {
		logger.info("testing Fasta file parsing");
		
		try {
			ParseProteinInformation parser =
					new ParseProteinInformation(
							fieldStartRegex.getText(),
							fieldEndRegex.getText(),
							fieldForwardRegex.getText(),
							fieldComplementRegex.getText(),
							fieldAccessionRegex.getText(),
							fieldDescriptionRegex.getText());
			
			return parser.testFASTAFile(fieldFilename.getText(),
					fieldGenomeName.getText(), 25);
		} catch (IOException e) {
			logger.error("Could not parse file", e);
		}
		
		return null;
	}
	
	
	/**
	 * Process a TSV/CSV file
	 */
	private void processTsvCsvFile() {
		logger.info("start processing CSV/TSV file");
		try {
			Integer accessionCol;
			Integer descriptionCol;
			Integer genomeCol;
			Integer startCol;
			Integer endCol;
			Integer strandCol;
			
			accessionCol = Integer.parseInt(fieldAccessionCol.getText());
			descriptionCol = Integer.parseInt(fieldDescriptionCol.getText());
			genomeCol = Integer.parseInt(fieldGenomeNameCol.getText());
			startCol = Integer.parseInt(fieldStartCol.getText());
			endCol = Integer.parseInt(fieldEndCol.getText());
			strandCol = Integer.parseInt(fieldStrandCol.getText());
			
			ParseProteinInformation parser =
					new ParseProteinInformation(
							accessionCol, descriptionCol, genomeCol, startCol,
							endCol, strandCol);
			
			String separatorString = fieldSeparator.getText();
			if (separatorString.startsWith("\\")) {
				if (separatorString.startsWith("\\t")) {
					separatorString = "\t";
				} else if (separatorString.startsWith("\\b")) {
					separatorString = "\b";
				} else if (separatorString.startsWith("\\n")) {
					separatorString = "\n";
				} else if (separatorString.startsWith("\\r")) {
					separatorString = "\r";
				} else if (separatorString.startsWith("\\f")) {
					separatorString = "\f";
				}
			}
			parser.setSeparator(separatorString.charAt(0));
			
			parser.parseTXTFile(fieldFilename.getText(),
					fieldOutFilename.getText());
		} catch (NumberFormatException e) {
			logger.error("Could not parse setting", e);
		} catch (IOException e) {
			logger.error("Could not parse file", e);
		}
	}
	
	
	/**
	 * Process a FASTA file
	 */
	private void processFASTAFile() {
		logger.info("start processing FASTA file");
		
		try {
			ParseProteinInformation parser =
					new ParseProteinInformation(
							fieldStartRegex.getText(),
							fieldEndRegex.getText(),
							fieldForwardRegex.getText(),
							fieldComplementRegex.getText(),
							fieldAccessionRegex.getText(),
							fieldDescriptionRegex.getText());
			
			parser.parseFASTAFile(fieldFilename.getText(),
					fieldOutFilename.getText(), fieldGenomeName.getText());
		} catch (IOException e) {
			logger.error("Could not parse file", e);
		}
	}
}
