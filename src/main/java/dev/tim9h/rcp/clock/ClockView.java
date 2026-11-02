package dev.tim9h.rcp.clock;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

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

public class ClockView implements Plugin {

	static final String SETTING_DATEFORMAT = "clock.dateformat.right";

	static final String SETTING_TIMEFORMAT = "clock.dateformat.left";

	@InjectLogger
	private Logger logger;

	@Inject
	private TimeProperty timeProperty;

	@Inject
	private DateProperty dateProperty;

	private StringProperty nextEventProperty;

	private boolean hasNextEvent;

	private Label lblNext = new Label("📆 NEXT ");

	private Label lblNextEvent = new Label();

	private Label lblNoEvent = new Label("📆 No upcoming events");

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

		lblNext.getStyleClass().addAll("accent-label", "detail-label");
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
		lblNoEvent.setAlignment(Pos.CENTER);
		lblNoEvent.setMaxWidth(Double.MAX_VALUE);
		lblNoEvent.setTextOverrun(OverrunStyle.ELLIPSIS);
		lblNoEvent.setVisible(!hasNextEvent);
		lblNoEvent.setManaged(!hasNextEvent);
		var eventContent = new StackPane(lblNoEvent, eventGroup);
		StackPane.setAlignment(lblNoEvent, Pos.CENTER);
		StackPane.setAlignment(eventGroup, Pos.CENTER);
		HBox.setHgrow(eventContent, Priority.ALWAYS);

		var lblDate = new Label();
		lblDate.getStyleClass().add("detail-label");
		lblDate.textProperty().bind(dateProperty);
		lblDate.setMinWidth(Region.USE_PREF_SIZE);
		lblDate.setAlignment(Pos.CENTER_RIGHT);

		hbox.getChildren().addAll(lblTime, eventContent, lblDate);

		return Optional.of(hbox);
	}

	@Override
	public void initBus(EventManager eventManager) {
		Plugin.super.initBus(eventManager);
		eventManager.listen(Event.EVENT_CALENDAR_NEXT_EVENT, event -> {
			hasNextEvent = event != null;
			updateEventVisibility();
			nextEventProperty.set(formatNextEvent(event));
		});
	}

	private void updateEventVisibility() {
		lblNext.setVisible(hasNextEvent);
		lblNext.setManaged(hasNextEvent);
		lblNextEvent.setVisible(hasNextEvent);
		lblNextEvent.setManaged(hasNextEvent);
		lblNoEvent.setVisible(!hasNextEvent);
		lblNoEvent.setManaged(!hasNextEvent);
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
		dateProperty.resetFormatter();
	}

	@Override
	public Map<String, String> getSettingsContributions() {
		return Map.of(SETTING_DATEFORMAT, "EEEE, dd.MM.yyyy", SETTING_TIMEFORMAT, "HH:mm:ss");
	}

}