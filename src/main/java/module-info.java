module rcp.clock {
	exports dev.tim9h.rcp.clock;

	requires transitive rcp.api;
	requires com.google.guice;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires org.apache.logging.log4j;
}