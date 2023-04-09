package dev.tim9h.rcp.clock;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.spi.CCard;
import dev.tim9h.rcp.spi.Gravity;
import dev.tim9h.rcp.spi.Position;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ClockView implements CCard {

	@InjectLogger
	private Logger logger;

	@Inject
	private TimeProperty timeProperty;

	@Inject
	private DateProperty dateProperty;

	@Override
	public String getName() {
		return "Clock";
	}

	@Override
	public Optional<Node> getNode() throws IOException {
		var lblTime = new Label();
		lblTime.getStyleClass().add("accent-label");
		lblTime.textProperty().bind(timeProperty);

		var spacer = new Region();

		var lblDate = new Label();
		lblDate.textProperty().bind(dateProperty);

		var hbox = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getStyleClass().add("ccCard");
		hbox.getChildren().addAll(lblTime, spacer, lblDate);

		return Optional.of(hbox);
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

}
