package dev.tim9h.rcp.clock;

import java.util.Map;

import com.google.inject.Inject;

import dev.tim9h.rcp.spi.CCard;
import dev.tim9h.rcp.spi.CCardFactory;

public class ClockViewFactory implements CCardFactory {

	static final String SETTING_DATEFORMAT = "clock.dateformat.right";

	static final String SETTING_TIMEFORMAT = "clock.dateformat.left";

	@Inject
	private ClockView clockView;

	@Override
	public String getId() {
		return "clock";
	}

	@Override
	public CCard createCCard() {
		return clockView;
	}

	@Override
	public Map<String, String> getSettingsContributions() {
		return Map.of(SETTING_DATEFORMAT, "EEEE, dd.MM.yyyy", SETTING_TIMEFORMAT, "HH:mm:ss");
	}

}
