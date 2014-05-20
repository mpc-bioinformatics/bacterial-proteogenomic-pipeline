package de.mpc.proteogenomics.pipeline.gui;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.RowFilter;

import java.awt.Graphics;
import java.awt.GridBagLayout;

import javax.swing.JTable;

import java.awt.GridBagConstraints;

import javax.swing.JScrollPane;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.CombineIdentificationResults;
import de.mpc.proteogenomics.pipeline.peptide.IdentifiedPeptide;
import de.mpc.proteogenomics.pipeline.protein.AbstractProtein;

import javax.swing.JButton;

import java.io.File;
import java.io.IOException;


public class AnalysisPanel extends JPanel
		implements ActionListener, ListSelectionListener {
	
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = Logger.getLogger(AnalysisPanel.class);
	
	private JFileChooser fileChooser;
	
	private CombineIdentificationResults results;
	
	private JButton btnOpen;
	private JButton btnClose;
	private JButton btnSave;
	private JButton btnExportToGff;
	private JButton btnExportToTsv;
	
	private JTable tableResults;
	private TableRowSorter<ResultsTableModel> tableSorter;
	
	private JCheckBox chckbxShowNormalizedCounts;
	private JCheckBox chckbxOnlyPseudoProteins;
	private JCheckBox chckbxElongationProteins;
	private JCheckBox chckbxOnlyStandaloneProteins;
	private JTextField textSequenceFilter;
	private JTextField textAccessionFilter;
	private JTextField textSeqIDFilter;
	private JTextField textMinimalIdentificationsFilter;
	private JLabel lblNrFilteredPeptides;
	
	private StatisticsPanel panelStatistics;
	private JEditorPane proteinSequencePanel;
	
	private JTextField textIdentificationsFileName;
	private JButton btnBrowseIdentificationsFile;
	private JTextField textIdentificationsGroup;
	private JButton btnAddIdentification;
	
	
	/**
	 * Create the panel.
	 */
	public AnalysisPanel(JFileChooser fc) {
		this.fileChooser = fc;
		this.results = null;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel panelButtons = new JPanel();
		GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.anchor = GridBagConstraints.WEST;
		gbc_panelButtons.insets = new Insets(0, 0, 5, 0);
		gbc_panelButtons.gridx = 0;
		gbc_panelButtons.gridy = 0;
		add(panelButtons, gbc_panelButtons);
		GridBagLayout gbl_panelButtons = new GridBagLayout();
		gbl_panelButtons.columnWidths = new int[]{72, 73, 68, 0, 129, 129, 0};
		gbl_panelButtons.rowHeights = new int[]{25, 0};
		gbl_panelButtons.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelButtons.setLayout(gbl_panelButtons);
		
		btnExportToTsv = new JButton("export to TSV");
		btnExportToTsv.addActionListener(this);
		
		btnOpen = new JButton("Open");
		btnOpen.addActionListener(this);
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.BOTH;
		gbc_btnOpen.insets = new Insets(0, 0, 0, 5);
		gbc_btnOpen.gridx = 0;
		gbc_btnOpen.gridy = 0;
		panelButtons.add(btnOpen, gbc_btnOpen);
		
		btnClose = new JButton("Close");
		btnClose.addActionListener(this);
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.fill = GridBagConstraints.BOTH;
		gbc_btnClose.insets = new Insets(0, 0, 0, 5);
		gbc_btnClose.gridx = 1;
		gbc_btnClose.gridy = 0;
		panelButtons.add(btnClose, gbc_btnClose);
		
		btnExportToGff = new JButton("export to GFF");
		btnExportToGff.addActionListener(this);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(this);
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.BOTH;
		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
		gbc_btnSave.gridx = 2;
		gbc_btnSave.gridy = 0;
		panelButtons.add(btnSave, gbc_btnSave);
		GridBagConstraints gbc_btnExportToGff = new GridBagConstraints();
		gbc_btnExportToGff.insets = new Insets(0, 0, 0, 5);
		gbc_btnExportToGff.gridx = 4;
		gbc_btnExportToGff.gridy = 0;
		panelButtons.add(btnExportToGff, gbc_btnExportToGff);
		GridBagConstraints gbc_btnExportToTsv = new GridBagConstraints();
		gbc_btnExportToTsv.gridx = 5;
		gbc_btnExportToTsv.gridy = 0;
		panelButtons.add(btnExportToTsv, gbc_btnExportToTsv);
		
		
		JSplitPane topdownSplitPane = new JSplitPane();
		topdownSplitPane.setResizeWeight(0.5);
		topdownSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
	    GridBagConstraints gbc_splitPane = new GridBagConstraints();
	    gbc_splitPane.fill = GridBagConstraints.BOTH;
	    gbc_splitPane.gridx = 0;
	    gbc_splitPane.gridy = 1;
	    add(topdownSplitPane, gbc_splitPane);
		
		
		JScrollPane scrollPane = new JScrollPane();
		topdownSplitPane.setTopComponent(scrollPane);
		
		tableResults = new JTable();
		tableResults.setFillsViewportHeight(true);
		scrollPane.setViewportView(tableResults);
		tableResults.setModel(new ResultsTableModel(null));
		
		tableSorter = new TableRowSorter<ResultsTableModel>(
						(ResultsTableModel)tableResults.getModel());
		tableResults.setRowSorter(tableSorter);
		
		tableResults.getSelectionModel().addListSelectionListener(this);
		
		JSplitPane leftrightSplitPane = new JSplitPane();
		leftrightSplitPane.setResizeWeight(0.7);
		leftrightSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
	    topdownSplitPane.setBottomComponent(leftrightSplitPane);
	    
		
	    JSplitPane statisticsproteinSplitPane = new JSplitPane();
	    statisticsproteinSplitPane.setResizeWeight(0.3);
	    statisticsproteinSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
	    leftrightSplitPane.setLeftComponent(statisticsproteinSplitPane);
	    
		panelStatistics = new StatisticsPanel();
		statisticsproteinSplitPane.setTopComponent(panelStatistics);
		
		proteinSequencePanel = new JEditorPane("text/html", "");
		proteinSequencePanel.setEditable(false);
		JScrollPane proteinSequenceScrollPane = new JScrollPane();
		proteinSequenceScrollPane.setViewportView(proteinSequencePanel);
		statisticsproteinSplitPane.setBottomComponent(proteinSequenceScrollPane);
		
		JPanel panelRightBottom = new JPanel();
		leftrightSplitPane.setRightComponent(panelRightBottom);
		
		GridBagLayout gbl_panelRightBottom = new GridBagLayout();
		gbl_panelRightBottom.columnWidths = new int[]{0, 0};
		gbl_panelRightBottom.rowHeights = new int[]{0, 0, 0};
		gbl_panelRightBottom.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelRightBottom.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelRightBottom.setLayout(gbl_panelRightBottom);
		
		JPanel panelFilters = new JPanel();
		GridBagConstraints gbc_panelFilters = new GridBagConstraints();
		gbc_panelFilters.insets = new Insets(0, 0, 5, 0);
		gbc_panelFilters.fill = GridBagConstraints.BOTH;
		gbc_panelFilters.gridx = 0;
		gbc_panelFilters.gridy = 0;
		panelRightBottom.add(panelFilters, gbc_panelFilters);
		GridBagLayout gbl_panelFilters = new GridBagLayout();
		gbl_panelFilters.columnWidths = new int[]{0, 0, 0};
		gbl_panelFilters.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelFilters.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panelFilters.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelFilters.setLayout(gbl_panelFilters);
		
		JLabel lblFilter = new JLabel("Filter");
		GridBagConstraints gbc_lblFilter = new GridBagConstraints();
		gbc_lblFilter.gridwidth = 2;
		gbc_lblFilter.insets = new Insets(0, 0, 5, 0);
		gbc_lblFilter.gridx = 0;
		gbc_lblFilter.gridy = 0;
		panelFilters.add(lblFilter, gbc_lblFilter);
		
		chckbxShowNormalizedCounts = new JCheckBox("show normalized counts");
		chckbxShowNormalizedCounts.addActionListener(this);
		chckbxShowNormalizedCounts.setSelected(true);
		GridBagConstraints gbc_chckbxShowNormalizedCounts = new GridBagConstraints();
		gbc_chckbxShowNormalizedCounts.anchor = GridBagConstraints.WEST;
		gbc_chckbxShowNormalizedCounts.gridwidth = 2;
		gbc_chckbxShowNormalizedCounts.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxShowNormalizedCounts.gridx = 0;
		gbc_chckbxShowNormalizedCounts.gridy = 1;
		panelFilters.add(chckbxShowNormalizedCounts, gbc_chckbxShowNormalizedCounts);
		
		chckbxOnlyPseudoProteins = new JCheckBox("only pseudo proteins");
		chckbxOnlyPseudoProteins.addActionListener(this);
		GridBagConstraints gbc_chckbxOnlyPseudoProteins = new GridBagConstraints();
		gbc_chckbxOnlyPseudoProteins.anchor = GridBagConstraints.WEST;
		gbc_chckbxOnlyPseudoProteins.gridwidth = 2;
		gbc_chckbxOnlyPseudoProteins.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxOnlyPseudoProteins.gridx = 0;
		gbc_chckbxOnlyPseudoProteins.gridy = 2;
		panelFilters.add(chckbxOnlyPseudoProteins, gbc_chckbxOnlyPseudoProteins);
		
		chckbxElongationProteins = new JCheckBox("elongation proteins");
		chckbxElongationProteins.addActionListener(this);
		GridBagConstraints gbc_chckbxElongationProteins = new GridBagConstraints();
		gbc_chckbxElongationProteins.anchor = GridBagConstraints.WEST;
		gbc_chckbxElongationProteins.gridwidth = 2;
		gbc_chckbxElongationProteins.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxElongationProteins.gridx = 0;
		gbc_chckbxElongationProteins.gridy = 3;
		panelFilters.add(chckbxElongationProteins, gbc_chckbxElongationProteins);
		
		chckbxOnlyStandaloneProteins = new JCheckBox("only standalone");
		chckbxOnlyStandaloneProteins.addActionListener(this);
		GridBagConstraints gbc_chckbxOnlyStandaloneProteins = new GridBagConstraints();
		gbc_chckbxOnlyStandaloneProteins.anchor = GridBagConstraints.WEST;
		gbc_chckbxOnlyStandaloneProteins.gridwidth = 2;
		gbc_chckbxOnlyStandaloneProteins.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxOnlyStandaloneProteins.gridx = 0;
		gbc_chckbxOnlyStandaloneProteins.gridy = 4;
		panelFilters.add(chckbxOnlyStandaloneProteins, gbc_chckbxOnlyStandaloneProteins);
		
		JLabel lblSequence = new JLabel("Sequence");
		GridBagConstraints gbc_lblSequence = new GridBagConstraints();
		gbc_lblSequence.anchor = GridBagConstraints.EAST;
		gbc_lblSequence.insets = new Insets(0, 0, 5, 5);
		gbc_lblSequence.gridx = 0;
		gbc_lblSequence.gridy = 5;
		panelFilters.add(lblSequence, gbc_lblSequence);
		
		textSequenceFilter = new JTextField();
		GridBagConstraints gbc_textSequenceFilter = new GridBagConstraints();
		gbc_textSequenceFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_textSequenceFilter.insets = new Insets(0, 0, 5, 0);
		gbc_textSequenceFilter.gridx = 1;
		gbc_textSequenceFilter.gridy = 5;
		panelFilters.add(textSequenceFilter, gbc_textSequenceFilter);
		textSequenceFilter.addActionListener(this);
		textSequenceFilter.setColumns(10);
		
		JLabel lblAccession = new JLabel("Accession");
		GridBagConstraints gbc_lblAccession = new GridBagConstraints();
		gbc_lblAccession.anchor = GridBagConstraints.EAST;
		gbc_lblAccession.insets = new Insets(0, 0, 5, 5);
		gbc_lblAccession.gridx = 0;
		gbc_lblAccession.gridy = 6;
		panelFilters.add(lblAccession, gbc_lblAccession);
		
		textAccessionFilter = new JTextField();
		GridBagConstraints gbc_textAccessionFilter = new GridBagConstraints();
		gbc_textAccessionFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_textAccessionFilter.insets = new Insets(0, 0, 5, 0);
		gbc_textAccessionFilter.gridx = 1;
		gbc_textAccessionFilter.gridy = 6;
		panelFilters.add(textAccessionFilter, gbc_textAccessionFilter);
		textAccessionFilter.addActionListener(this);
		textAccessionFilter.setColumns(10);
		
		JLabel lblSeqid = new JLabel("seqID");
		GridBagConstraints gbc_lblSeqid = new GridBagConstraints();
		gbc_lblSeqid.anchor = GridBagConstraints.EAST;
		gbc_lblSeqid.insets = new Insets(0, 0, 5, 5);
		gbc_lblSeqid.gridx = 0;
		gbc_lblSeqid.gridy = 7;
		panelFilters.add(lblSeqid, gbc_lblSeqid);
		
		textSeqIDFilter = new JTextField();
		GridBagConstraints gbc_textSeqIDFilter = new GridBagConstraints();
		gbc_textSeqIDFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_textSeqIDFilter.insets = new Insets(0, 0, 5, 0);
		gbc_textSeqIDFilter.gridx = 1;
		gbc_textSeqIDFilter.gridy = 7;
		panelFilters.add(textSeqIDFilter, gbc_textSeqIDFilter);
		textSeqIDFilter.addActionListener(this);
		textSeqIDFilter.setColumns(10);
		
		JLabel lblMinIdentifications = new JLabel("min. # identifications");
		GridBagConstraints gbc_lblMinIdentifications = new GridBagConstraints();
		gbc_lblMinIdentifications.anchor = GridBagConstraints.EAST;
		gbc_lblMinIdentifications.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinIdentifications.gridx = 0;
		gbc_lblMinIdentifications.gridy = 8;
		panelFilters.add(lblMinIdentifications, gbc_lblMinIdentifications);
		
		textMinimalIdentificationsFilter = new JTextField();
		GridBagConstraints gbc_textMinimalIdentificationsFilter = new GridBagConstraints();
		gbc_textMinimalIdentificationsFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_textMinimalIdentificationsFilter.insets = new Insets(0, 0, 5, 0);
		gbc_textMinimalIdentificationsFilter.gridx = 1;
		gbc_textMinimalIdentificationsFilter.gridy = 8;
		panelFilters.add(textMinimalIdentificationsFilter, gbc_textMinimalIdentificationsFilter);
		textMinimalIdentificationsFilter.addActionListener(this);
		textMinimalIdentificationsFilter.setColumns(10);
		
		lblNrFilteredPeptides = new JLabel("nr. filtered peptides");
		GridBagConstraints gbc_lblNrFilteredPeptides = new GridBagConstraints();
		gbc_lblNrFilteredPeptides.anchor = GridBagConstraints.EAST;
		gbc_lblNrFilteredPeptides.gridwidth = 2;
		gbc_lblNrFilteredPeptides.gridx = 0;
		gbc_lblNrFilteredPeptides.gridy = 9;
		panelFilters.add(lblNrFilteredPeptides, gbc_lblNrFilteredPeptides);
		
		JPanel panelAddIdentification = new JPanel();
		GridBagConstraints gbc_panelAddIdentification = new GridBagConstraints();
		gbc_panelAddIdentification.fill = GridBagConstraints.BOTH;
		gbc_panelAddIdentification.gridx = 0;
		gbc_panelAddIdentification.gridy = 1;
		panelRightBottom.add(panelAddIdentification, gbc_panelAddIdentification);
		GridBagLayout gbl_panelAddIdentification = new GridBagLayout();
		gbl_panelAddIdentification.columnWidths = new int[]{0, 0, 0};
		gbl_panelAddIdentification.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panelAddIdentification.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panelAddIdentification.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelAddIdentification.setLayout(gbl_panelAddIdentification);
		
		JLabel lblAddIdentifications = new JLabel("Add Identifications");
		GridBagConstraints gbc_lblAddIdentifications = new GridBagConstraints();
		gbc_lblAddIdentifications.gridwidth = 2;
		gbc_lblAddIdentifications.insets = new Insets(0, 0, 5, 0);
		gbc_lblAddIdentifications.gridx = 0;
		gbc_lblAddIdentifications.gridy = 0;
		panelAddIdentification.add(lblAddIdentifications, gbc_lblAddIdentifications);
		
		textIdentificationsFileName = new JTextField();
		GridBagConstraints gbc_textIdentificationsFileName = new GridBagConstraints();
		gbc_textIdentificationsFileName.insets = new Insets(0, 0, 5, 5);
		gbc_textIdentificationsFileName.fill = GridBagConstraints.HORIZONTAL;
		gbc_textIdentificationsFileName.gridx = 0;
		gbc_textIdentificationsFileName.gridy = 1;
		panelAddIdentification.add(textIdentificationsFileName, gbc_textIdentificationsFileName);
		textIdentificationsFileName.setColumns(10);
		
		btnBrowseIdentificationsFile = new JButton("Browse...");
		btnBrowseIdentificationsFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseIdentificationsFile = new GridBagConstraints();
		gbc_btnBrowseIdentificationsFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseIdentificationsFile.gridx = 1;
		gbc_btnBrowseIdentificationsFile.gridy = 1;
		panelAddIdentification.add(btnBrowseIdentificationsFile, gbc_btnBrowseIdentificationsFile);
		
		textIdentificationsGroup = new JTextField();
		textIdentificationsGroup.setText("default");
		GridBagConstraints gbc_textIdentificationsGroup = new GridBagConstraints();
		gbc_textIdentificationsGroup.insets = new Insets(0, 0, 5, 5);
		gbc_textIdentificationsGroup.fill = GridBagConstraints.HORIZONTAL;
		gbc_textIdentificationsGroup.gridx = 0;
		gbc_textIdentificationsGroup.gridy = 2;
		panelAddIdentification.add(textIdentificationsGroup, gbc_textIdentificationsGroup);
		textIdentificationsGroup.setColumns(10);
		
		JLabel lblGroup = new JLabel("Group");
		GridBagConstraints gbc_lblGroup = new GridBagConstraints();
		gbc_lblGroup.insets = new Insets(0, 0, 5, 0);
		gbc_lblGroup.anchor = GridBagConstraints.WEST;
		gbc_lblGroup.gridx = 1;
		gbc_lblGroup.gridy = 2;
		panelAddIdentification.add(lblGroup, gbc_lblGroup);
		
		btnAddIdentification = new JButton("Add Identifications");
		btnAddIdentification.addActionListener(this);
		GridBagConstraints gbc_btnAddIdentification = new GridBagConstraints();
		gbc_btnAddIdentification.gridwidth = 2;
		gbc_btnAddIdentification.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddIdentification.gridx = 0;
		gbc_btnAddIdentification.gridy = 3;
		panelAddIdentification.add(btnAddIdentification, gbc_btnAddIdentification);
		
		
		deactivateAll();
	}
	
	
	/**
	 * Action listener
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOpen) {
			btnOpen.setEnabled(false);
			btnClose.setEnabled(false);
			btnSave.setEnabled(false);
			openFilePressed();
		} else if (e.getSource() == btnClose) {
			results = null;
			deactivateAll();
		} else if (e.getSource() == btnSave) {
			btnOpen.setEnabled(false);
			btnClose.setEnabled(false);
			btnSave.setEnabled(false);
			saveFilePressed();
			btnOpen.setEnabled(true);
			btnClose.setEnabled(true);
			btnSave.setEnabled(true);
		} else if (e.getSource() == chckbxShowNormalizedCounts) {
			if (results != null) {
				results.setNormalizeCounts(
						chckbxShowNormalizedCounts.isSelected());
				
				setupTableSorterAndModel();
				applyFilter();
			}
		} else if (e.getSource() == btnBrowseIdentificationsFile) {
			GUIHelper.browseFileForField(textIdentificationsFileName,
					fileChooser, AnalysisPanel.this);
		} else if (e.getSource() == btnAddIdentification) {
			addIdentificationPressed();
		} else if (e.getSource() == btnExportToGff) {
			exportGffPressed();
		} else if (e.getSource() == btnExportToTsv) {
			exportTsvPressed();
		} else if ((e.getSource() == chckbxOnlyPseudoProteins) ||
				(e.getSource() == chckbxElongationProteins) ||
				(e.getSource() == chckbxOnlyStandaloneProteins) ||
				(e.getSource() == textSequenceFilter) ||
				(e.getSource() == textAccessionFilter) ||
				(e.getSource() == textSeqIDFilter) ||
				(e.getSource() == textMinimalIdentificationsFilter)) {
			applyFilter();
			
			lblNrFilteredPeptides.setText(tableResults.getRowCount() +
					" filtered peptides");
		}
	}
	
	
	private void setupTableSorterAndModel() {
		ResultsTableModel model = new ResultsTableModel(results);
		tableSorter = new TableRowSorter<ResultsTableModel>(model);
		tableResults.setModel(model);
		tableResults.setRowSorter(tableSorter);
	}
	
	
	/**
	 * If a filter was changed, create the filters.
	 */
	private void applyFilter() {
		RowFilter<ResultsTableModel, Object> rf = null;
		
		if (chckbxOnlyPseudoProteins.isSelected() ||
				chckbxElongationProteins.isSelected() ||
				chckbxOnlyStandaloneProteins.isSelected() ||
				!textSequenceFilter.getText().trim().equals("") ||
				!textAccessionFilter.getText().trim().equals("") ||
				!textSeqIDFilter.getText().trim().equals("") ||
				!textMinimalIdentificationsFilter.getText().trim().equals("")) {
			
			rf = new RowFilter<AnalysisPanel.ResultsTableModel, Object>() {
				
				@Override
				public boolean include(
						RowFilter.Entry<? extends ResultsTableModel, ? extends Object> entry) {
					
					if (chckbxOnlyPseudoProteins.isSelected()) {
						if (!(Boolean)entry.getValue(ResultsTableModel.ONLY_PSEUDO_COL)) {
							return false;
						}
					}
					
					if (chckbxElongationProteins.isSelected()) {
						if (!(Boolean)entry.getValue(ResultsTableModel.IS_ELONGATION_COL)) {
							return false;
						}
					}
					
					if (chckbxOnlyStandaloneProteins.isSelected()) {
						if (!(Boolean)entry.getValue(ResultsTableModel.STANDALONE_COL)) {
							return false;
						}
					}
					
					if (!textSequenceFilter.getText().trim().equals("")) {
						if (!entry.getStringValue(ResultsTableModel.SEQUENCE_COL).
								toUpperCase().contains(textSequenceFilter.
										getText().trim().toUpperCase())) {
							return false;
						}
					}
					
					if (!textAccessionFilter.getText().trim().equals("")) {
						if (!entry.getStringValue(ResultsTableModel.PROTEIN_COL).
								toUpperCase().contains(textAccessionFilter.
										getText().trim().toUpperCase())) {
							return false;
						}
					}
					
					if (!textSeqIDFilter.getText().trim().equals("")) {
						if (!entry.getStringValue(ResultsTableModel.SEQID_COL).
								toUpperCase().contains(textSeqIDFilter.
										getText().trim().toUpperCase())) {
							return false;
						}
					}
					
					if (!textMinimalIdentificationsFilter.getText().trim().equals("")) {
						try {
							Integer minIDs = Integer.parseInt(
									textMinimalIdentificationsFilter.getText());
							
							Integer counts = (Integer)entry.getValue(
									ResultsTableModel.IDENTIFICATIONSCOUNT_COL);
							
							if (counts < minIDs) {
								return false;
							}
						} catch (NumberFormatException e) {
							logger.warn("Could not parse text to number!", e);
						}
					}
					
					return true;
				}
				
			};
			
		}
		
		tableSorter.setRowFilter(rf);
	}
	
	
	/**
	 * Deactivates all (except file opening)
	 */
	private void deactivateAll() {
		btnOpen.setEnabled(true);
		btnClose.setEnabled(false);
		btnSave.setEnabled(false);
		btnAddIdentification.setEnabled(false);
		btnExportToGff.setEnabled(false);
		btnExportToTsv.setEnabled(false);
		
		tableResults.setEnabled(false);
		tableResults.setRowSorter(null);
		tableResults.setModel(new ResultsTableModel(null));
		
		textSequenceFilter.setEnabled(false);
		textAccessionFilter.setEnabled(false);
		textSeqIDFilter.setEnabled(false);
		textMinimalIdentificationsFilter.setEnabled(false);
		
		chckbxShowNormalizedCounts.setEnabled(false);
		chckbxOnlyPseudoProteins.setEnabled(false);
		chckbxElongationProteins.setEnabled(false);
		chckbxOnlyStandaloneProteins.setEnabled(false);
		
		textIdentificationsFileName.setEnabled(false);
		btnBrowseIdentificationsFile.setEnabled(false);
		textIdentificationsGroup.setEnabled(false);
		btnAddIdentification.setEnabled(false);
		
		lblNrFilteredPeptides.setText("(nothing to filter)");
	}
	
	
	/**
	 * Activates all (except file opening)
	 */
	private void activateAll() {
		btnOpen.setEnabled(true);
		btnClose.setEnabled(true);
		btnSave.setEnabled(true);
		btnAddIdentification.setEnabled(true);
		btnExportToGff.setEnabled(true);
		btnExportToTsv.setEnabled(true);
		
		tableResults.setEnabled(true);
		tableResults.setRowSorter(tableSorter);
		
		textSequenceFilter.setEnabled(true);
		textAccessionFilter.setEnabled(true);
		textSeqIDFilter.setEnabled(true);
		textMinimalIdentificationsFilter.setEnabled(true);
		
		chckbxShowNormalizedCounts.setEnabled(true);
		chckbxOnlyPseudoProteins.setEnabled(true);
		chckbxElongationProteins.setEnabled(true);
		chckbxOnlyStandaloneProteins.setEnabled(true);
		
		textIdentificationsFileName.setEnabled(true);
		btnBrowseIdentificationsFile.setEnabled(true);
		textIdentificationsGroup.setEnabled(true);
		btnAddIdentification.setEnabled(true);
		
		lblNrFilteredPeptides.setText("(nothing to filter)");
	}
	
	
	/**
	 * The open file button was pressed
	 */
	private void openFilePressed() {
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showOpenDialog(AnalysisPanel.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			try {
				results = CombineIdentificationResults.loadFromFile(
								file.getAbsolutePath());
			} catch (Exception e) {
				results = null;
				logger.error(
						"Could not open results from " + file.getAbsolutePath(),
						e);
			}
			
			if (results != null) {
				results.setNormalizeCounts(
						chckbxShowNormalizedCounts.isSelected());
			}
		}
		
		if (results != null) {
			setupTableSorterAndModel();
			activateAll();
		} else {
			deactivateAll();
		}
	}
	
	
	/**
	 * The save file button was pressed
	 */
	private void saveFilePressed() {
		if (results == null) {
			logger.error("No data to save, aborting.");
			return;
		}
		
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showSaveDialog(AnalysisPanel.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			try {
				results.saveToFile(file.getAbsolutePath());
			} catch (Exception e) {
				logger.error(
						"Could not save results to " + file.getAbsolutePath(),
						e);
			}
		}
	}
	
	
	/**
	 * Add identification button was pressed.
	 */
	private void addIdentificationPressed() {
		if (results == null) {
			logger.error("No initial data to add identifications, aborting.");
			return;
		}
		
		String fileName = textIdentificationsFileName.getText();
		if (fileName.trim().length() < 1) {
			logger.error("No file specified!");
			return;
		}
		
		String groupName = textIdentificationsGroup.getText();
		if (groupName.trim().length() < 1) {
			logger.error("No group specified!");
			return;
		}
		
		try {
			results.parseMzTab(fileName, groupName);
			results.setNormalizeCounts(chckbxShowNormalizedCounts.isSelected());
			
			tableResults.setModel(new ResultsTableModel(results));
			tableSorter = new TableRowSorter<ResultsTableModel>(
					(ResultsTableModel)tableResults.getModel());
			activateAll();
		} catch (Exception e) {
			logger.error("Error while parsing the identification file.", e);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Export to GFF was pressed
	 */
	private void exportGffPressed() {
		if (results == null) {
			logger.error("No data to export, aborting.");
			return;
		}
		
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showSaveDialog(AnalysisPanel.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			try {
				results.writeIdentifiedPeptidesToGFF(
						file.getAbsolutePath() + "-pseudos.gff",
						file.getAbsolutePath() + "-others.gff");
			} catch (IOException e) {
				logger.error("Could not export results", e);
			}
		}
	}
	
	
	/**
	 * Export to TSV was pressed
	 */
	private void exportTsvPressed() {
		if (results == null) {
			logger.error("No data to export, aborting.");
			return;
		}
		
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showSaveDialog(AnalysisPanel.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			try {
				results.writeIdentifiedPeptidesToTSV(file.getAbsolutePath());
			} catch (IOException e) {
				logger.error("Could not export results", e);
			}
		}
	}
	
	
	/**
	 * Table listener changed.
	 */
	public void valueChanged(ListSelectionEvent e) {
		int viewRow = tableResults.getSelectedRow();
		
		if (viewRow < 0) {
			// Selection got filtered away.
			panelStatistics.clearIdentifications();
			proteinSequencePanel.setText("no selection");
		} else {
			int modelRow = tableResults.convertRowIndexToModel(viewRow);
			
			panelStatistics.setIdentifications(
					((ResultsTableModel)tableResults.getModel()).getIdentifications(modelRow));
			
			proteinSequencePanel.setText(
					((ResultsTableModel)tableResults.getModel()).getHTMLProteinSequences(modelRow));
		}
		
		panelStatistics.repaint();
	}
	
	
	/**
	 * The table model with {@link CombineIdentificationResults} as data.
	 * 
	 * @author julian
	 *
	 */
	private class ResultsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		/** the actual data, which will be shown */
		private CombineIdentificationResults data;
		
		/** maps from the position of the peptide in the table to its sequence */
		private Map<Integer, String> mapPositionToPeptide;
		
		/** maps from the position in the table to the group name */
		private Map<Integer, String> mapPositionToGroupName;
		
		/** names of the columns, most are static, except for the group names */
		private List<String> columnNames;
		
		// the column numbers of the shown data 
		public static final int SEQUENCE_COL = 0;
		public static final int SEQID_COL = 1;
		public static final int PROTEIN_COL = 2;
		public static final int ONLY_PSEUDO_COL = 3;
		public static final int IS_ELONGATION_COL = 4;
		public static final int STANDALONE_COL = 5;
		public static final int IDENTIFICATIONSCOUNT_COL = 6;
		public static final int FIRST_GROUP_COL = 7;
		
		
		public ResultsTableModel(CombineIdentificationResults results) {
			super();
			
			columnNames = new ArrayList<String>();
			columnNames.add("Sequence");
			columnNames.add("seqID");
			columnNames.add("Protein");
			columnNames.add("only pseudo");
			columnNames.add("is elongation");
			columnNames.add("standalone");
			columnNames.add("#identifications");
			
			this.data = results;
			
			if (this.data != null) {
				mapPositionToPeptide =
						new HashMap<Integer, String>(this.data.getPeptides().size());
				int pos = 0;
				for (Map.Entry<String, IdentifiedPeptide> pepIt
						: this.data.getPeptides().entrySet()) {
					mapPositionToPeptide.put(pos, pepIt.getKey());
					pos++;
				}
				
				mapPositionToGroupName =
						new HashMap<Integer, String>();
				for (String group : this.data.getGroups()) {
					mapPositionToGroupName.put(columnNames.size(), group);
					columnNames.add(group);
				}
			}
		}
		
		
		public int getColumnCount() {
			return columnNames.size();
		}
		
		
		public int getRowCount() {
			if (data == null) {
				return 0;
			} else {
				return data.getPeptides().size();
			}
		}
		
		
		public String getColumnName(int col) {
			return columnNames.get(col);
		}
		
		
		public Object getValueAt(int row, int col) {
			if (data == null) {
				return null;
			}
			
			String pepSequence = mapPositionToPeptide.get(row);
			IdentifiedPeptide peptide = data.getPeptides().get(pepSequence);
			if (peptide == null) {
				return null;
			}
			
			// first check the default rows
			switch (col) {
			case SEQUENCE_COL:
				return peptide.getSequence();
				
			case SEQID_COL:
				Set<String> seqIds = new HashSet<String>();
				for (AbstractProtein prot : peptide.getProteins()) {
					seqIds.add(prot.getGenomeName());
				}
				return seqIds;
				
			case PROTEIN_COL:
				Set<String> proteins = new HashSet<String>();
				for (AbstractProtein prot : peptide.getProteins()) {
					proteins.add(prot.getAccession());
				}
				return proteins;
				
			case ONLY_PSEUDO_COL:
				return peptide.getHasOnlyGenomeTranslations();
				
			case IS_ELONGATION_COL:
				return peptide.getIsElongation();
				
			case STANDALONE_COL:
				return peptide.getHasOnlyGenomeTranslations() &&
						!peptide.getIsElongation();
				
			case IDENTIFICATIONSCOUNT_COL:
				return peptide.getNrAllIdentifications();
			}
			
			if (col >= FIRST_GROUP_COL) {
				// no default column, try the groups
				String groupName = mapPositionToGroupName.get(col);
				return data.getNrIdentificationsInGroup(peptide, groupName);
			}
			
			return null;
		}
		
		
		/**
		 * Getter for the identifications of the peptide.
		 * @param rowNr
		 * @return
		 */
		public Map<String, Float> getIdentifications(int rowNr) {
			String pepSequence = mapPositionToPeptide.get(rowNr);
			IdentifiedPeptide peptide = data.getPeptides().get(pepSequence);
			if (peptide == null) {
				return null;
			}
			
			Map<String, Float> identifications =
					new HashMap<String, Float>(
							mapPositionToGroupName.values().size());
			for (String groupName : mapPositionToGroupName.values()) {
				identifications.put(groupName,
						data.getNrIdentificationsInGroup(peptide, groupName));
			}
			
			return identifications;
		}
		
		
		/**
		 * Returns the protein sequences of the selected row in nicely formatted
		 * HTML.
		 * 
		 * @param rowNr
		 * @return
		 */
		public String getHTMLProteinSequences(int rowNr) {
			StringBuilder sb = new StringBuilder();
			
			String pepSequence = mapPositionToPeptide.get(rowNr);
			IdentifiedPeptide peptide = data.getPeptides().get(pepSequence);
			if (peptide == null) {
				return null;
			}
			
			for (AbstractProtein protein : peptide.getProteins()) {
				sb.append("<div style=\"white-space:nowrap;\">");
				sb.append("<span style=\"font-weight:bold; color:darkblue;\">");
				sb.append(protein.getAccession());
				sb.append("</span> ");
				sb.append(protein.getDescription());
				sb.append("</div><div style=\"white-space:nowrap; font-family: monospace;\">");
				
				Set<Integer> pepOverlap =
						new HashSet<Integer>(protein.getSequence().length());
				
				Long protStart = protein.getStart();
				Long protEnd = protein.getEnd();
				String protSequence = protein.getSequence();
				
				for (String pepId
						: data.getProteinPeptideMap().get(protein.getAccession())) {
					IdentifiedPeptide pep = data.getPeptides().get(pepId);
					
					if ((protStart != null) && (protEnd != null)) {
						for (Map.Entry<Long, Long> posIt
								: pep.getProteinPositions(protein.getAccession()).entrySet()) {
							
							if ((posIt.getKey() == null) ||
									(posIt.getValue() == null)) {
								continue;
							}
							
							// start position in protein
							int start = protein.getIsComplement() ?
									(int)(protEnd - posIt.getValue()) / 3 - 1 :
									(int)(posIt.getKey() - protStart) / 3 - 1;
							
							for (int t=0; t < pep.getSequence().length(); t++) {
								pepOverlap.add(t+start);
							}
						}
					} else {
						// no protein position information
						
						if ((protSequence != null) &&
								(protein.getSequence().length() > 0)) {
							
							int start = protein.getSequence().indexOf(
									pep.getSequence());
							
							if (start > -1) {
								for (int t=0; t < pep.getSequence().length(); t++) {
									pepOverlap.add(t+start);
								}
							}
						}
					}
				}
				
				if ((protSequence != null) &&
						(protein.getSequence().length() > 0)) {
					int seqLength = protein.getSequence().length();
					for (int t=0; t < seqLength; t++) {
						if (pepOverlap.contains(t) && !pepOverlap.contains(t-1)) {
							sb.append("<span style=\"font-weight:bold;\">");
						}
						
						sb.append(protein.getSequence().charAt(t));
						
						if (pepOverlap.contains(t) && !pepOverlap.contains(t+1)) {
							sb.append("</span>");
						}
						
						if ((t+1)%60 == 0) {
							sb.append("<br/>");
						}
					}
					sb.append("</div><br/>");
				}
			}
			
			return sb.toString();
		}
		
		
		/**
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int c) {
			Object obj = getValueAt(0, c);
			if (obj != null) {
				return obj.getClass();
			} else {
				return null;
			}
		}
    }
	
	
	private class StatisticsPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		/** map from groupname to number of identifications */
		private Map<String, Float> identifications;
		
		public StatisticsPanel() {
			this.identifications = new HashMap<String, Float>();
		}
		
		
		/**
		 * Clears all identification data
		 */
		public void clearIdentifications() {
			identifications.clear();
		}
		
		
		/**
		 * Sets the identification data
		 * @param identifications
		 */
		public void setIdentifications(Map<String, Float> identifications) {
			this.identifications.putAll(identifications);
		}
		
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			if (identifications.size() > 0) {
				int step = this.getWidth() / (identifications.size() + 1);
				int center = step;
				
				// get maximal identifications
				float maxIDs = 0;
				for (Float ids : identifications.values()) {
					if (ids > maxIDs) {
						maxIDs = ids;
					}
				}
				
				// draw the identification bars
				float barStep = (float)(this.getHeight() - 80) / maxIDs;
				for (Map.Entry<String, Float> idIt
						: identifications.entrySet()) {
					g.drawString(idIt.getKey(),
							center-10, this.getHeight()-15);
					g.drawString(idIt.getValue().toString(),
							center-10, this.getHeight()-50-(int)(maxIDs*barStep));
					
					g.fillRect(center-10,
							this.getHeight()-30-(int)(idIt.getValue()*barStep),
							20, (int)(idIt.getValue()*barStep));
					
					center += step;
				}
			}
		}
	}
}
