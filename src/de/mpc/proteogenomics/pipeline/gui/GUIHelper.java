package de.mpc.proteogenomics.pipeline.gui;

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JTextField;


/**
 * Some helper functions for the ProteoGenomicGUI
 * 
 * @author julian
 *
 */
public class GUIHelper {
	
	
	/**
	 * Uses the fileChooser to browse a (not further filtered) file and put the
	 * path to the file in the given {@link JTextField}.
	 * 
	 * @param textField
	 */
	public static void browseFileForField(JTextField textField,
			JFileChooser fileChooser, Component parent) {
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showOpenDialog(parent);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			textField.setText(file.getAbsolutePath());
		}
	}
	
	
	/**
	 * Uses the fileChooser to browse a (not further filtered) file and put the
	 * path into the list(-model)
	 * 
	 * @param textField
	 */
	public static void browseFileForList(DefaultListModel listModel,
			JFileChooser fileChooser, Component parent) {
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(parent);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			for (File file : fileChooser.getSelectedFiles()) {
				listModel.addElement(file.getAbsolutePath());
			}
		}
	}
	
	
	/**
	 * Removes the selected entry from the given list. If nothing is selected,
	 * nothing will be removed.
	 * 
	 * @param fileList
	 */
	public static void removeSelectionFromList(JList fileList) {
		if (fileList.getSelectedIndex() > -1) {
			((DefaultListModel)fileList.getModel()).remove(fileList.getSelectedIndex());
		}
	}
}
