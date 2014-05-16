package de.mpc.proteogenomics.pipeline.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;

import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.CombineIdentificationResults;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabException;

import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class CombineIdentificationResultsPanel extends JPanel
		implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	private final static Logger logger =
			Logger.getLogger(CombineIdentificationResultsPanel.class);
	
	private JFileChooser fileChooser;
	
	private JList listKnownProteins;
	private JButton btnKnownAddFile;
	private JButton btnKnownRemove;
	
	private JList listPseudoProteins;
	private JButton btnPseudoAddFile;
	private JButton btnPseudoRemoveFile;
	
	private JTable tableIdentifications;
	private JButton btnRemoveIdentifications;
	private JButton btnAddIdentifications;
	
	private JTextField fieldDecoyRegularExpression;
	
	private JTextField fieldFASTAfile;
	private JButton btnBrowseFASTAFile;
	
	private JTextField fieldResultsFile;
	private JButton btnBrowseResultsFile;
	private JButton btnProcess;
	
	
	/**
	 * Create the panel.
	 */
	public CombineIdentificationResultsPanel(JFileChooser fc) {
		this.fileChooser = fc;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 2.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblCombineIdentifications = new JLabel("Combine Identifications");
		GridBagConstraints gbc_lblCombineIdentifications = new GridBagConstraints();
		gbc_lblCombineIdentifications.gridwidth = 2;
		gbc_lblCombineIdentifications.insets = new Insets(0, 0, 5, 0);
		gbc_lblCombineIdentifications.gridx = 0;
		gbc_lblCombineIdentifications.gridy = 0;
		add(lblCombineIdentifications, gbc_lblCombineIdentifications);
		
		JPanel panelKnown = new JPanel();
		panelKnown.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelKnown = new GridBagConstraints();
		gbc_panelKnown.insets = new Insets(0, 0, 5, 5);
		gbc_panelKnown.fill = GridBagConstraints.BOTH;
		gbc_panelKnown.gridx = 0;
		gbc_panelKnown.gridy = 1;
		add(panelKnown, gbc_panelKnown);
		GridBagLayout gbl_panelKnown = new GridBagLayout();
		gbl_panelKnown.columnWidths = new int[]{0, 0, 0};
		gbl_panelKnown.rowHeights = new int[]{0, 100, 0, 0};
		gbl_panelKnown.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panelKnown.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		panelKnown.setLayout(gbl_panelKnown);
		
		JLabel lblKnownProteins = new JLabel("Known proteins");
		GridBagConstraints gbc_lblKnownProteins = new GridBagConstraints();
		gbc_lblKnownProteins.gridwidth = 2;
		gbc_lblKnownProteins.anchor = GridBagConstraints.WEST;
		gbc_lblKnownProteins.insets = new Insets(0, 0, 5, 0);
		gbc_lblKnownProteins.gridx = 0;
		gbc_lblKnownProteins.gridy = 0;
		panelKnown.add(lblKnownProteins, gbc_lblKnownProteins);
		
		JScrollPane scrollPaneKnown = new JScrollPane();
		GridBagConstraints gbc_scrollPaneKnown = new GridBagConstraints();
		gbc_scrollPaneKnown.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneKnown.gridwidth = 2;
		gbc_scrollPaneKnown.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPaneKnown.gridx = 0;
		gbc_scrollPaneKnown.gridy = 1;
		panelKnown.add(scrollPaneKnown, gbc_scrollPaneKnown);
		
		listKnownProteins = new JList(new DefaultListModel());
		scrollPaneKnown.setViewportView(listKnownProteins);
		listKnownProteins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		btnKnownAddFile = new JButton("Add file");
		btnKnownAddFile.addActionListener(this);
		GridBagConstraints gbc_btnKnownAddFile = new GridBagConstraints();
		gbc_btnKnownAddFile.anchor = GridBagConstraints.WEST;
		gbc_btnKnownAddFile.insets = new Insets(0, 0, 0, 5);
		gbc_btnKnownAddFile.gridx = 0;
		gbc_btnKnownAddFile.gridy = 2;
		panelKnown.add(btnKnownAddFile, gbc_btnKnownAddFile);
		
		btnKnownRemove = new JButton("Remove file");
		btnKnownRemove.addActionListener(this);
		GridBagConstraints gbc_btnKnownRemove = new GridBagConstraints();
		gbc_btnKnownRemove.anchor = GridBagConstraints.WEST;
		gbc_btnKnownRemove.gridx = 1;
		gbc_btnKnownRemove.gridy = 2;
		panelKnown.add(btnKnownRemove, gbc_btnKnownRemove);
		
		JPanel panelPseudo = new JPanel();
		panelPseudo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelPseudo = new GridBagConstraints();
		gbc_panelPseudo.insets = new Insets(0, 0, 5, 0);
		gbc_panelPseudo.fill = GridBagConstraints.BOTH;
		gbc_panelPseudo.gridx = 1;
		gbc_panelPseudo.gridy = 1;
		add(panelPseudo, gbc_panelPseudo);
		GridBagLayout gbl_panelPseudo = new GridBagLayout();
		gbl_panelPseudo.columnWidths = new int[]{0, 0, 0};
		gbl_panelPseudo.rowHeights = new int[]{15, 0, 0, 0};
		gbl_panelPseudo.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panelPseudo.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		panelPseudo.setLayout(gbl_panelPseudo);
		
		JLabel lblPseudoProteins = new JLabel("Pseudo proteins");
		GridBagConstraints gbc_lblPseudoProteins = new GridBagConstraints();
		gbc_lblPseudoProteins.anchor = GridBagConstraints.WEST;
		gbc_lblPseudoProteins.insets = new Insets(0, 0, 5, 0);
		gbc_lblPseudoProteins.gridwidth = 2;
		gbc_lblPseudoProteins.gridx = 0;
		gbc_lblPseudoProteins.gridy = 0;
		panelPseudo.add(lblPseudoProteins, gbc_lblPseudoProteins);
		
		JScrollPane scrollPanePseudo = new JScrollPane();
		GridBagConstraints gbc_scrollPanePseudo = new GridBagConstraints();
		gbc_scrollPanePseudo.fill = GridBagConstraints.BOTH;
		gbc_scrollPanePseudo.gridwidth = 2;
		gbc_scrollPanePseudo.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPanePseudo.gridx = 0;
		gbc_scrollPanePseudo.gridy = 1;
		panelPseudo.add(scrollPanePseudo, gbc_scrollPanePseudo);
		
		listPseudoProteins = new JList(new DefaultListModel());
		scrollPanePseudo.setViewportView(listPseudoProteins);
		listPseudoProteins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		btnPseudoAddFile = new JButton("Add file");
		btnPseudoAddFile.addActionListener(this);
		GridBagConstraints gbc_btnPseudoAddFile = new GridBagConstraints();
		gbc_btnPseudoAddFile.anchor = GridBagConstraints.WEST;
		gbc_btnPseudoAddFile.insets = new Insets(0, 0, 0, 5);
		gbc_btnPseudoAddFile.gridx = 0;
		gbc_btnPseudoAddFile.gridy = 2;
		panelPseudo.add(btnPseudoAddFile, gbc_btnPseudoAddFile);
		
		btnPseudoRemoveFile = new JButton("Remove File");
		btnPseudoRemoveFile.addActionListener(this);
		GridBagConstraints gbc_btnPseudoRemoveFile = new GridBagConstraints();
		gbc_btnPseudoRemoveFile.anchor = GridBagConstraints.WEST;
		gbc_btnPseudoRemoveFile.gridx = 1;
		gbc_btnPseudoRemoveFile.gridy = 2;
		panelPseudo.add(btnPseudoRemoveFile, gbc_btnPseudoRemoveFile);
		
		JPanel panelIdentifications = new JPanel();
		panelIdentifications.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelIdentifications = new GridBagConstraints();
		gbc_panelIdentifications.gridwidth = 2;
		gbc_panelIdentifications.insets = new Insets(0, 0, 5, 0);
		gbc_panelIdentifications.fill = GridBagConstraints.BOTH;
		gbc_panelIdentifications.gridx = 0;
		gbc_panelIdentifications.gridy = 2;
		add(panelIdentifications, gbc_panelIdentifications);
		GridBagLayout gbl_panelIdentifications = new GridBagLayout();
		gbl_panelIdentifications.columnWidths = new int[]{0, 0, 0};
		gbl_panelIdentifications.rowHeights = new int[]{0, 100, 0, 0, 0};
		gbl_panelIdentifications.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panelIdentifications.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		panelIdentifications.setLayout(gbl_panelIdentifications);
		
		JLabel lblIdentifications = new JLabel("Identifications");
		GridBagConstraints gbc_lblIdentifications = new GridBagConstraints();
		gbc_lblIdentifications.anchor = GridBagConstraints.WEST;
		gbc_lblIdentifications.gridwidth = 2;
		gbc_lblIdentifications.insets = new Insets(0, 0, 5, 0);
		gbc_lblIdentifications.gridx = 0;
		gbc_lblIdentifications.gridy = 0;
		panelIdentifications.add(lblIdentifications, gbc_lblIdentifications);
		
		JScrollPane scrollPaneIdentifications = new JScrollPane();
		GridBagConstraints gbc_scrollPaneIdentifications = new GridBagConstraints();
		gbc_scrollPaneIdentifications.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneIdentifications.gridwidth = 2;
		gbc_scrollPaneIdentifications.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPaneIdentifications.gridx = 0;
		gbc_scrollPaneIdentifications.gridy = 1;
		panelIdentifications.add(scrollPaneIdentifications, gbc_scrollPaneIdentifications);
		
		tableIdentifications = new JTable();
		tableIdentifications.setFillsViewportHeight(true);
		scrollPaneIdentifications.setViewportView(tableIdentifications);
		Object[] columnNames = {"File", "Assay"};
		tableIdentifications.setModel(new DefaultTableModel(columnNames, 0));
		
		btnAddIdentifications = new JButton("Add Identifications");
		btnAddIdentifications.addActionListener(this);
		GridBagConstraints gbc_btnAddIdentifications = new GridBagConstraints();
		gbc_btnAddIdentifications.anchor = GridBagConstraints.WEST;
		gbc_btnAddIdentifications.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddIdentifications.gridx = 0;
		gbc_btnAddIdentifications.gridy = 2;
		panelIdentifications.add(btnAddIdentifications, gbc_btnAddIdentifications);
		
		btnRemoveIdentifications = new JButton("Remove Identifications");
		btnRemoveIdentifications.addActionListener(this);
		GridBagConstraints gbc_btnRemoveIdentifications = new GridBagConstraints();
		gbc_btnRemoveIdentifications.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveIdentifications.anchor = GridBagConstraints.WEST;
		gbc_btnRemoveIdentifications.gridx = 1;
		gbc_btnRemoveIdentifications.gridy = 2;
		panelIdentifications.add(btnRemoveIdentifications, gbc_btnRemoveIdentifications);
		
		JPanel panelDecoyRegex = new JPanel();
		GridBagConstraints gbc_panelDecoyRegex = new GridBagConstraints();
		gbc_panelDecoyRegex.gridwidth = 2;
		gbc_panelDecoyRegex.insets = new Insets(0, 0, 0, 5);
		gbc_panelDecoyRegex.fill = GridBagConstraints.BOTH;
		gbc_panelDecoyRegex.gridx = 0;
		gbc_panelDecoyRegex.gridy = 3;
		panelIdentifications.add(panelDecoyRegex, gbc_panelDecoyRegex);
		GridBagLayout gbl_panelDecoyRegex = new GridBagLayout();
		gbl_panelDecoyRegex.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panelDecoyRegex.rowHeights = new int[]{0, 0, 0};
		gbl_panelDecoyRegex.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panelDecoyRegex.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panelDecoyRegex.setLayout(gbl_panelDecoyRegex);
		
		JLabel lblDecoyRegularExpression = new JLabel("Decoy regular expression:");
		GridBagConstraints gbc_lblDecoyRegularExpression = new GridBagConstraints();
		gbc_lblDecoyRegularExpression.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecoyRegularExpression.anchor = GridBagConstraints.EAST;
		gbc_lblDecoyRegularExpression.gridx = 0;
		gbc_lblDecoyRegularExpression.gridy = 0;
		panelDecoyRegex.add(lblDecoyRegularExpression, gbc_lblDecoyRegularExpression);
		
		fieldDecoyRegularExpression = new JTextField();
		fieldDecoyRegularExpression.setText("decoy.*");
		GridBagConstraints gbc_fieldDecoyRegularExpression = new GridBagConstraints();
		gbc_fieldDecoyRegularExpression.gridwidth = 2;
		gbc_fieldDecoyRegularExpression.insets = new Insets(0, 0, 5, 5);
		gbc_fieldDecoyRegularExpression.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldDecoyRegularExpression.gridx = 1;
		gbc_fieldDecoyRegularExpression.gridy = 0;
		panelDecoyRegex.add(fieldDecoyRegularExpression, gbc_fieldDecoyRegularExpression);
		fieldDecoyRegularExpression.setColumns(10);
		
		JLabel lblFastaFile = new JLabel("FASTA file:");
		GridBagConstraints gbc_lblFastaFile = new GridBagConstraints();
		gbc_lblFastaFile.anchor = GridBagConstraints.EAST;
		gbc_lblFastaFile.insets = new Insets(0, 0, 0, 5);
		gbc_lblFastaFile.gridx = 0;
		gbc_lblFastaFile.gridy = 1;
		panelDecoyRegex.add(lblFastaFile, gbc_lblFastaFile);
		
		fieldFASTAfile = new JTextField();
		GridBagConstraints gbc_fieldFASTAfile = new GridBagConstraints();
		gbc_fieldFASTAfile.insets = new Insets(0, 0, 0, 5);
		gbc_fieldFASTAfile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldFASTAfile.gridx = 1;
		gbc_fieldFASTAfile.gridy = 1;
		panelDecoyRegex.add(fieldFASTAfile, gbc_fieldFASTAfile);
		fieldFASTAfile.setColumns(10);
		
		btnBrowseFASTAFile = new JButton("Browse...");
		btnBrowseFASTAFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseFASTAFile = new GridBagConstraints();
		gbc_btnBrowseFASTAFile.gridx = 2;
		gbc_btnBrowseFASTAFile.gridy = 1;
		panelDecoyRegex.add(btnBrowseFASTAFile, gbc_btnBrowseFASTAFile);
		
		JPanel panelProcess = new JPanel();
		GridBagConstraints gbc_panelProcess = new GridBagConstraints();
		gbc_panelProcess.gridwidth = 2;
		gbc_panelProcess.fill = GridBagConstraints.BOTH;
		gbc_panelProcess.gridx = 0;
		gbc_panelProcess.gridy = 3;
		add(panelProcess, gbc_panelProcess);
		GridBagLayout gbl_panelProcess = new GridBagLayout();
		gbl_panelProcess.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panelProcess.rowHeights = new int[]{0, 0, 0};
		gbl_panelProcess.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panelProcess.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panelProcess.setLayout(gbl_panelProcess);
		
		
		JLabel lblResultsFile = new JLabel("Results File:");
		GridBagConstraints gbc_lblResultsFile = new GridBagConstraints();
		gbc_lblResultsFile.anchor = GridBagConstraints.EAST;
		gbc_lblResultsFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblResultsFile.gridx = 0;
		gbc_lblResultsFile.gridy = 0;
		panelProcess.add(lblResultsFile, gbc_lblResultsFile);
		
		fieldResultsFile = new JTextField();
		GridBagConstraints gbc_fieldResultsFile = new GridBagConstraints();
		gbc_fieldResultsFile.insets = new Insets(0, 0, 5, 5);
		gbc_fieldResultsFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_fieldResultsFile.gridx = 1;
		gbc_fieldResultsFile.gridy = 0;
		panelProcess.add(fieldResultsFile, gbc_fieldResultsFile);
		fieldResultsFile.setColumns(10);
		
		btnBrowseResultsFile = new JButton("Browse...");
		btnBrowseResultsFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseResultsFile = new GridBagConstraints();
		gbc_btnBrowseResultsFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseResultsFile.gridx = 2;
		gbc_btnBrowseResultsFile.gridy = 0;
		panelProcess.add(btnBrowseResultsFile, gbc_btnBrowseResultsFile);
		
		
		btnProcess = new JButton("Process and save");
		btnProcess.addActionListener(this);
		GridBagConstraints gbc_btnProcess = new GridBagConstraints();
		gbc_btnProcess.gridwidth = 3;
		gbc_btnProcess.insets = new Insets(0, 0, 0, 5);
		gbc_btnProcess.anchor = GridBagConstraints.NORTH;
		gbc_btnProcess.gridx = 0;
		gbc_btnProcess.gridy = 1;
		
		panelProcess.add(btnProcess, gbc_btnProcess);
	}
	
	
	/**
	 * Action listener
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnKnownAddFile) {
			browseFileForList(listKnownProteins);
		} else if (e.getSource() == btnKnownRemove) {
			removeSelectionFromList(listKnownProteins);
		} else if (e.getSource() == btnPseudoAddFile) {
			browseFileForList(listPseudoProteins);
		} else if (e.getSource() == btnPseudoRemoveFile) {
			removeSelectionFromList(listPseudoProteins);
		} else if (e.getSource() == btnAddIdentifications) {
			browseFileForIdentificationTable();
		} else if (e.getSource() == btnRemoveIdentifications) {
			if (tableIdentifications.getSelectedRow() > -1) {
				((DefaultTableModel)tableIdentifications.getModel()).removeRow(
						tableIdentifications.getSelectedRow());
			}
		} else if (e.getSource() == btnBrowseFASTAFile) {
			browseFileForField(fieldFASTAfile);
		} else if (e.getSource() == btnBrowseResultsFile) {
			browseFileForField(fieldResultsFile);
		} else if (e.getSource() == btnProcess) {
			
			// TODO: put a thread here, which handles the processing
			
			btnProcess.setEnabled(false);
			btnProcess.setText("Processing...");
			processInput();
			btnProcess.setText("Process and save");
			btnProcess.setEnabled(true);
		}
	}
	
	
	/**
	 * Uses the fileChooser to browse a (not further filtered) file and put the
	 * path into the {@link JList}
	 * 
	 * @param textField
	 */
	private void browseFileForList(JList fileList) {
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(
				CombineIdentificationResultsPanel.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			for (File file : fileChooser.getSelectedFiles()) {
				((DefaultListModel)fileList.getModel()).addElement(file.getAbsolutePath());
			}
		}
	}
	
	
	/**
	 * Removes the selected entry from the given list. If nothing is selected,
	 * nothing will be removed.
	 * 
	 * @param fileList
	 */
	private void removeSelectionFromList(JList fileList) {
		if (fileList.getSelectedIndex() > -1) {
			((DefaultListModel)fileList.getModel()).remove(fileList.getSelectedIndex());
		}
	}
	
	
	/**
	 * Uses the fileChooser to browse a (not further filtered) file and put it
	 * into the identification file table
	 * 
	 * @param textField
	 */
	private void browseFileForIdentificationTable() {
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(
				CombineIdentificationResultsPanel.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			for (File file : fileChooser.getSelectedFiles()) {
				Object[] rowData = new String[2];
				
				rowData[0] = file.getAbsolutePath();
				rowData[1] = "default";
				
				((DefaultTableModel)tableIdentifications.getModel()).addRow(
						rowData);
			}
		}
	}
	
	
	/**
	 * Uses the fileChooser to browse a (not further filtered) file and put the
	 * path to the file in the given {@link JTextField}.
	 * 
	 * @param textField
	 */
	private void browseFileForField(JTextField textField) {
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showOpenDialog(
				CombineIdentificationResultsPanel.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			textField.setText(file.getAbsolutePath());
		}
	}
	
	
	/**
	 * Process the information and show the results
	 */
	private void processInput() {
		if (fieldResultsFile.getText().trim().length() < 1) {
			logger.error("Select a path to save the results to!");
			return;
		}
		
		CombineIdentificationResults combiner =
				new CombineIdentificationResults();
		
		try {
			for (int i=0; i < listKnownProteins.getModel().getSize(); i++) {
				String fileName =
						(String)listKnownProteins.getModel().getElementAt(i);
				combiner.parseKnownProteinsFromGFF(fileName);
			}
			
			for (int i=0; i < listPseudoProteins.getModel().getSize(); i++) {
				String fileName =
						(String)listPseudoProteins.getModel().getElementAt(i);
				combiner.parsePseudoProteinsFromGFF(fileName);
			}
			
			if (fieldFASTAfile.getText().trim().length() > 0) {
				combiner.parseProteinSequencesFromFASTA(
						fieldFASTAfile.getText());
			}
			
			// set the decoy regex, if given
			if (fieldDecoyRegularExpression.getText().trim().length() > 0) {
				combiner.setDecoyRegex(fieldDecoyRegularExpression.getText());
			}
			
			for (int row=0;
					row < ((DefaultTableModel)tableIdentifications.getModel()).getRowCount();
					row++) {
				String fileName =
						(String)((DefaultTableModel)tableIdentifications.getModel()).getValueAt(row, 0);
				String groupName =
						(String)((DefaultTableModel)tableIdentifications.getModel()).getValueAt(row, 1);
				
				combiner.parseMzTab(fileName, groupName);
			}
			
			combiner.saveToFile(fieldResultsFile.getText());
		} catch (IOException e) {
			logger.error("Problem while processing files.", e);
		} catch (MZTabException e) {
			logger.error("Problem while processing mzTab file.", e);
		}
	}
}
