package ij.macro;
import ij.*;
import ij.plugin.frame.*;
import ij.gui.GUI;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** This class implements the text editor's Macros/Find Functions command.
	It was written by jerome.mutterer at ibmp.fr, and is based on Mark Longair's CommandFinder plugin.
*/
public class FunctionFinder implements DocumentListener,  WindowListener, KeyListener, ItemListener, ActionListener {
	private static JDialog dialog;
	private JTextField prompt;
	private JList<String> functions;
	private DefaultListModel<String> functionsm;
	private JButton insertButton, infoButton, closeButton;
	private String [] commands;
	private Editor editor;

	public FunctionFinder(Editor editor) {

		this.editor = editor;

		String exists = IJ.runMacro("return File.exists(getDirectory('macros')+'functions.html');");
		if (exists=="0")	{
			String installLocalMacroFunctionsFile = "functions = File.openUrlAsString('"+IJ.URL+"/developer/macro/functions.html');\n"+
			"f = File.open(getDirectory('macros')+'functions.html');\n"+
			"print (f, functions);\n"+
			"File.close(f);";
			try { IJ.runMacro(installLocalMacroFunctionsFile);
			} catch (Throwable e) { IJ.error("Problem downloading functions.html"); return;}
		}
		String f = IJ.runMacro("return File.openAsString(getDirectory('macros')+'functions.html');");
		String [] l = f.split("\n");
		commands= new String [l.length];
		int c=0;
		for (int i=0; i<l.length; i++) {
			String line = l[i];
			if (line.startsWith("<b>")) {
				commands[c]=line.substring(line.indexOf("<b>")+3,line.indexOf("</b>"));
				c++;
			}
		}
		if (c==0) {
			IJ.error("ImageJ/macros/functions.html is corrupted");
			return;
		}

		ImageJ imageJ = IJ.getInstance();
		if (dialog==null) {
			dialog = new JDialog(imageJ, "Built-in Functions");
			dialog.setLayout(new BorderLayout());
			dialog.addWindowListener(this);
			JPanel northPanel = new JPanel();
			prompt = new JTextField("", 32);
			prompt.getDocument().addDocumentListener(this);
			prompt.addKeyListener(this);
			northPanel.add(prompt);
			dialog.add(northPanel, BorderLayout.NORTH);
			functionsm=new DefaultListModel<String>();
			functions = new JList<String>();
			functions.addKeyListener(this);
			populateList("");
			dialog.add(functions, BorderLayout.CENTER);
			JPanel buttonPanel = new JPanel();
			insertButton = new JButton("Insert");
			insertButton.addActionListener(this);
			buttonPanel.add(insertButton);
			infoButton = new JButton("Info");
			infoButton.addActionListener(this);
			buttonPanel.add(infoButton);
			closeButton = new JButton("Close");
			closeButton.addActionListener(this);
			buttonPanel.add(closeButton);
			dialog.add(buttonPanel, BorderLayout.SOUTH);
			GUI.scale(dialog);
			dialog.pack();
		}

		JFrame frame = WindowManager.getFrontWindow();
		if (frame==null) return;
		java.awt.Point posi=frame.getLocationOnScreen();
		int initialX = (int)posi.getX() + 38;
		int initialY = (int)posi.getY() + 84;
		dialog.setLocation(initialX,initialY);
		dialog.setVisible(true);
		dialog.toFront();
	}

	public FunctionFinder() {
		this(null);
	}

	public void populateList(String matchingSubstring) {
		String substring = matchingSubstring.toLowerCase();
		functions.removeAll();
		try {
			for(int i=0; i<commands.length; ++i) {
				String commandName = commands[i];
				if (commandName.length()==0)
					continue;
				String lowerCommandName = commandName.toLowerCase();
				if( lowerCommandName.indexOf(substring) >= 0 ) {
					functionsm.addElement(commands[i]);
				}
			}
		} catch (Exception e){}
	}

	public void edPaste(String arg) {
		JFrame frame = WindowManager.getFrontWindow();
		if (!(frame instanceof Editor))
			return;

		try {
			JTextArea ta = ((Editor)frame).getTextArea();
			editor = (Editor)frame;
			int start = ta.getSelectionStart( );
			int end = ta.getSelectionEnd( );
			try {
				ta.replaceRange(arg.substring(0,arg.length()), start, end);
			} catch (Exception e) { }
			if (IJ.isMacOSX())
				ta.setCaretPosition(start+arg.length());
		} catch (Exception e) { }
	}

	public void itemStateChanged(ItemEvent ie) {
		populateList(prompt.getText());
	}

	protected void runFromLabel(String listLabel) {
		edPaste(listLabel);
		closeAndRefocus();
	}

	public void close() {
		closeAndRefocus();
	}

	public void closeAndRefocus() {
		if (dialog!=null)
			dialog.dispose();
		if (editor!=null)
			editor.toFront();
	}

	public void keyPressed(KeyEvent ke) {
		int key = ke.getKeyCode();
		int items = functionsm.getSize();
		Object source = ke.getSource();
		if (source==prompt) {
			if (key==KeyEvent.VK_ENTER) {
				if (1==items) {
					String selected = functionsm.get(0);
					edPaste(selected);
				}
			} else if (key==KeyEvent.VK_UP) {
				functions.requestFocus();
				if(items>0)
					functions.setSelectedIndex(functionsm.getSize()-1);
			} else if (key==KeyEvent.VK_ESCAPE) {
				closeAndRefocus();
			} else if (key==KeyEvent.VK_DOWN)  {
				functions.requestFocus();
				if (items>0)
					functions.setSelectedIndex(0);
			}
		} else if (source==functions) {
			if (key==KeyEvent.VK_ENTER) {
				String selected = functions.getSelectedValue();
				if (selected!=null)
					edPaste(selected);
			}
			else if (key==KeyEvent.VK_ESCAPE) {
				closeAndRefocus();
			}
			else if (key==KeyEvent.VK_BACK_SPACE || key==KeyEvent.VK_DELETE) {
			/* If someone presses backspace or delete they probably
			   want to remove the last letter from the search string, so
			   switch the focus back to the prompt: */
			prompt.requestFocus();
		}
		}
	}

	public void keyReleased(KeyEvent ke) { }

	public void keyTyped(KeyEvent ke) { }

	public void textValueChanged(TextEvent te) {
		populateList(prompt.getText());
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {textValueChanged(new TextEvent(e.getDocument(),TextEvent.TEXT_VALUE_CHANGED));}
	@Override
	public void removeUpdate(DocumentEvent e) {textValueChanged(new TextEvent(e.getDocument(),TextEvent.TEXT_VALUE_CHANGED));}
	@Override
	public void changedUpdate(DocumentEvent e) {textValueChanged(new TextEvent(e.getDocument(),TextEvent.TEXT_VALUE_CHANGED));}

	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==insertButton) {
			int index = functions.getSelectedIndex();
			if (index>=0) {
				String selected = functionsm.get(index);
				edPaste(selected);
			}
		} else if (b==infoButton) {
			String url = IJ.URL+"/developer/macro/functions.html";
			int index = functions.getSelectedIndex();
			if (index>=0) {
				String selected = functionsm.get(index);
				int index2 = selected.indexOf("(");
				if (index2==-1)
					index2 = selected.length();
				url = url + "#" + selected.substring(0, index2);
			}
			IJ.runPlugIn("ij.plugin.BrowserLauncher", url);
		} else if (b==closeButton)
		closeAndRefocus();
	}

	public void windowClosing(WindowEvent e) {
		closeAndRefocus();
	}

	public void windowActivated(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	public void windowClosed(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
}
