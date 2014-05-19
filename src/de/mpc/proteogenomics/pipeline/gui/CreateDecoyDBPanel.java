package de.mpc.proteogenomics.pipeline.gui;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.mpc.proteogenomics.pipeline.CreateDecoyDB;

import javax.swing.JLabel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.JCheckBox;

public class CreateDecoyDBPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = Logger.getLogger(CreateDecoyDBPanel.class);
	
	private JList listInputFastas;
	private JButton btnAddFastaFiles;
	private JButton btnRemoveFile;
	private JTextField textOutputFasta;
	private JButton btnBrowseOutputFasta;
	private JButton btnCreateDecoyDb;
	private JCheckBox chckbxCreateDecoysOnly;
	
	private JFileChooser fileChooser;
	
	public CreateDecoyDBPanel(JFileChooser fc) {
		this.fileChooser = fc;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{166, 118, 0, 0};
		gridBagLayout.rowHeights = new int[]{15, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Create Decoy DB");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 3;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTH;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblInputFastas = new JLabel("Input FASTAs");
		GridBagConstraints gbc_lblInputFastas = new GridBagConstraints();
		gbc_lblInputFastas.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblInputFastas.insets = new Insets(0, 0, 5, 5);
		gbc_lblInputFastas.gridx = 0;
		gbc_lblInputFastas.gridy = 1;
		add(lblInputFastas, gbc_lblInputFastas);
		
		JScrollPane scrollPaneFastaInput = new JScrollPane();
		GridBagConstraints gbc_scrollPaneFastaInput = new GridBagConstraints();
		gbc_scrollPaneFastaInput.gridwidth = 2;
		gbc_scrollPaneFastaInput.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneFastaInput.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPaneFastaInput.gridx = 1;
		gbc_scrollPaneFastaInput.gridy = 1;
		add(scrollPaneFastaInput, gbc_scrollPaneFastaInput);
		
		listInputFastas = new JList(new DefaultListModel());
		listInputFastas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneFastaInput.setViewportView(listInputFastas);
		
		JPanel panelAddFastas = new JPanel();
		GridBagConstraints gbc_panelAddFastas = new GridBagConstraints();
		gbc_panelAddFastas.gridwidth = 2;
		gbc_panelAddFastas.fill = GridBagConstraints.VERTICAL;
		gbc_panelAddFastas.insets = new Insets(0, 0, 5, 0);
		gbc_panelAddFastas.gridx = 1;
		gbc_panelAddFastas.gridy = 2;
		add(panelAddFastas, gbc_panelAddFastas);
		GridBagLayout gbl_panelAddFastas = new GridBagLayout();
		gbl_panelAddFastas.columnWidths = new int[]{0, 0, 0};
		gbl_panelAddFastas.rowHeights = new int[]{0, 0};
		gbl_panelAddFastas.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panelAddFastas.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelAddFastas.setLayout(gbl_panelAddFastas);
		
		btnAddFastaFiles = new JButton("Add FASTA(s)");
		btnAddFastaFiles.addActionListener(this);
		GridBagConstraints gbc_btnAddFastaFiles = new GridBagConstraints();
		gbc_btnAddFastaFiles.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddFastaFiles.gridx = 0;
		gbc_btnAddFastaFiles.gridy = 0;
		panelAddFastas.add(btnAddFastaFiles, gbc_btnAddFastaFiles);
		
		btnRemoveFile = new JButton("Remove file");
		btnRemoveFile.addActionListener(this);
		GridBagConstraints gbc_btnRemoveFile = new GridBagConstraints();
		gbc_btnRemoveFile.gridx = 1;
		gbc_btnRemoveFile.gridy = 0;
		panelAddFastas.add(btnRemoveFile, gbc_btnRemoveFile);
		
		JLabel lblOutputFasta = new JLabel("Output FASTA");
		GridBagConstraints gbc_lblOutputFasta = new GridBagConstraints();
		gbc_lblOutputFasta.anchor = GridBagConstraints.EAST;
		gbc_lblOutputFasta.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputFasta.gridx = 0;
		gbc_lblOutputFasta.gridy = 3;
		add(lblOutputFasta, gbc_lblOutputFasta);
		
		textOutputFasta = new JTextField();
		GridBagConstraints gbc_textOutputFasta = new GridBagConstraints();
		gbc_textOutputFasta.insets = new Insets(0, 0, 5, 5);
		gbc_textOutputFasta.fill = GridBagConstraints.HORIZONTAL;
		gbc_textOutputFasta.gridx = 1;
		gbc_textOutputFasta.gridy = 3;
		add(textOutputFasta, gbc_textOutputFasta);
		textOutputFasta.setColumns(10);
		
		btnBrowseOutputFasta = new JButton("Browse...");
		btnBrowseOutputFasta.addActionListener(this);
		GridBagConstraints gbc_btnBrowseOutputFasta = new GridBagConstraints();
		gbc_btnBrowseOutputFasta.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseOutputFasta.gridx = 2;
		gbc_btnBrowseOutputFasta.gridy = 3;
		add(btnBrowseOutputFasta, gbc_btnBrowseOutputFasta);
		
		chckbxCreateDecoysOnly = new JCheckBox("Create database containing decoys only");
		GridBagConstraints gbc_chckbxCreateDecoysOnly = new GridBagConstraints();
		gbc_chckbxCreateDecoysOnly.gridwidth = 2;
		gbc_chckbxCreateDecoysOnly.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCreateDecoysOnly.gridx = 1;
		gbc_chckbxCreateDecoysOnly.gridy = 4;
		add(chckbxCreateDecoysOnly, gbc_chckbxCreateDecoysOnly);
		
		JPanel panelCreateDB = new JPanel();
		GridBagConstraints gbc_panelCreateDB = new GridBagConstraints();
		gbc_panelCreateDB.gridwidth = 3;
		gbc_panelCreateDB.fill = GridBagConstraints.VERTICAL;
		gbc_panelCreateDB.gridx = 0;
		gbc_panelCreateDB.gridy = 5;
		add(panelCreateDB, gbc_panelCreateDB);
		GridBagLayout gbl_panelCreateDB = new GridBagLayout();
		gbl_panelCreateDB.columnWidths = new int[]{0, 0};
		gbl_panelCreateDB.rowHeights = new int[]{0, 0};
		gbl_panelCreateDB.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panelCreateDB.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelCreateDB.setLayout(gbl_panelCreateDB);
		
		btnCreateDecoyDb = new JButton("Create Decoy DB");
		btnCreateDecoyDb.addActionListener(this);
		GridBagConstraints gbc_btnCreateDecoyDb = new GridBagConstraints();
		gbc_btnCreateDecoyDb.gridx = 0;
		gbc_btnCreateDecoyDb.gridy = 0;
		panelCreateDB.add(btnCreateDecoyDb, gbc_btnCreateDecoyDb);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(btnAddFastaFiles)) {
			GUIHelper.browseFileForList(
					(DefaultListModel)listInputFastas.getModel(),
					fileChooser, CreateDecoyDBPanel.this);
		} else if (e.getSource().equals(btnRemoveFile)) {
			GUIHelper.removeSelectionFromList(listInputFastas);
		} else if (e.getSource().equals(btnBrowseOutputFasta)) {
			GUIHelper.browseFileForField(textOutputFasta, fileChooser,
					CreateDecoyDBPanel.this);
		} else if (e.getSource().equals(btnCreateDecoyDb)) {
			
			// TODO: add thread
			
			btnBrowseOutputFasta.setEnabled(false);
			createDB();
			btnBrowseOutputFasta.setEnabled(true);
		}
		
	}
	
	
	/**
	 * Create the DB
	 */
	private void createDB() {
		List<String> inFiles = new ArrayList<String>();
		Enumeration<?> en =
				((DefaultListModel)listInputFastas.getModel()).elements();
		
		while (en.hasMoreElements()) {
			String file = (String)en.nextElement();
			inFiles.add(file);
		}
		
		if (inFiles.size() < 1) {
			logger.info("No FASTA input files selected.");
			return;
		}
		
		if (textOutputFasta.getText().trim().length() < 1) {
			logger.info("No output file selected.");
			return;
		}
		
		String fastaOut = textOutputFasta.getText();
		boolean decoyOnly = chckbxCreateDecoysOnly.isSelected();
		
		try {
			if (decoyOnly) {
				logger.info("Creating decoy database only.");
				CreateDecoyDB.createDecoyDatabase(inFiles, null, fastaOut);
			} else {
				logger.info("Creating concatenation of target and decoy entries.");
				CreateDecoyDB.createDecoyDatabase(inFiles, fastaOut, null);
			}
		} catch (IOException e) {
			logger.error("Error while creating decoy database.", e);
		}
		
	}
}
