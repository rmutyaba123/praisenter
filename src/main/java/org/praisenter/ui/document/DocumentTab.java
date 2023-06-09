package org.praisenter.ui.document;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.praisenter.async.AsyncHelper;
import org.praisenter.data.Persistable;
import org.praisenter.data.bible.Bible;
import org.praisenter.data.slide.Slide;
import org.praisenter.data.song.Song;
import org.praisenter.ui.GlobalContext;
import org.praisenter.ui.bible.BibleEditor;
import org.praisenter.ui.controls.Dialogs;
import org.praisenter.ui.slide.SlideEditor;
import org.praisenter.ui.song.SongEditor;
import org.praisenter.ui.translations.Translations;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.stage.Modality;

public final class DocumentTab extends Tab {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String UNSAVED_DOCUMENT_CSS = "p-unsaved-document";
	
	private final GlobalContext context;
	private final DocumentContext<? extends Persistable> document;
	private final DocumentEditor<?> editor;
	
	public DocumentTab(
			GlobalContext context,
			DocumentContext<? extends Persistable> document) {
		this.context = context;
		this.document = document;
		
		this.editor = this.createEditorForDocument();
		
		// check for unsaved changes on close of a tab
		this.setOnCloseRequest(e -> {
			if (document.hasUnsavedChanges()) {
				Alert alert = Dialogs.yesNoCancel(
						this.context.getStage(), 
						Modality.WINDOW_MODAL, 
						Translations.get("action.confirm"), 
						Translations.get("action.close.save"), 
						Translations.get("action.close.discard"));
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent()) {
					if (result.get() == ButtonType.YES) {
						// then save the document first
						this.context.save(document).thenCompose(AsyncHelper.onJavaFXThreadAndWait(() -> {
							context.closeDocument(document);
						})).exceptionally(t -> {
							// don't close to the document and show the error
							Platform.runLater(() -> {
								Alert errorAlert = Dialogs.exception(this.context.getStage(), t);
								errorAlert.show();
							});
							return null;
						});
					} else if (result.get() == ButtonType.NO) {
						// they don't want to save the changes
						context.closeDocument(document);
					}
				}
			} else {
				// there was nothing to save
				context.closeDocument(document);
			}
			
			// otherwise dont do anything
			// the user closed the confirmation or chose cancel
			
			e.consume();
		});
		
		this.textProperty().bind(document.documentNameProperty());
		
		// set the default focus when the tab is selected
		this.selectedProperty().addListener((obs, ov, nv) -> {
			if (nv) {
				context.setCurrentDocument(document);
			}
		});
		
		document.hasUnsavedChangesProperty().addListener((obs, ov, nv) -> {
			this.setUnsavedChangesStyle(nv);
		});
		
		this.setUnsavedChangesStyle(document.isNew());
	}
	
	private void setUnsavedChangesStyle(boolean hasUnsavedChanges) {
		if (hasUnsavedChanges) {
			this.getStyleClass().add(UNSAVED_DOCUMENT_CSS);
		} else {
			this.getStyleClass().remove(UNSAVED_DOCUMENT_CSS);
		}
	}
	
	@SuppressWarnings("unchecked")
	private DocumentEditor<?> createEditorForDocument() {
		Persistable document = this.document.getDocument();
		if (document != null) {
			if (document.getClass() == Bible.class) {
				BibleEditor bep = new BibleEditor(this.context, (DocumentContext<Bible>)this.document);
				this.setContent(bep);
				return bep;
			} else if (document.getClass() == Slide.class) {
				SlideEditor sep = new SlideEditor(this.context, (DocumentContext<Slide>)this.document);
				this.setContent(sep);
				return sep;
			} else if (document.getClass() == Song.class) {
				SongEditor sep = new SongEditor(this.context, (DocumentContext<Song>)this.document);
				this.setContent(sep);
				return sep;
			} else {
				LOGGER.warn("No editor for class '" + document.getClass().getName() + "'.");
			}
		} else {
			LOGGER.warn("The document was null. Cannot edit a null document.");
		}
		
		UnknownDocumentEditor uep = new UnknownDocumentEditor((DocumentContext<Persistable>)this.document);
		this.setContent(uep);
		return uep;
	}
	
	public DocumentEditor<?> getDocumentEditor() {
		return this.editor;
	}
	
	public DocumentContext<?> getDocumentContext() {
		return this.document;
	}
}
