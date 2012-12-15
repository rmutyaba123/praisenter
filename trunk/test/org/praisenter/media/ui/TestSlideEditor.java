package org.praisenter.media.ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.xml.DOMConfigurator;
import org.praisenter.media.MediaLibrary;
import org.praisenter.preferences.Preferences;
import org.praisenter.slide.BibleSlideTemplate;
import org.praisenter.slide.SlideLibrary;
import org.praisenter.slide.SlideLibraryException;
import org.praisenter.slide.ui.editor.FillEditorPanel;
import org.praisenter.slide.ui.editor.SlideEditorPanel;
import org.praisenter.slide.ui.editor.LineStyleEditorPanel;
import org.praisenter.utilities.LookAndFeelUtilities;

public class TestSlideEditor {
	public static void main(String[] args) throws SlideLibraryException {
		DOMConfigurator.configure("config/log4j.xml");
		
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	        if (LookAndFeelUtilities.NIMBUS.equalsIgnoreCase(info.getName())) {
	            try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
	        }
	    }
		
		MediaLibrary.loadMediaLibrary();
		SlideLibrary.loadSlideLibrary();
		
//		PaintEditorPanel panel = new PaintEditorPanel(null);
//		StrokeEditorPanel panel = new StrokeEditorPanel(null);
		Dimension size = Preferences.getInstance().getPrimaryOrDefaultDeviceResolution();
//		SlideEditorPanel panel = new SlideEditorPanel(BibleSlideTemplate.getDefaultTemplate(size.width, size.height), size);
		BibleSlideTemplate template = SlideLibrary.getTemplate("templates\\bible\\default.xml", BibleSlideTemplate.class).copy();
		if (template.getWidth() != size.width || template.getHeight() != size.height) {
			template.adjustSize(size.width, size.height);
		}
		SlideEditorPanel panel = new SlideEditorPanel(template, size);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
}
