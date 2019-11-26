package ij;
import ij.io.*;

import javax.swing.*;

/** Opens, in a separate thread, files selected from the File/Open Recent submenu.*/
public class RecentOpener implements Runnable {
	private String path;

	RecentOpener(String path) {
		this.path = path;
		Thread thread = new Thread(this, "RecentOpener");
		thread.start();
	}

	/** Open the file and move the path to top of the submenu. */
	public void run() {
		Opener o = new Opener();
		o.open(path);
		JMenu menu = Menus.getOpenRecentMenu();
		int n = menu.getItemCount();
		int index = 0;
		for (int i=0; i<n; i++) {
			JMenuItem mi=menu.getItem(i);
			if (mi!=null && mi.getLabel().equals(path)) {
				index = i;
				break;
			}
		}
		if (index>0) {
			JMenuItem item = menu.getItem(index);
			if(item!=null) {
				menu.remove(index);
				menu.insert(item, 0);
			}
		}
	}

}

