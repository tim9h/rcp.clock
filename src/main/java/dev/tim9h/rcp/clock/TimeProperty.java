package dev.tim9h.rcp.clock;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import dev.tim9h.rcp.settings.Settings;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public class TimeProperty extends SimpleStringProperty {

	private DateTimeFormatter formatter;

	private Timer timer;

	@Inject
	private Settings settings;

	private String getCurrentTimeFormatted() {
		if (formatter == null) {
			formatter = DateTimeFormatter.ofPattern(settings.getString(ClockViewFactory.SETTING_TIMEFORMAT));
		}
		return LocalTime.now().format(formatter);
	}

	public void wake() {
		timer = new Timer("clockUpdater");
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				Platform.runLater(() -> TimeProperty.this.set(getCurrentTimeFormatted()));
			}
		}, 0, TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));
	}

	public void sleep() {
		if (timer != null) {
			timer.cancel();
		}
	}

	public void resetFormatter() {
		formatter = null;
	}

}
