/*
 * Copyright (c) 2015-2016 William Bittle  http://www.praisenter.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of Praisenter nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.praisenter.javafx.bible;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.praisenter.FailedOperation;
import org.praisenter.bible.Bible;
import org.praisenter.javafx.Alerts;
import org.praisenter.javafx.FlowListView;
import org.praisenter.javafx.PraisenterContext;
import org.praisenter.resources.translations.Translations;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;

// FEATURE might be nice to be able to edit bibles when they are incomplete or messed up

/**
 * Pane specifically for showing the bibles in a bible library.
 * @author William Bittle
 * @version 3.0.0
 */
public final class BibleLibraryPane extends BorderPane {
	// selection
	
	/** The selected bible */
	private final ObjectProperty<Bible> selected = new SimpleObjectProperty<Bible>();
	
	// data
	
	/** The context */
	private final PraisenterContext context;
	
	// nodes
	
	/** The bible listing */
	private final FlowListView<BibleListItem> lstBibles;
	
	/**
	 * Minimal constructor.
	 * @param context the praisenter context
	 */
	public BibleLibraryPane(PraisenterContext context) {
		this.context = context;
		
		VBox right = new VBox();
		VBox importSteps = new VBox();
		
		Hyperlink lnkUnboundBible = new Hyperlink(Translations.get("bible.unbound"));
		lnkUnboundBible.setOnAction((e) -> {
			context.getApplication().getHostServices().showDocument("http://unbound.biola.edu/index.cfm?method=downloads.showDownloadMain");
		});
		Label lblStep1 = new Label(Translations.get("bible.import.list1"));
		Label lblStep2 = new Label(Translations.get("bible.import.list2"));
		Label lblStep3 = new Label(Translations.get("bible.import.list3"));
		TextFlow lblStep1Text = new TextFlow(new Text(Translations.get("bible.import.step1")), lnkUnboundBible);
		Label lblStep2Text = new Label(Translations.get("bible.import.step2"));
		Label lblStep3Text = new Label(Translations.get("bible.import.step3"));
		lblStep1.setMinWidth(20);
		// try to align the text due to the weird height of hyperlinks
		lblStep1.setPadding(new Insets(3, 0, 0, 0));
		lblStep2.setMinWidth(20);
		lblStep3.setMinWidth(20);
		lblStep2Text.setWrapText(true);
		lblStep3Text.setWrapText(true);
		
		importSteps.getChildren().addAll(
				new HBox(lblStep1, lblStep1Text),
				new HBox(lblStep2, lblStep2Text),
				new HBox(lblStep3, lblStep3Text));

		BibleMetadataPane bmp = new BibleMetadataPane();

		GridPane settingsPane = new GridPane();
		settingsPane.setHgap(5);
		settingsPane.setVgap(5);
		
		Label lblIncludeApocrypha = new Label(Translations.get("bible.apocrypha"));
		CheckBox chkIncludeApocrypha = new CheckBox();
		chkIncludeApocrypha.selectedProperty().bindBidirectional(context.getConfiguration().apocryphaIncludedProperty());
		settingsPane.add(lblIncludeApocrypha, 0, 0);
		settingsPane.add(chkIncludeApocrypha, 1, 0);
		
		TitledPane ttlImport = new TitledPane(Translations.get("bible.import.title"), importSteps);
		TitledPane ttlMetadata = new TitledPane(Translations.get("bible.properties.title"), bmp);
		TitledPane ttlSettings = new TitledPane(Translations.get("bible.settings.title"), settingsPane);
		
		right.getChildren().addAll(ttlImport, ttlMetadata, ttlSettings);
		
		this.lstBibles = new FlowListView<BibleListItem>(new BibleListViewCellFactory());
		this.lstBibles.itemsProperty().bindContent(context.getObservableBibleLibrary().getItems());
		this.lstBibles.setOrientation(Orientation.HORIZONTAL);
		
		ScrollPane leftScroller = new ScrollPane();
        leftScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroller.setFitToWidth(true);
		leftScroller.addEventHandler(KeyEvent.KEY_PRESSED, this::onBibleDelete);
		leftScroller.setFocusTraversable(true);
        leftScroller.setContent(this.lstBibles);
        leftScroller.setOnDragOver(this::onBibleDragOver);
        leftScroller.setOnDragDropped(this::onBibleDragDropped);
		
        ScrollPane rightScroller = new ScrollPane();
        rightScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroller.setFitToWidth(true);
        rightScroller.setContent(right);
        rightScroller.setMinWidth(250);
        
		SplitPane split = new SplitPane(leftScroller, rightScroller);
		split.setDividerPositions(0.75);
		SplitPane.setResizableWithParent(rightScroller, false);
		
		this.setCenter(split);
		
		this.lstBibles.selectionProperty().addListener((obs, ov, nv) -> {
        	if (nv == null) {
        		this.selected.set(null);
        	} else {
        		this.selected.set(nv.bible);
        	}
        });
        this.selected.addListener((obs, ov, nv) -> {
        	if (nv == null) {
        		lstBibles.selectionProperty().set(null);
        	} else {
        		lstBibles.selectionProperty().set(new BibleListItem(nv));
        	}
        });
        
        // wire up the selected media to the media metadata view with a unidirectional binding
        bmp.bibleProperty().bind(this.lstBibles.selectionProperty());
	}
	
	/**
	 * Called when something is dragged over the element.
	 * @param event the event
	 */
	private void onBibleDragOver(DragEvent event) {
		Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		} else {
			event.consume();
		}
	}
	
	/**
	 * Handler for when files have been drag and dropped to import.
	 * @param event the drag event
	 */
	private void onBibleDragDropped(DragEvent event) {
		// get the dragboard
		Dragboard db = event.getDragboard();
		// make sure it contains files
		if (db.hasFiles()) {
			// get the files
			final List<File> files = db.getFiles();
			
			// convert to paths
			final List<Path> paths = new ArrayList<Path>();
			for (File file : files) {
				paths.add(file.toPath());
			}
			
			// attempt to add to the library
			this.context.getObservableBibleLibrary().add(
					paths, 
					(List<Bible> bibles) -> {
						// get the warning files
						String[] wFileNames = bibles.stream().filter(b -> b.hadImportWarning()).map(f -> f.getName()).collect(Collectors.toList()).toArray(new String[0]);
						if (wFileNames.length > 0) {
							// show a dialog of the files that had warnings
							String list = String.join(", ", wFileNames);
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.initOwner(getScene().getWindow());
							alert.initModality(Modality.WINDOW_MODAL);
							alert.setTitle(Translations.get("bible.import.info.title"));
							alert.setHeaderText(Translations.get("bible.import.info.header"));
							alert.setContentText(list);
							alert.show();
						}
					},
					(List<FailedOperation<Path>> failures) -> {
						// get the exceptions
						Exception[] exceptions = failures.stream().map(f -> f.getException()).collect(Collectors.toList()).toArray(new Exception[0]);
						// get the failed media
						String list = String.join(", ", failures.stream().map(f -> f.getData().toAbsolutePath().toString()).collect(Collectors.toList()));
						Alert alert = Alerts.exception(
								getScene().getWindow(),
								null, 
								null,
								MessageFormat.format(Translations.get("bible.import.error"), list), 
								exceptions);
						alert.show();
					});
		}
		event.setDropCompleted(true);
		event.consume();
	}
	
	/**
	 * Handler for when bibles are deleted using the delete key
	 * @param event the key event
	 */
	private void onBibleDelete(KeyEvent event) {
		if (event.getCode() == KeyCode.DELETE) {
			List<Bible> bibles = new ArrayList<Bible>();
			for (BibleListItem item : lstBibles.selectionsProperty().get()) {
				// can't delete items that are still being imported
				if (item.loaded) {
					bibles.add(item.bible);
				}
			}
			if (bibles.size() > 0) {
				// attempt to delete the selected media
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.initOwner(getScene().getWindow());
				alert.initModality(Modality.WINDOW_MODAL);
				alert.setTitle(Translations.get("bible.remove.title"));
				alert.setContentText(Translations.get("bible.remove.content"));
				alert.setHeaderText(null);
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					// attempt to remove
					this.context.getObservableBibleLibrary().remove(
							bibles, 
							() -> {
								// nothing to do on success
							}, 
							(List<FailedOperation<Bible>> failures) -> {
								// get the exceptions
								Exception[] exceptions = failures.stream().map(f -> f.getException()).collect(Collectors.toList()).toArray(new Exception[0]);
								// get the failed media
								String list = String.join(", ", failures.stream().map(f -> f.getData().getName()).collect(Collectors.toList()));
								Alert fAlert = Alerts.exception(
										getScene().getWindow(),
										null, 
										null, 
										MessageFormat.format(Translations.get("bible.remove.error"), list), 
										exceptions);
								fAlert.show();
							});
				}
			}
		}
	}
}
