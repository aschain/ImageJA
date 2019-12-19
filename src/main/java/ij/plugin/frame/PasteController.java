package ij.plugin.frame;
import java.awt.FlowLayout;
import java.awt.event.*;
import ij.*;
import ij.plugin.*;
import ij.gui.*;
import ij.process.*;
import javax.swing.*;

/** Implements ImageJ's Paste Control window. */
public class PasteController extends PlugInFrame implements PlugIn, ItemListener {

	//private JPanel panel;
	private JComboBox<String> pasteMode;
	private static JFrame instance;
	
	public PasteController() {
		super("Paste Control");
		if (instance!=null) {
			WindowManager.toFront(instance);
			return;
		}
		WindowManager.addWindow(this);
		instance = this;
		IJ.register(PasteController.class);
		getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 2, 5));
		
		add(new JLabel(" Transfer Mode:"));
		pasteMode = new JComboBox<String>();
		pasteMode.addItem("Copy");
		pasteMode.addItem("Blend");
		pasteMode.addItem("Difference");
		pasteMode.addItem("Transparent-white");
		pasteMode.addItem("Transparent-zero");
		pasteMode.addItem("AND");
		pasteMode.addItem("OR");
		pasteMode.addItem("XOR");
		pasteMode.addItem("Add");
		pasteMode.addItem("Subtract");
		pasteMode.addItem("Multiply");
		pasteMode.addItem("Divide");
		pasteMode.addItem("Min");
		pasteMode.addItem("Max");
		pasteMode.setSelectedItem("Copy");
		pasteMode.addItemListener(this);
		add(pasteMode);
		Roi.setPasteMode(Blitter.COPY);

		GUI.scale(this);
		pack();
		GUI.centerOnImageJScreen(this);
		setResizable(false);
		show();
	}
	
	public void itemStateChanged(ItemEvent e) {
		int index = pasteMode.getSelectedIndex();
		int mode = Blitter.COPY;
		switch (index) {
			case 0: mode = Blitter.COPY; break;
			case 1: mode = Blitter.AVERAGE; break;
			case 2: mode = Blitter.DIFFERENCE; break;
			case 3: mode = Blitter.COPY_TRANSPARENT; break;
			case 4: mode = Blitter.COPY_ZERO_TRANSPARENT; break;
			case 5: mode = Blitter.AND; break;
			case 6: mode = Blitter.OR; break;
			case 7: mode = Blitter.XOR; break;
			case 8: mode = Blitter.ADD; break;
			case 9: mode = Blitter.SUBTRACT; break;
			case 10: mode = Blitter.MULTIPLY; break;
			case 11: mode = Blitter.DIVIDE; break;
			case 12: mode = Blitter.MIN; break;
			case 13: mode = Blitter.MAX; break;
		}
		Roi.setPasteMode(mode);
		if (Recorder.record)
			Recorder.record("setPasteMode", (String)pasteMode.getSelectedItem());
		ImagePlus imp = WindowManager.getCurrentImage();
	}
	
	public void close() {
		super.close();
		instance = null;
	}
	
}
