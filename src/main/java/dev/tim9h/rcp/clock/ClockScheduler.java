package dev.tim9h.rcp.clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

final class ClockScheduler {

	static final ScheduledExecutorService INSTANCE = Executors.newSingleThreadScheduledExecutor(runnable -> {
		var thread = new Thread(runnable, "clock-scheduler");
		thread.setDaemon(true);
		return thread;
	});

	private ClockScheduler() {
		// utility class
	}

}
