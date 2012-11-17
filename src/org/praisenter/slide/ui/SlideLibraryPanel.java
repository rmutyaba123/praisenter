package org.praisenter.slide.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;
import org.praisenter.slide.Slide;
import org.praisenter.slide.SlideLibrary;
import org.praisenter.slide.SlideThumbnail;

/**
 * Panel used to maintain the Slide Library.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class SlideLibraryPanel extends JPanel implements ActionListener {
	/** The version id */

	/** The class level logger */
	private static final Logger LOGGER = Logger.getLogger(SlideLibraryPanel.class);
	
	// controls
	
	/**
	 * Default constructor.
	 */
	public SlideLibraryPanel() {
		this.setLayout(new BorderLayout());
		List<SlideThumbnail> thumbnails = SlideLibrary.getThumbnails(Slide.class);
		JList lstThumbs = createJList(thumbnails);
		this.add(lstThumbs, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new JList for the given list of {@link SlideThumbnail}s.
	 * @param thumbnails the list of thumbnails
	 * @return JList
	 */
	private static final JList<SlideThumbnail> createJList(List<SlideThumbnail> thumbnails) {
		JList<SlideThumbnail> list = new JList<SlideThumbnail>();
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFixedCellWidth(100);
		list.setVisibleRowCount(-1);
		list.setCellRenderer(new SlideThumbnailListCellRenderer());
		list.setLayout(new BorderLayout());
		// setup the items
		DefaultListModel<SlideThumbnail> model = new DefaultListModel<SlideThumbnail>();
		for (SlideThumbnail thumbnail : thumbnails) {
			model.addElement(thumbnail);
		}
		list.setModel(model);
		
		return list;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
	}
}