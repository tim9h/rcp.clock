package dev.tim9h.rcp.clock;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import dev.tim9h.rcp.settings.Settings;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TimeProperty extends SimpleStringProperty {

	private static final DateTimeFormatter SECONDS_FORMATTER = DateTimeFormatter.ofPattern(":ss");

	private DateTimeFormatter formatter;

	private final StringProperty secondsProperty = new SimpleStringProperty();

	private ScheduledFuture<?> task;

	@Inject
	private Settings settings;

	private String getCurrentTimeFormatted() {
		if (formatter == null) {
			var pattern = Boolean.TRUE.equals(settings.getBoolean(ClockView.SETTING_12_HOUR)) ? "hh:mm" : "HH:mm";
			formatter = DateTimeFormatter.ofPattern(pattern);
		}
		var time = LocalTime.now(ZoneId.systemDefault());
		secondsProperty.set(time.format(SECONDS_FORMATTER));
		return time.format(formatter);
	}

	public StringProperty getSecondsProperty() {
		return secondsProperty;
	}

	public boolean isShowSeconds() {
		return Boolean.TRUE.equals(settings.getBoolean(ClockView.SETTING_SHOW_SECONDS));
	}

	public void refresh() {
		Platform.runLater(() -> set(getCurrentTimeFormatted()));
	}

	public synchronized void wake() {
		sleep();
		task = ClockScheduler.INSTANCE.scheduleAtFixedRate(this::refresh, 0, 1, TimeUnit.SECONDS);
	}

	public synchronized void sleep() {
		if (task != null) {
			task.cancel(false);
			task = null;
		}
	}

	public void resetFormatter() {
		formatter = null;
	}

}
