package dev.tim9h.rcp.clock;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import dev.tim9h.rcp.settings.Settings;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public class DateProperty extends SimpleStringProperty {

	private DateTimeFormatter formatter;

	private ScheduledFuture<?> task;

	private boolean active;

	@Inject
	private Settings settings;

	private String getCurrentDateFormatted() {
		if (formatter == null) {
			formatter = DateTimeFormatter.ofPattern(settings.getString(ClockView.SETTING_DATEFORMAT));
		}
		return LocalDate.now(ZoneId.systemDefault()).format(formatter);
	}

	public synchronized void wake() {
		sleep();
		active = true;
		Platform.runLater(() -> set(getCurrentDateFormatted()));
		scheduleNextMidnight();
	}

	private synchronized void scheduleNextMidnight() {
		if (!active) {
			return;
		}
		var zone = ZoneId.systemDefault();
		var now = ZonedDateTime.now(zone);
		var nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
		var delayMillis = Duration.between(now, nextMidnight).toMillis() + 100;
		task = ClockScheduler.INSTANCE.schedule(() -> {
			Platform.runLater(() -> set(getCurrentDateFormatted()));
			scheduleNextMidnight();
		}, delayMillis, TimeUnit.MILLISECONDS);
	}

	public synchronized void sleep() {
		active = false;
		if (task != null) {
			task.cancel(false);
			task = null;
		}
	}

	public void resetFormatter() {
		formatter = null;
	}

}
