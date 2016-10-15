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
package org.praisenter.javafx.media;

import java.io.File;
import java.nio.file.Path;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.praisenter.FailedOperation;
import org.praisenter.Tag;
import org.praisenter.javafx.Alerts;
import org.praisenter.javafx.ApplicationAction;
import org.praisenter.javafx.ApplicationPane;
import org.praisenter.javafx.ApplicationPaneEvent;
import org.praisenter.javafx.FlowListView;
import org.praisenter.javafx.Option;
import org.praisenter.javafx.PraisenterContext;
import org.praisenter.javafx.SortGraphic;
import org.praisenter.media.Media;
import org.praisenter.media.MediaType;
import org.praisenter.resources.translations.Translations;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;

/**
 * Pane specifically for showing the media in a media library.
 * @author William Bittle
 * @version 3.0.0
 */
public final class MediaLibraryPane extends BorderPane implements ApplicationPane {
	/** The class-level loader */
	private static final Logger LOGGER = LogManager.getLogger();
	
	/** The collator for locale dependent sorting */
	private static final Collator COLLATOR = Collator.getInstance();
	
	// data
	
	/** The praisenter context */
	private final PraisenterContext context;
	
	// selection
	
	/** The selected media */
	private final ObjectProperty<Media> selected;
	
	// filters
	
	/** The media type filter */
	private final ObjectProperty<Option<MediaType>> typeFilter;
	
	/** The tag filter */
	private final ObjectProperty<Option<Tag>> tagFilter;
	
	/** The search */
	private final StringProperty textFilter;
	
	// sorting
	
	/** The sort property */
	private final ObjectProperty<Option<MediaSortField>> sortField;
	
	/** The sort direction */
	private final BooleanProperty sortDescending;
	
	// nodes
	
	/** The media list view */
	private final FlowListView<MediaListItem> lstMedia;
	
	/** The media metadata pane */
	private final MediaMetadataPane pneMetadata;
	
	/** The media preview player */
	private final MediaPlayerPane pnePlayer;
	
    /**
     * Full constructor.
     * @param context the {@link PraisenterContext}
     * @param orientation the orientation of the flow of items
     * @param types the desired {@link MediaType}s to show; null or empty to show all
     */
    public MediaLibraryPane(
    		PraisenterContext context, 
    		Orientation orientation, 
    		MediaType... types) {
    	this.context = context;
		this.typeFilter = new SimpleObjectProperty<>(new Option<MediaType>());
		this.tagFilter = new SimpleObjectProperty<>(new Option<Tag>());
		this.textFilter = new SimpleStringProperty();
		this.sortField = new SimpleObjectProperty<Option<MediaSortField>>(new Option<MediaSortField>(MediaSortField.NAME.getName(), MediaSortField.NAME));
		this.sortDescending = new SimpleBooleanProperty(true);
    	
		this.selected = new SimpleObjectProperty<Media>();
		
		final ObservableMediaLibrary library = context.getMediaLibrary();
		final ObservableSet<Tag> tags = context.getTags();
		
        // add sorting and filtering capabilities
		ObservableList<MediaListItem> theList = library.getItems();
        FilteredList<MediaListItem> filtered = new FilteredList<MediaListItem>(theList, p -> true);
        SortedList<MediaListItem> sorted = new SortedList<MediaListItem>(filtered);
        
        // define a general listener for all the filters and sorting
        InvalidationListener filterListener = new InvalidationListener() {
			@Override
			public void invalidated(Observable obs) {
				MediaType type = typeFilter.get().getValue();
				Tag tag = tagFilter.get().getValue();
				String text = textFilter.get();
				MediaSortField field = sortField.get().getValue();
				boolean desc = sortDescending.get();
				filtered.setPredicate(m -> {
					if (!m.loaded || 
						((type == null || m.media.getType() == type) &&
						 (tag == null || m.media.getTags().contains(tag)) &&
						 (text == null || text.length() == 0 || m.media.getName().toLowerCase().contains(text.toLowerCase())))) {
						// make sure its in the available types
						if (types != null && types.length > 0 && m.loaded) {
							for (MediaType t : types) {
								if (t == m.media.getType()) {
									return true;
								}
							}
							return false;
						}
						return true;
					}
					return false;
				});
				sorted.setComparator(new Comparator<MediaListItem>() {
					@Override
					public int compare(MediaListItem o1, MediaListItem o2) {
						int value = 0;
						if (field == MediaSortField.NAME) {
							value = COLLATOR.compare(o1.name, o2.name);
						} else {
							// check for loaded vs. not loaded media
							// sort non-loaded media to the end
							if (o1.media == null && o2.media == null) return 0;
							if (o1.media == null && o2.media != null) return 1;
							if (o1.media != null && o2.media == null) return -1;
							
							if (field == MediaSortField.TYPE) {
								value = o1.media.getType().compareTo(o2.media.getType());
							} else {
								value = -1 * (o1.media.getDateAdded().compareTo(o2.media.getDateAdded()));
							}
						}
						return (desc ? 1 : -1) * value;
					}
				});
			}
		};
		this.textFilter.addListener(filterListener);
		this.typeFilter.addListener(filterListener);
		this.tagFilter.addListener(filterListener);
		this.sortField.addListener(filterListener);
		this.sortDescending.addListener(filterListener);
		filterListener.invalidated(null);
        
        final MediaType[] mediaTypes = types != null && types.length > 0 ? types : MediaType.values();
        ObservableList<Option<MediaType>> opTypes = FXCollections.observableArrayList();
        // add the all option
        opTypes.add(new Option<>());
        // add the current options
        opTypes.addAll(Arrays.asList(mediaTypes).stream().map(t -> new Option<MediaType>(Translations.get(t.getClass().getName() + "." + t.name()), t)).collect(Collectors.toList()));

        // sorting options
        ObservableList<Option<MediaSortField>> sortFields = FXCollections.observableArrayList();
        sortFields.addAll(Arrays.asList(MediaSortField.values())
        		.stream()
        		// don't include the type sort if theres only one
        		.filter(t -> t != MediaSortField.TYPE || (t == MediaSortField.TYPE && (types == null || types.length != 1)))
        		.map(t -> new Option<MediaSortField>(t.getName(), t))
        		.collect(Collectors.toList()));
        
        ObservableList<Option<Tag>> opTags = FXCollections.observableArrayList();
        // add the all option
        opTags.add(new Option<>());
        // add the current options
        opTags.addAll(tags.stream().map(t -> new Option<Tag>(t.getName(), t)).collect(Collectors.toList()));
        // add a listener for more tags being added
        tags.addListener(new SetChangeListener<Tag>() {
        	@Override
        	public void onChanged(SetChangeListener.Change<? extends Tag> change) {
        		if (change.wasRemoved()) {
        			opTags.removeIf(fo -> fo.getValue().equals(change.getElementRemoved()));
        		}
        		if (change.wasAdded()) {
        			Tag tag = change.getElementAdded();
        			opTags.add(new Option<Tag>(tag.getName(), tag));
        		}
        	}
		});
        
        // the right side of the split pane
        this.lstMedia = new FlowListView<MediaListItem>(new MediaListViewCellFactory(context.getMediaLibrary().getThumbnailSettings()));
        this.lstMedia.itemsProperty().bindContent(sorted);
        this.lstMedia.setOrientation(orientation);
        
        ScrollPane leftScroller = new ScrollPane();
        leftScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        if (orientation == Orientation.HORIZONTAL) {
        	leftScroller.setFitToWidth(true);
        } else {
        	leftScroller.setFitToHeight(true);
        }
		leftScroller.addEventHandler(KeyEvent.KEY_PRESSED, this::onMediaDelete);
		leftScroller.setFocusTraversable(true);
        leftScroller.setContent(this.lstMedia);
        leftScroller.setOnDragDropped(this::onMediaDropped);
        leftScroller.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					event.acceptTransferModes(TransferMode.COPY);
				} else {
					event.consume();
				}
			}
        });
        
        // only scroll down when media is being added
        // we know this when there's medialistitems added
        // that have the loaded flag = false
        theList.addListener(new ListChangeListener<MediaListItem>() {
        	@Override
        	public void onChanged(javafx.collections.ListChangeListener.Change<? extends MediaListItem> c) {
        		boolean added = false;
        		while (c.next()) {
        			if (c.wasAdded()) {
        				for (int i = 0; i < c.getAddedSize(); i++) {
        					MediaListItem item = c.getAddedSubList().get(i);
        					added |= item != null && item.loaded == false;
        				}
        			}
        		}
        		if (added) {
        			leftScroller.setVvalue(leftScroller.getVmax());
        		}
        	}
        });

        this.lstMedia.selectionProperty().addListener((obs, ov, nv) -> {
        	if (nv == null) {
        		this.selected.set(null);
        	} else {
        		this.selected.set(nv.media);
        	}
        });
        this.selected.addListener((obs, ov, nv) -> {
        	if (nv == null) {
        		lstMedia.selectionProperty().set(null);
        	} else {
        		lstMedia.selectionProperty().set(new MediaListItem(nv));
        	}
        });
        
        this.pneMetadata = new MediaMetadataPane(tags);
        // wire up the selected media to the media metadata view with a unidirectional binding
        this.pneMetadata.mediaProperty().bind(this.lstMedia.selectionProperty());
        this.pneMetadata.addEventHandler(MediaMetadataEvent.RENAME, this::onMediaRename);
        this.pneMetadata.addEventHandler(MediaMetadataEvent.ADD_TAG, this::onMediaTagAdded);
        this.pneMetadata.addEventHandler(MediaMetadataEvent.REMOVE_TAG, this::onMediaTagRemoved);

        // setup preview pane for video/audio
        this.pnePlayer = new MediaPlayerPane();
        this.lstMedia.selectionProperty().addListener((obs, oldValue, newValue) -> {
        	MediaPlayer player = null;
        	MediaType type = null;
        	if (newValue != null && newValue.media != null) {
        		type = newValue.media.getType();
        		if (type == MediaType.AUDIO || type == MediaType.VIDEO) {
        			javafx.scene.media.Media m = new javafx.scene.media.Media(newValue.media.getPath().toUri().toString());
        			Exception ex = m.getError();
        			if (ex != null) {
        				LOGGER.error("Error loading media " + newValue.media.getName(), ex);
        			} else {
        				player = new MediaPlayer(m);
        				ex = player.getError();
        				if (ex != null) {
        					player = null;
        					LOGGER.error("Error creating media player for " + newValue.media.getName(), ex);
        				}
        			}
        		}
        	}
        	pnePlayer.setMediaPlayer(player, type);
        	fireEvent(new ApplicationPaneEvent(this.lstMedia, MediaLibraryPane.this, ApplicationPaneEvent.STATE_CHANGED, MediaLibraryPane.this));
        });
        
        Label lblImport = new Label(Translations.get("media.import.step1"));
        lblImport.setPadding(new Insets(7));
        lblImport.setWrapText(true);
        
        TitledPane ttlImport = new TitledPane(Translations.get("media.import.title"), lblImport);
        TitledPane ttlMetadata = new TitledPane(Translations.get("media.metadata.title"), this.pneMetadata);
        TitledPane ttlPreview = new TitledPane(Translations.get("media.preview.title"), this.pnePlayer);
        
        VBox rightGroup = new VBox(ttlImport, ttlMetadata);
        if (!(mediaTypes.length == 1 && mediaTypes[0] == MediaType.IMAGE)) {
        	rightGroup.getChildren().add(ttlPreview);
        }
        ScrollPane rightScroller = new ScrollPane();
        rightScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroller.setFitToWidth(true);
        rightScroller.setContent(rightGroup);
        rightScroller.setMinWidth(250);
        
        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.HORIZONTAL);
        
        split.getItems().add(leftScroller);
        split.getItems().add(rightScroller);
        split.setDividerPositions(0.9);
        SplitPane.setResizableWithParent(rightScroller, Boolean.FALSE);
        
        // scale the media player pane's fit property to the scroller's width property minus some for padding
        this.pnePlayer.mediaFitWidthProperty().bind(rightScroller.widthProperty().subtract(20));
        this.pnePlayer.setPadding(new Insets(8, 0, 0, 0));
        
        // FILTERING & SORTING
        
        Label lblFilter = new Label(Translations.get("field.filter"));
        ComboBox<Option<MediaType>> cbTypes = new ComboBox<Option<MediaType>>(opTypes);
        cbTypes.setValue(new Option<>());
        cbTypes.valueProperty().bindBidirectional(this.typeFilter);
        
        ComboBox<Option<Tag>> cbTags = new ComboBox<Option<Tag>>(opTags);
        cbTags.valueProperty().bindBidirectional(this.tagFilter);
        cbTags.setValue(new Option<>());
        
        Label lblSort = new Label(Translations.get("field.sort"));
        ChoiceBox<Option<MediaSortField>> cbSort = new ChoiceBox<Option<MediaSortField>>(sortFields);
        cbSort.valueProperty().bindBidirectional(this.sortField);
        SortGraphic sortGraphic = new SortGraphic(17, 0, 4, 2, 4);
        ToggleButton tgl = new ToggleButton(null, sortGraphic);
        tgl.selectedProperty().bindBidirectional(this.sortDescending);
        sortGraphic.flipProperty().bind(this.sortDescending);
        
        TextField txtSearch = new TextField();
        txtSearch.setPromptText(Translations.get("field.search.placeholder"));
        txtSearch.textProperty().bindBidirectional(this.textFilter);
        
        HBox pFilter = new HBox(); 
        pFilter.setAlignment(Pos.BASELINE_LEFT);
        pFilter.setSpacing(5);
        
        pFilter.getChildren().add(lblFilter);
        // don't include the type filter if theres only one type
        if (types == null || types.length != 1) {
        	pFilter.getChildren().add(cbTypes);
        }
        pFilter.getChildren().addAll(cbTags, txtSearch);
        
        HBox pSort = new HBox();
        pSort.setAlignment(Pos.CENTER_LEFT);
        pSort.setSpacing(5);
        pSort.getChildren().addAll(lblSort, cbSort, tgl);
        
        FlowPane top = new FlowPane();
        top.setHgap(5);
        top.setVgap(5);
        top.setAlignment(Pos.BASELINE_LEFT);
        top.setPadding(new Insets(5));
        top.setPrefWrapLength(0);
        
        top.getChildren().addAll(pFilter, pSort);
        
        this.setTop(top);
        this.setCenter(split);
    }
    
    /**
     * Event handler for dragging files to import media.
     * @param event the event
     */
    private final void onMediaDropped(DragEvent event) {
    	// check the type of information stored in the dragboard
		Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			// get the files
			final List<File> files = db.getFiles();
			
			// convert to list of paths
			final List<Path> paths = new ArrayList<Path>();
			for (File file : files) {
				paths.add(file.toPath());
			}
			
			// attempt to import them
			this.context.getMediaLibrary().add(
					paths, 
					(List<Media> media) -> {
						// nothing to do on success
					}, 
					(List<FailedOperation<Path>> failures) -> {
						// get the exceptions
						Exception[] exceptions = failures.stream().map(f -> f.getException()).collect(Collectors.toList()).toArray(new Exception[0]);
						// get the failed media
						String list = String.join(", ", failures.stream().map(f -> f.getData().getFileName().toString()).collect(Collectors.toList()));
						Alert alert = Alerts.exception(
								getScene().getWindow(),
								null, 
								null, 
								MessageFormat.format(Translations.get("media.import.error"), list), 
								exceptions);
						alert.show();
					});
		}
		event.setDropCompleted(true);
		event.consume();
	}
    
    /**
     * Event handler for the delete key for removing media.
     * @param event the event
     */
    private final void onMediaDelete(KeyEvent event) {
    	// make sure the file isn't being previewed
    	this.pnePlayer.setMediaPlayer(null, null);
    	// check the key code
		if (event.getCode() == KeyCode.DELETE) {
			// collect the items that are selected
			List<Media> items = new ArrayList<Media>();
			for (MediaListItem item : this.lstMedia.selectionsProperty().get()) {
				// only include those that are imported
				if (item.loaded && item.media != null) {
					items.add(item.media);
				}
			}

			// are there any items selected?
			if (items.size() > 0) {
				// make sure the user really wants to do this
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.initOwner(getScene().getWindow());
				alert.initModality(Modality.WINDOW_MODAL);
				alert.setTitle(Translations.get("media.remove.title"));
				alert.setContentText(Translations.get("media.remove.content"));
				alert.setHeaderText(null);
				Optional<ButtonType> result = alert.showAndWait();
				
				if (result.get() == ButtonType.OK) {
					// attempt to delete the selected media
					this.context.getMediaLibrary().remove(items, () -> {
						// shouldn't have to do anything on success
					}, (List<FailedOperation<Media>> failures) -> {
						// on failure we should notify the user
						// get the exceptions
						Exception[] exceptions = failures.stream().map(f -> f.getException()).collect(Collectors.toList()).toArray(new Exception[0]);
						// get the failed media
						String list = String.join(", ", failures.stream().map(f -> f.getData().getName()).collect(Collectors.toList()));
						Alert fAlert = Alerts.exception(
								getScene().getWindow(),
								null, 
								null, 
								MessageFormat.format(Translations.get("media.remove.error"), list), 
								exceptions);
						fAlert.show();
					});
				}
			}
		}
    }
    
    /**
     * Event handler for renaming media.
     * @param event the event
     */
    private final void onMediaRename(MediaRenameEvent event) {
    	// make sure the file isn't being previewed
    	this.pnePlayer.setMediaPlayer(null, null);
    	// update the media's name
    	this.context.getMediaLibrary().rename(
    			event.media,
    			event.name, 
    			(Media media) -> {
    				// nothing to do on success
    			}, 
    			(Media media, Throwable ex) -> {
    				// log the error
    				LOGGER.error("Failed to rename media from '{}' to '{}': {}", event.getMedia().getName(), event.getName(), ex.getMessage());
    				// show an error to the user
    				Alert alert = Alerts.exception(
    						getScene().getWindow(),
    						null, 
    						null, 
    						MessageFormat.format(Translations.get("media.metadata.rename.error"), event.getMedia().getName(), event.getName()), 
    						ex);
    				alert.show();
    			});
    }
    
    /**
     * Event handler for adding a tag to a media item.
     * @param event the event
     */
    private final void onMediaTagAdded(MediaTagEvent event) {
    	MediaListItem item = event.media;
    	Media media = item.media;
    	Tag tag = event.tag;
    	
    	this.context.getMediaLibrary().addTag(
    			media, 
    			tag, 
    			(Tag addedTag) -> {
    				// add the tag to the global list of tags
    				this.context.getTags().add(addedTag);
    			},
    			(Tag failedTag, Throwable ex) -> {
    				// remove it from the tags
    				item.tags.remove(tag);
    				// log the error
    				LOGGER.error("Failed to add tag '{}' for '{}': {}", tag.getName(), media.getPath().toAbsolutePath().toString(), ex.getMessage());
    				// show an error to the user
    				Alert alert = Alerts.exception(
    						getScene().getWindow(),
    						null, 
    						null, 
    						MessageFormat.format(Translations.get("tags.add.error"), tag.getName()), 
    						ex);
    				alert.show();
    			});
    }
    
    /**
     * Event handler for removing a tag from a media item.
     * @param event the event
     */
    private final void onMediaTagRemoved(MediaTagEvent event) {
    	MediaListItem item = event.media;
    	Media media = item.media;
    	Tag tag = event.tag;
    	
    	this.context.getMediaLibrary().removeTag(
    			media, 
    			tag, 
    			null,  // nothing to do on success
    			(Tag failedTag, Throwable ex) -> {
    				// add it back
    				item.tags.add(tag);
    				// log the error
    				LOGGER.error("Failed to remove tag '{}' for '{}': {}", tag.getName(), media.getPath().toAbsolutePath().toString(), ex.getMessage());
    				// show an error to the user
    				Alert alert = Alerts.exception(
    						getScene().getWindow(),
    						null, 
    						null, 
    						MessageFormat.format(Translations.get("tags.remove.error"), tag.getName()), 
    						ex);
    				alert.show();
    			});
    }
    
    @Override
    public boolean isApplicationActionEnabled(ApplicationAction action) {
    	boolean isSingleSelected = this.selected.get() != null;
    	switch (action) {
			case RENAME:
				return isSingleSelected;
			default:
				break;
		}
    	return false;
    }
    
    @Override
    public boolean isApplicationActionVisible(ApplicationAction action) {
    	return true;
    }
    
    /**
     * Returns the selected property.
     * @return ObjectProperty&lt;{@link Media}&gt;
     */
    public ObjectProperty<Media> selectedProperty() {
    	return this.selected;
    }
    
    /**
     * Returns the selected media.
     * @return {@link Media} or null
     */
    public Media getSelected() {
    	return this.selected.get();
    }
    
    /**
     * Sets the selected media.
     * @param media the media
     */
    public void setSelected(Media media) {
    	this.selected.set(media);
    }
}