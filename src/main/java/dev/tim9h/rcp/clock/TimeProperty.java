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

public class TimeProperty extends SimpleStringProperty {

	private DateTimeFormatter formatter;

	private ScheduledFuture<?> task;

	@Inject
	private Settings settings;

	private String getCurrentTimeFormatted() {
		if (formatter == null) {
			formatter = DateTimeFormatter.ofPattern(settings.getString(ClockView.SETTING_TIMEFORMAT));
		}
		return LocalTime.now(ZoneId.systemDefault()).format(formatter);
	}

	public synchronized void wake() {
		sleep();
		task = ClockScheduler.INSTANCE.scheduleAtFixedRate(
				() -> Platform.runLater(() -> set(getCurrentTimeFormatted())), 0, 1, TimeUnit.SECONDS);
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
