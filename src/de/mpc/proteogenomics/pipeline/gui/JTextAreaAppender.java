package de.mpc.proteogenomics.pipeline.gui;

import java.util.LinkedList;

import javax.swing.JTextArea;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

public class JTextAreaAppender extends AppenderSkeleton {
	
	/** the textArea for logging */
	private final JTextArea textArea;
	
	/** the layout for logging */
	private PatternLayout layout;
	
	/** maximal number of lines in the logger */
	private final int maxlines;
	
	/** length of lines within text area */
	private final LinkedList<Integer> lengths; 
	
	
	public JTextAreaAppender(JTextArea txtArea) {
		this(txtArea, "%d %p %c{1} - %m%n", 10000);
	}
	
	
	public JTextAreaAppender(JTextArea txtArea, String layoutString,
			int maxLines) {
		this.textArea = txtArea;
		this.layout = new PatternLayout(layoutString);
		this.maxlines = maxLines;
		this.lengths = new LinkedList<Integer>();
		
		lengths.addLast(this.textArea.getText().length());
	}
	
	
	@Override
	protected void append(LoggingEvent event) {
		String formattedEvent = layout.format(event);
		
		textArea.append(formattedEvent);
		lengths.addLast(formattedEvent.length());
		
		if (event.getThrowableStrRep() != null) {
			for (String line : event.getThrowableStrRep()) {
				String brLine = line + "\n";
				textArea.append(brLine);
				lengths.addLast(brLine.length());
			}
		}
		
		while (lengths.size() >= maxlines) {
			textArea.replaceRange("", 0, lengths.removeFirst());
		}
	}
	
	
	public boolean requiresLayout() {
		return false;
	}
	
	
	public void close() {
	}
}