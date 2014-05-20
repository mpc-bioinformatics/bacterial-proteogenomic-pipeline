package de.mpc.proteogenomics.pipeline.gui;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JSplitPane;


public class ProteoGenomicsPipelineGUI {
	
	private JFrame mainGUIFrame;
	
	
	/**
	 * Create the application.
	 */
	public ProteoGenomicsPipelineGUI() {
		initialize();
	}
	
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mainGUIFrame = new JFrame();
		mainGUIFrame.setTitle("Bacterial ProteoGenomics Pipeline");
		mainGUIFrame.setMinimumSize(new Dimension(800, 600));
		mainGUIFrame.setBounds(100, 100, 1200, 800);
		mainGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    GridBagLayout gridBagLayout = new GridBagLayout();
	    gridBagLayout.columnWidths = new int[]{0, 0};
	    gridBagLayout.rowHeights = new int[]{0, 0};
	    gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
	    gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
	    mainGUIFrame.getContentPane().setLayout(gridBagLayout);
	    
	    
	    JSplitPane splitPane = new JSplitPane();
	    splitPane.setResizeWeight(0.8);
	    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
	    GridBagConstraints gbc_splitPane = new GridBagConstraints();
	    gbc_splitPane.fill = GridBagConstraints.BOTH;
	    gbc_splitPane.gridx = 0;
	    gbc_splitPane.gridy = 0;
	    mainGUIFrame.getContentPane().add(splitPane, gbc_splitPane);
	    
		
		JTabbedPane tabbedPane = new JTabbedPane();
		splitPane.setTopComponent(tabbedPane);
		
		JFileChooser fileChooser = new JFileChooser();
		
		tabbedPane.addTab("1. Parse Protein Information", null,
				new ParseProteinInformationPanel(fileChooser));
		
		tabbedPane.addTab("2. Compare and Combine", null,
				new CompareAndCombineProteinInformationPanel(fileChooser));
		
		
		tabbedPane.addTab("3. Genome Parser", null,
				new GenomeParserPanel(fileChooser));
		
		tabbedPane.addTab("4. Create Decoy DB", null,
				new CreateDecoyDBPanel(fileChooser));
		
		tabbedPane.addTab("5. Combine Identifications", null,
				new CombineIdentificationResultsPanel(fileChooser));
		
		tabbedPane.addTab("6. Analysis", null,
				new AnalysisPanel(fileChooser));
		
		
	    JScrollPane scrollPaneLogging = new JScrollPane();
	    splitPane.setBottomComponent(scrollPaneLogging);
	    
	    JTextArea textArea = new JTextArea();
	    scrollPaneLogging.setViewportView(textArea);
	    textArea.setText("Logging...\n");
	    
	    JTextAreaAppender ta = new JTextAreaAppender(textArea);
	    ta.setName("TextAreaLogger");
	    ta.setThreshold(Level.DEBUG);
	    Logger.getRootLogger().addAppender(ta);
	}
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProteoGenomicsPipelineGUI window = new ProteoGenomicsPipelineGUI();
					window.mainGUIFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
