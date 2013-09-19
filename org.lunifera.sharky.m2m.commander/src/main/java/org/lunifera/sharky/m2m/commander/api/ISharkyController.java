package org.lunifera.sharky.m2m.commander.api;

/**
 * Controls the sharky. The values are being passed to sharky using m2m.
 */
public interface ISharkyController {
	
	public static final String TOPIC = "sharky_commands";

	/**
	 * A value from -5 to +5. "-" means down and "+" means up.
	 * @param value
	 * @return
	 */
	String pitch(int value);

	/**
	 * A value from -5 to +5. "-" means left and "+" means right.
	 * @param value
	 * @return
	 */
	String rotation(int value);
	
	/**
	 * A value from 0 to +5 defining the speed.
	 * @param value
	 * @return
	 */
	String speed(int value);
	
	/**
	 * Stop will reset all sent parameters.
	 * @return
	 */
	String stop();
	
}
