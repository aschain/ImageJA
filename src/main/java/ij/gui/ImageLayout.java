package ij.gui;
import java.awt.*;
import javax.swing.*;
import ij.*;

/** This is a custom layout manager that supports resizing of zoomed
images. It's based on FlowLayout, but with vertical and centered flow. */
public class ImageLayout implements LayoutManager {

    int hgap = ImageWindow.HGAP;
    int vgap = ImageWindow.VGAP;
	ImageCanvas ic;
	boolean ignoreNonImageWidths;

    /** Creates a new ImageLayout with center alignment. */
    public ImageLayout(ImageCanvas ic) {
    	this.ic = ic;
    }

    /** Not used by this class. */
    public void addLayoutComponent(String name, Component comp) {
    }

    /** Not used by this class. */
    public void removeLayoutComponent(Component comp) {
    }

    /** Returns the preferred dimensions for this layout. */
    public Dimension preferredLayoutSize(Container target) {
		Dimension dim = new Dimension(0,0);
		int nmembers = target.getComponentCount();
		for (int i=0; i<nmembers; i++) {
		    Component m = target.getComponent(i);
			Dimension d = m.getPreferredSize();
			if (i==1 || !ignoreNonImageWidths)
    			dim.width = Math.max(dim.width, d.width);
			if (i>1) dim.height += vgap;
			dim.height += d.height;
		}
		Insets insets = target.getInsets();
		dim.width += insets.left + insets.right + hgap*2;
		dim.height += insets.top + insets.bottom + vgap+2;
		return dim;
    }

    /** Returns the minimum dimensions for this layout. */
    public Dimension minimumLayoutSize(Container target) {
		return preferredLayoutSize(target);
    }

    /** Determines whether to ignore the width of non-image components when calculating
     *  the preferred width (default false, i.e. the maximum of the widths of all components is used).
     *  When true, components that do not fit the window will be truncated at the right.
     *  The width of the 0th component (the ImageCanvas) is always taken into account. */
	public void ignoreNonImageWidths(boolean ignoreNonImageWidths) {
		this.ignoreNonImageWidths = ignoreNonImageWidths;
	}

    /** Centers the elements in the specified column, if there is any slack.*/
    private void moveComponents(Container target, int x, int y, int width, int height, int nmembers) {
    	int x2 = x + (width - ic.getSize().width)/2;
	    y += height / 2;
		for (int i=0; i<nmembers; i++) {
		    Component m = target.getComponent(i);
		    Dimension d = m.getSize();
		    if (d.height>60)
		    	x2 = x + (width - d.width)/2;
			m.setLocation(x2, y);
			if(i>0)y += vgap;
			else y+=2;
			y+=d.height;
		}
    }

    /** Lays out the container and calls ImageCanvas.resizeCanvas()
		to adjust the image canvas size as needed. */
    public void layoutContainer(Container target) {
		Insets insets = target.getInsets();
		IJ.log("topis:"+insets.top);
		int nmembers = target.getComponentCount();
		Dimension d;
		int extraHeight = 0;
		for (int i=0; i<nmembers; i++) {
			Component m = target.getComponent(i);
			if ((m instanceof ScrollbarWithLabel) || (m instanceof JScrollBar) || m instanceof ImageWindow.InfoPanel) {
				d = m.getPreferredSize();
				extraHeight += d.height;
				if ((m instanceof ScrollbarWithLabel) || (m instanceof JScrollBar)) extraHeight+=vgap;
			}
		}
		d = target.getSize();
		int preferredImageWidth = d.width - (insets.left + insets.right + hgap*2);
		int preferredImageHeight = d.height - (insets.top + insets.bottom + vgap + extraHeight+2);
		ic.resizeCanvas(preferredImageWidth, preferredImageHeight);
		//int maxwidth = d.width - (insets.left + insets.right + hgap*2);
		int maxheight = d.height - (insets.top + insets.bottom + vgap);
		Dimension psize = preferredLayoutSize(target);
		int x = insets.left + hgap + (d.width - psize.width)/2;
		int y = 0;
		int colw = 0;
		
		for (int i=0; i<nmembers; i++) {
			Component m = target.getComponent(i);
			d = m.getPreferredSize();
			if ((m instanceof ScrollbarWithLabel) || (m instanceof JScrollBar) || m instanceof ImageWindow.InfoPanel) {
				if(m instanceof ImageWindow.InfoPanel) IJ.log("IPh:"+d.height);
				int scrollbarWidth = target.getComponent(1).getPreferredSize().width;
				Dimension minSize = m.getMinimumSize();
				if (scrollbarWidth<minSize.width) scrollbarWidth = minSize.width;
				m.setSize(scrollbarWidth, d.height);
			} else
				m.setSize(d.width, d.height);
			if (y > 1) y += vgap;
			else if(y==1)y++;
			y += d.height;
			if (i==1 || !ignoreNonImageWidths)
				colw = Math.max(colw, d.width);
		}
		moveComponents(target, x, insets.top, colw, maxheight - y, nmembers);
    }
    
}
