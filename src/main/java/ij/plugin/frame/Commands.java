package ij.plugin.frame;
import ij.*;
import ij.gui.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.*;


/** This plugin implements the Plugins>Utilties>Recent Commands command. */
public class Commands extends PlugInFrame implements ActionListener, ListSelectionListener, CommandListener {
	public static final String LOC_KEY = "commands.loc";
	public static final String CMDS_KEY = "commands.cmds";
		public static final int MAX_COMMANDS = 20;
	private static JFrame instance;
	private static final String divider = "---------------";
	private static final String[] commands = {
		"Blobs (25K)",
		"Open...",
		"Show Info...",
		"Close",
		"Close All",
		"Histogram",
		"Find Maxima...",
		"Gaussian Blur...",		
		"Record...",
		"Capture Screen",
		"Find Commands..."
	};
	private JList<String> list;
	private DefaultListModel<String> listm;
	private String command;
	private JButton button;

	public Commands() {
		super("Commands");
		if (instance!=null) {
			WindowManager.toFront(instance);
			return;
		}
		instance = this;
		WindowManager.addWindow(this);
		listm=new DefaultListModel<String>();
		list = new JList<String>(listm);
		list.addListSelectionListener(this);
		String cmds = Prefs.get(CMDS_KEY, null);
		
		if (cmds!=null) {
			String[] cmd = cmds.split(",");
			int len = cmd.length<=MAX_COMMANDS?cmd.length:MAX_COMMANDS;
			boolean isDivider = false;
			for (int i=0; i<len; i++) {
				if (divider.equals(cmd[i])) {
					isDivider = true;
					break;
				}
			}
			if (isDivider) {
				for (int i=0; i<len; i++)
					listm.addElement(cmd[i]);
			} else
				cmds = null;				
		}
		if (cmds==null) {
			listm.addElement(divider);
			int len = commands.length<MAX_COMMANDS?commands.length:MAX_COMMANDS-1;
			for (int i=0; i<len; i++)
				listm.addElement(commands[i]);		
		}
		ImageJ ij = IJ.getInstance();
		addKeyListener(ij);
		Executer.addCommandListener(this);
		GUI.scale(list);
		list.addKeyListener(ij);
		GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.insets = new Insets(0, 0, 0, 0); 
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        add(list,c); 
		button = new JButton("Edit");
		button.addActionListener(this);
		button.addKeyListener(ij);
        //c.insets = new Insets(2, 6, 6, 6); 
        c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.CENTER;
        add(button, c);
		pack();
		Dimension size = getSize();
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		show();
	}

	public void actionPerformed(ActionEvent e) {
		GenericDialog gd = new GenericDialog("Commands");
		int dividerIndex = getDividerIndex();
		StringBuilder sb = new StringBuilder(200);
		sb.append("| ");
		for (int i=0; i<dividerIndex; i++) {
			String cmd = listm.get(i);
			sb.append(cmd);
			sb.append(" | ");
		}
		sb.append("Debug Mode | Hyperstack |");
		String recentCommands = sb.toString();
		gd.setInsets(5, 0, 0);
		gd.addTextAreas(recentCommands, null, 5, 28);
		int index = dividerIndex + 1;
		int n = 1;
		for (int i=index; i<listm.getSize(); i++) {
			gd.setInsets(2, 8, 0);
			gd.addStringField("Cmd"+IJ.pad(n++,2)+":", listm.get(i), 20);
		}
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		for (int i=index; i<listm.getSize(); i++)
			listm.set(i,gd.getNextString());
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		int index = list.getSelectedIndex();
		command = listm.get(index);
		if (!command.equals(divider)) {
			if (command.equals("Debug Mode"))
				IJ.runMacro("setOption('DebugMode')");
			else if (command.equals("Hyperstack"))
				IJ.runMacro("newImage('HyperStack', '8-bit color label', 400, 300, 3, 4, 25)");
			else
				IJ.doCommand(command);
		}
		list.clearSelection();	
	}
	
	public String commandExecuting(String cmd2) {
		if ("Quit".equals(cmd2))
			return cmd2;
		String cmd1 = command;
		if (cmd1==null || !cmd1.equals(cmd2)) {
			try {
				listm.removeElement(cmd2);
			} catch(Exception e) {}
			if (listm.getSize()>=MAX_COMMANDS)
				listm.remove(getDividerIndex()-1);
			listm.insertElementAt(cmd2, 0);
		}
		command = null;
		return cmd2;
	}
	
	private int getDividerIndex() {
		int index = 0;
		for (int i=0; i<MAX_COMMANDS; i++) {
			String cmd = listm.get(i);
			if (divider.equals(cmd)) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	/** Overrides PlugInFrame.close(). */
	public void close() {
		super.close();
		instance = null;
		Executer.removeCommandListener(this);
		Prefs.saveLocation(LOC_KEY, getLocation());
		StringBuilder sb = new StringBuilder(200);
		for (int i=0; i<listm.getSize(); i++) {
			String cmd = listm.get(i);
			sb.append(cmd);
			sb.append(",");
		}
		String cmds = sb.toString();
		cmds = cmds.substring(0, cmds.length()-1);
		//IJ.log("close: "+cmds); IJ.wait(5000);
		Prefs.set(CMDS_KEY, cmds);
	}

}
