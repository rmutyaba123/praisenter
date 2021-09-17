package org.praisenter.ui;

import java.util.List;
import java.util.Locale;

import org.praisenter.data.workspace.PlaceholderTransitionBehavior;
import org.praisenter.data.workspace.WorkspaceConfiguration;
import org.praisenter.ui.controls.FormField;
import org.praisenter.ui.themes.Theme;
import org.praisenter.ui.translations.Translations;
import org.praisenter.utility.StringManipulator;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SettingsPane extends BorderPane {
	private final ObservableList<Theme> themes;
	private final ObservableList<Locale> locales;
	
	public SettingsPane(GlobalContext context) {
		WorkspaceConfiguration configuration = context.getWorkspaceConfiguration();
		
		// theme
		this.themes = FXCollections.observableArrayList(Theme.getAvailableThemes());
		ComboBox<Theme> cmbTheme = new ComboBox<>();
		Bindings.bindContent(cmbTheme.getItems(), this.themes);
		cmbTheme.setValue(Theme.getTheme(configuration.getThemeName()));
		
		Button btnRefreshThemes = new Button(Translations.get("refresh"));
		btnRefreshThemes.setOnAction(e -> {
			this.themes.setAll(Theme.getAvailableThemes());
		});
		
		cmbTheme.valueProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				configuration.setThemeName(nv.getName());
			}
		});
		
		// language
		List<Locale> locales = Translations.getAvailableLocales();
		Locale locale = Locale.getDefault();
		String languageTag = configuration.getLanguageTag();
		if (!StringManipulator.isNullOrEmpty(languageTag)) {
			try {
				locale = Locale.forLanguageTag(languageTag);
			} catch (Exception ex) {
				// TODO log this
			}
		}
		locale = Translations.getClosestMatch(locale, locales);
		this.locales = FXCollections.observableArrayList(locales);
		ComboBox<Locale> cmbLocales = new ComboBox<>();
		Bindings.bindContent(cmbLocales.getItems(), this.locales);
		cmbLocales.setValue(locale);
		
		Button btnRefreshLocales = new Button(Translations.get("refresh"));
		btnRefreshLocales.setOnAction(e -> {
			this.locales.setAll(Translations.getAvailableLocales());
		});
		
		cmbLocales.valueProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				configuration.setLanguageTag(nv.toLanguageTag());
			}
		});

		// placeholder transition behavior
		ObservableList<Option<PlaceholderTransitionBehavior>> behaviors = FXCollections.observableArrayList();
		behaviors.add(new Option<PlaceholderTransitionBehavior>(Translations.get("settings.slide.placeholderTransitionBehavior." + PlaceholderTransitionBehavior.PLACEHOLDERS), PlaceholderTransitionBehavior.PLACEHOLDERS));
		behaviors.add(new Option<PlaceholderTransitionBehavior>(Translations.get("settings.slide.placeholderTransitionBehavior." + PlaceholderTransitionBehavior.CONTENT), PlaceholderTransitionBehavior.CONTENT));
		behaviors.add(new Option<PlaceholderTransitionBehavior>(Translations.get("settings.slide.placeholderTransitionBehavior." + PlaceholderTransitionBehavior.SLIDE), PlaceholderTransitionBehavior.SLIDE));
		ComboBox<Option<PlaceholderTransitionBehavior>> cmbPlaceholderTransitionBehavior = new ComboBox<Option<PlaceholderTransitionBehavior>>(behaviors);
		cmbPlaceholderTransitionBehavior.setValue(new Option<>(null, configuration.getPlaceholderTransitionBehavior()));
		
		cmbPlaceholderTransitionBehavior.valueProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				configuration.setPlaceholderTransitionBehavior(nv.getValue());
			}
		});
		
		// wait for transition flag
		CheckBox chkWaitForTransition = new CheckBox();
		chkWaitForTransition.setSelected(configuration.isWaitForTransitionsToCompleteEnabled());
		chkWaitForTransition.selectedProperty().addListener((obs, ov, nv) -> {
			configuration.setWaitForTransitionsToCompleteEnabled(nv);
		});
		
		// audio transcode enabled
		CheckBox chkTranscodeAudio = new CheckBox();
		chkTranscodeAudio.setSelected(configuration.isAudioTranscodingEnabled());
		chkTranscodeAudio.selectedProperty().addListener((obs, ov, nv) -> {
			configuration.setAudioTranscodingEnabled(nv);
		});
		
		// video transcode enabled
		CheckBox chkTranscodeVideo = new CheckBox();
		chkTranscodeVideo.setSelected(configuration.isVideoTranscodingEnabled());
		chkTranscodeVideo.selectedProperty().addListener((obs, ov, nv) -> {
			configuration.setVideoTranscodingEnabled(nv);
		});
		
		// volume adjust enabled
		CheckBox chkAdjustVolume = new CheckBox();
		chkAdjustVolume.setSelected(configuration.isVolumeAdjustmentEnabled());
		chkAdjustVolume.selectedProperty().addListener((obs, ov, nv) -> {
			configuration.setVolumeAdjustmentEnabled(nv);
		});
		
		// audio transcode extension
		TextField txtAudioTranscodeExtension = new TextField(configuration.getAudioTranscodeExtension());
		txtAudioTranscodeExtension.setMaxWidth(100);
		txtAudioTranscodeExtension.textProperty().addListener((obs, ov, nv) -> {
			configuration.setAudioTranscodeExtension(nv);
		});
		
		// video transcode extension
		TextField txtVideoTranscodeExtension = new TextField(configuration.getVideoTranscodeExtension());
		txtVideoTranscodeExtension.setMaxWidth(100);
		txtVideoTranscodeExtension.textProperty().addListener((obs, ov, nv) -> {
			configuration.setVideoTranscodeExtension(nv);
		});
		
		// audio transcode command
		TextField txtAudioTranscodeCommand = new TextField(configuration.getAudioTranscodeCommand());
		txtAudioTranscodeCommand.setMaxWidth(Double.MAX_VALUE);
		txtAudioTranscodeCommand.textProperty().addListener((obs, ov, nv) -> {
			configuration.setAudioTranscodeCommand(nv);
		});
		
		// video transcode command
		TextField txtVideoTranscodeCommand = new TextField(configuration.getVideoTranscodeCommand());
		txtVideoTranscodeCommand.setMaxWidth(Double.MAX_VALUE);
		txtVideoTranscodeCommand.textProperty().addListener((obs, ov, nv) -> {
			configuration.setVideoTranscodeCommand(nv);
		});
		
		// video frame extract command
		TextField txtVideoExtractCommand = new TextField(configuration.getVideoFrameExtractCommand());
		txtVideoExtractCommand.setMaxWidth(Double.MAX_VALUE);
		txtVideoExtractCommand.textProperty().addListener((obs, ov, nv) -> {
			configuration.setVideoFrameExtractCommand(nv);
		});
		
		// target mean volume
		Spinner<Double> spnTargetVolume = new Spinner<>(-100, 100, configuration.getTargetMeanVolume(), 1);
		spnTargetVolume.valueProperty().addListener((obs, ov, nv) -> {
			configuration.setTargetMeanVolume(nv);
		});
		
		// bible renumber
		CheckBox chkBibleRenumberWarning = new CheckBox();
		chkBibleRenumberWarning.setSelected(configuration.isRenumberBibleWarningEnabled());
		chkBibleRenumberWarning.selectedProperty().addListener((obs, ov, nv) -> {
			configuration.setRenumberBibleWarningEnabled(nv);
		});
		
		// bible reorder
		CheckBox chkBibleReorderWarning = new CheckBox();
		chkBibleReorderWarning.setSelected(configuration.isReorderBibleWarningEnabled());
		chkBibleReorderWarning.selectedProperty().addListener((obs, ov, nv) -> {
			configuration.setReorderBibleWarningEnabled(nv);
		});
		
		// debug mode
		CheckBox chkDebugMode = new CheckBox();
		chkDebugMode.setSelected(configuration.isDebugModeEnabled());
		chkDebugMode.selectedProperty().addListener((obs, ov, nv) -> {
			configuration.setDebugModeEnabled(nv);
		});
		
		VBox boxGeneral = new VBox(
				new FormField(Translations.get("settings.theme"), Translations.get("settings.theme.description"), cmbTheme, btnRefreshThemes),
				new FormField(Translations.get("settings.locale"), Translations.get("settings.locale.description"), cmbLocales, btnRefreshLocales),
				new FormField(Translations.get("settings.debug"), Translations.get("settings.debug.description"), chkDebugMode));
		TitledPane pneGeneral = new TitledPane(Translations.get("settings.general"), boxGeneral);
		
		VBox boxSlide = new VBox(
				new FormField(Translations.get("settings.slide.waitForTransition"), Translations.get("settings.slide.waitForTransition.description"), chkWaitForTransition),
				new FormField(Translations.get("settings.slide.placeholderTransitionBehavior"), Translations.get("settings.slide.placeholderTransitionBehavior.description"), cmbPlaceholderTransitionBehavior));
		TitledPane pneSlide = new TitledPane(Translations.get("settings.slide"), boxSlide);
		
		VBox boxMedia = new VBox(
				new FormField(Translations.get("settings.media.audioTranscode"), chkTranscodeAudio),
				new FormField(Translations.get("settings.media.audioTranscode.extension"), txtAudioTranscodeExtension),
				new FormField(Translations.get("settings.media.audioTranscode.command"), txtAudioTranscodeCommand),
				new FormField(Translations.get("settings.media.videoTranscode"), chkTranscodeVideo),
				new FormField(Translations.get("settings.media.videoTranscode.extension"), txtVideoTranscodeExtension),
				new FormField(Translations.get("settings.media.videoTranscode.command"), txtVideoTranscodeCommand),
				new FormField(Translations.get("settings.media.videoFrameExtract.command"), txtVideoExtractCommand),
				new FormField(Translations.get("settings.media.adjustVolume"), chkAdjustVolume),
				new FormField(Translations.get("settings.media.targetVolume"), spnTargetVolume));
		TitledPane pneAV = new TitledPane(Translations.get("settings.media"), boxMedia);
		
		VBox boxBible = new VBox(
				new FormField(Translations.get("settings.bible.renumberWarning"), chkBibleRenumberWarning),
				new FormField(Translations.get("settings.bible.reorderWarning"), chkBibleReorderWarning));
		TitledPane pneBible = new TitledPane(Translations.get("settings.bible"), boxBible);
		
		VBox layout = new VBox(
				pneGeneral,
				pneSlide,
				pneBible,
				pneAV);
		layout.getStyleClass().add("settings");
		
		ScrollPane scrLayout = new ScrollPane(layout);
		scrLayout.setFitToWidth(true);
		scrLayout.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		this.setCenter(scrLayout);
	}
}
