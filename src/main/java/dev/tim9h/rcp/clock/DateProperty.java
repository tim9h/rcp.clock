package dev.tim9h.rcp.clock;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import dev.tim9h.rcp.settings.Settings;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public class DateProperty extends SimpleStringProperty {

	private DateTimeFormatter formatter;

	private Calendar midnight;

	private Timer timer;

	@Inject
	private Settings settings;

	public DateProperty() {
		midnight = Calendar.getInstance();
		midnight.set(Calendar.HOUR_OF_DAY, 0);
		midnight.set(Calendar.MINUTE, 0);
		midnight.set(Calendar.SECOND, 0);
	}

	private String getCurrentDateFormatted() {
		if (formatter == null) {
			formatter = DateTimeFormatter.ofPattern(settings.getString(ClockViewFactory.SETTING_DATEFORMAT));
		}
		return LocalDate.now().format(formatter);
	}

	public void wake() {
		timer = new Timer("dateUpdater");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(() -> DateProperty.this.set(getCurrentDateFormatted()));
			}
		}, midnight.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
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
