package dev.tim9h.rcp.clock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.rcp.event.Event;
import dev.tim9h.rcp.event.EventManager;
import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.spi.Gravity;
import dev.tim9h.rcp.spi.Plugin;
import dev.tim9h.rcp.spi.Position;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

public class ClockView implements Plugin {

	private static final Pattern SVG_PATH = Pattern.compile("<path[^>]* d=\"([^\"]+)\"");

	private static final double ICON_SCALE = 16.0 / 24;

	static final String SETTING_DATEFORMAT = "clock.dateformat.right";

	static final String SETTING_SHOW_SECONDS = "clock.time.showseconds";

	static final String SETTING_12_HOUR = "clock.time.12hour";

	@InjectLogger
	private Logger logger;

	@Inject
	private TimeProperty timeProperty;

	@Inject
	private DateProperty dateProperty;

	private StringProperty nextEventProperty;

	private boolean hasNextEvent;

	private boolean calendarEventReceived;

	private Label lblNext = new Label("NEXT");

	private Label lblNextEvent = new Label();

	private Label lblNoEvent = new Label("No upcoming events");

	private Label lblSeconds = new Label();

	@Override
	public String getName() {
		return "Clock";
	}

	@Override
	public void init() {
		nextEventProperty = new SimpleStringProperty();
	}

	@Override
	public Optional<Node> getNode() throws IOException {
		var hbox = new HBox(16);
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.getStyleClass().add("ccCard");

		var lblTime = new Label();
		lblTime.getStyleClass().addAll("accent-label", "highlighted-label");
		lblTime.textProperty().bind(timeProperty);
		lblTime.setAlignment(Pos.CENTER_LEFT);
		lblTime.setStyle("-fx-padding: 1px 0 1px 4px; -fx-border-width: 1px 0 1px 1px;");

		lblSeconds.textProperty().bind(timeProperty.getSecondsProperty());
		lblSeconds.setAlignment(Pos.CENTER_LEFT);
		lblSeconds.setStyle("-fx-padding: 1px 4px 1px 0; -fx-border-width: 1px 1px 1px 0;");
		updateTimeVisibility();
		var timeGroup = new HBox(0, lblTime, lblSeconds);
		timeGroup.setAlignment(Pos.CENTER_LEFT);

		lblNext.getStyleClass().addAll("accent-label", "detail-label");
		lblNext.setGraphic(loadSvgIcon("/images/next_event.svg", lblNext));
		lblNext.setAlignment(Pos.CENTER);
		lblNext.setMaxWidth(Double.MAX_VALUE);
		lblNext.setVisible(hasNextEvent);
		lblNext.setManaged(hasNextEvent);

		lblNextEvent.getStyleClass().add("detail-label");
		lblNextEvent.textProperty().bind(nextEventProperty);
		lblNextEvent.setAlignment(Pos.CENTER);
		lblNextEvent.setMaxWidth(Double.MAX_VALUE);
		lblNextEvent.setTextOverrun(OverrunStyle.ELLIPSIS);
		lblNextEvent.setVisible(hasNextEvent);
		lblNextEvent.setManaged(hasNextEvent);
		var eventGroup = new HBox(0, lblNext, lblNextEvent);
		eventGroup.setAlignment(Pos.CENTER);
		HBox.setHgrow(eventGroup, Priority.ALWAYS);

		lblNoEvent.getStyleClass().addAll("detail-label", "secondary");
		lblNoEvent.setGraphic(loadSvgIcon("/images/no_event.svg", lblNoEvent));
		lblNoEvent.setAlignment(Pos.CENTER);
		lblNoEvent.setMaxWidth(Double.MAX_VALUE);
		lblNoEvent.setTextOverrun(OverrunStyle.ELLIPSIS);
		lblNoEvent.setVisible(calendarEventReceived && !hasNextEvent);
		lblNoEvent.setManaged(calendarEventReceived && !hasNextEvent);
		var eventContent = new StackPane(lblNoEvent, eventGroup);
		StackPane.setAlignment(lblNoEvent, Pos.CENTER);
		StackPane.setAlignment(eventGroup, Pos.CENTER);
		HBox.setHgrow(eventContent, Priority.ALWAYS);

		var lblDate = new Label();
		lblDate.getStyleClass().add("detail-label");
		lblDate.textProperty().bind(dateProperty);
		lblDate.setMinWidth(Region.USE_PREF_SIZE);
		lblDate.setAlignment(Pos.CENTER_RIGHT);

		hbox.getChildren().addAll(timeGroup, eventContent, lblDate);

		return Optional.of(hbox);
	}

	private SVGPath loadSvgIcon(String resourcePath, Label label) throws IOException {
		try (var input = getClass().getResourceAsStream(resourcePath)) {
			if (input == null) {
				throw new IOException("Missing SVG resource: " + resourcePath);
			}
			var svg = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			var matcher = SVG_PATH.matcher(svg);
			if (!matcher.find()) {
				throw new IOException("Invalid SVG resource: " + resourcePath);
			}
			var icon = new SVGPath();
			icon.setContent(matcher.group(1));
			icon.setScaleX(ICON_SCALE);
			icon.setScaleY(ICON_SCALE);
			icon.fillProperty().bind(label.textFillProperty());
			return icon;
		}
	}

	@Override
	public void initBus(EventManager eventManager) {
		Plugin.super.initBus(eventManager);
		eventManager.listen(Event.EVENT_CALENDAR_NEXT_EVENT, event -> {
			calendarEventReceived = true;
			hasNextEvent = event != null;
			updateEventVisibility();
			nextEventProperty.set(formatNextEvent(event));
		});
	}

	private void updateEventVisibility() {
		var showNextEvent = calendarEventReceived && hasNextEvent;
		var showNoEvent = calendarEventReceived && !hasNextEvent;
		lblNext.setVisible(showNextEvent);
		lblNext.setManaged(showNextEvent);
		lblNextEvent.setVisible(showNextEvent);
		lblNextEvent.setManaged(showNextEvent);
		lblNoEvent.setVisible(showNoEvent);
		lblNoEvent.setManaged(showNoEvent);
	}

	private void updateTimeVisibility() {
		var showSeconds = timeProperty.isShowSeconds();
		lblSeconds.setVisible(showSeconds);
		lblSeconds.setManaged(showSeconds);
	}

	private String formatNextEvent(Object[] event) {
		if (event == null) {
			return "";
		}
		var summary = (String) event[0];
		var start = (OffsetDateTime) event[1];
		var allDay = (Boolean) event[2];
		if (allDay) {
			return "ALL DAY  ·  " + summary;
		}
		return start.format(DateTimeFormatter.ofPattern("HH:mm")) + "  ·  " + summary;
	}

	@Override
	public Gravity getGravity() {
		return new Gravity(0, Position.TOP);
	}

	@Override
	public void onShown() {
		timeProperty.wake();
		dateProperty.wake();
	}

	@Override
	public void onHidden() {
		timeProperty.sleep();
		dateProperty.sleep();
	}

	@Override
	public void onSettingsChanged() {
		timeProperty.resetFormatter();
		timeProperty.refresh();
		dateProperty.resetFormatter();
		updateTimeVisibility();
	}

	@Override
	public Map<String, String> getSettingsContributions() {
		return Map.of(SETTING_DATEFORMAT, "EEEE, dd.MM.yyyy", SETTING_SHOW_SECONDS, "true", SETTING_12_HOUR, "false");
	}

}