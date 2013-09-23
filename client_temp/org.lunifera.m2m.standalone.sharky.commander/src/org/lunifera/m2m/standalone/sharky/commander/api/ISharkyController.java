package org.lunifera.m2m.standalone.sharky.commander.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Controls the sharky. The values are being passed to sharky using m2m.
 */
public interface ISharkyController {

	public static final String COMMANDS_TOPIC = "sharky_commands";
	public static final String SENSORS_TOPIC = "sharky_sensors";
	public static final String ALARM_TOPIC = "sharky_alarmfence";

	/**
	 * A value from -5 to +5. "-" means down and "+" means up.
	 * 
	 * @param value
	 * @param recorder
	 * @return the current value
	 */
	int pitch(int value, CommandRecorder recorder);

	/**
	 * A value from -5 to +5. "-" means left and "+" means right.
	 * 
	 * @param value
	 * @param recorder
	 * @return the current value
	 */
	int rotation(int value, CommandRecorder recorder);

	/**
	 * A value from 0 to +5 defining the speed.
	 * 
	 * @param value
	 * @param recorder
	 * @return the current value
	 */
	int speed(int value, CommandRecorder recorder);

	/**
	 * Stop will reset all sent parameters.
	 * 
	 * @param recorder
	 * @return the current value
	 */
	void stop(CommandRecorder recorder);

	/**
	 * Sends the shark alarm distance to the m2m server.
	 * 
	 * @param valueOf
	 * @param recorder
	 */
	void sharkAlarmDistance(Integer valueOf, CommandRecorder recorder);

	/**
	 * Sends the given command.
	 * 
	 * @param topic
	 * @param command
	 * @param retain
	 * @param recorder
	 */
	void sendCommand(String topic, String command, boolean retain,
			CommandRecorder recorder) throws Exception;

	/**
	 * Is used to record commands
	 */
	public static class CommandRecorder {

		private Date lastTime;
		private StringBuilder builder = new StringBuilder();

		/**
		 * Records the given command.
		 * 
		 * @param topic
		 * @param command
		 * @param retain
		 */
		public void record(String topic, String command, boolean retain) {

			if (lastTime == null) {
				lastTime = new Date();
			}

			builder.append(command);
			builder.append(",");
			builder.append(topic);
			builder.append(",");
			builder.append(Boolean.toString(retain));
			builder.append(",");
			Date current = new Date();
			builder.append(Long.toString(current.getTime() - lastTime.getTime()));
			lastTime = current;
			builder.append(";");
		}

		public List<String> getStored() {
			List<String> names = new ArrayList<String>();
			Bundle bundle = FrameworkUtil.getBundle(CommandRecorder.this
					.getClass());
			File file = bundle.getBundleContext().getDataFile("Dummy.sharky");
			File parent = file.getParentFile();
			for (File sharkyDef : parent.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".sharky");
				}
			})) {
				names.add(sharkyDef.getName());
			}

			return names;
		}

		/**
		 * Plays the stored commands.
		 * 
		 * @param name
		 * @throws Exception
		 */
		public void play(final String name, final ISharkyController controller)
				throws Exception {

			new Thread(new Runnable() {
				@Override
				public void run() {
					StringBuilder builder = new StringBuilder();
					Bundle bundle = FrameworkUtil
							.getBundle(CommandRecorder.this.getClass());

					String fileName = !name.endsWith(".sharky") ? name
							+ ".sharky" : name;
					File file = bundle.getBundleContext().getDataFile(fileName);
					try {
						BufferedReader reader = new BufferedReader(
								new FileReader(file));
						while (reader.ready()) {
							builder.append(reader.readLine());
						}
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					String[] commands = builder.toString().split(";");
					for (String command : commands) {
						String[] tokens = command.split(",");
						try {
							controller.sendCommand(tokens[1], tokens[0],
									Boolean.parseBoolean(tokens[2]),
									CommandRecorder.this);
							// suspend the thread for the delta time before the
							// next
							// command
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(Long.parseLong(tokens[3]));
						} catch (NumberFormatException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}

		/**
		 * Saves the recorded commands under the given name.
		 * 
		 * @param name
		 */
		public void save(String name) {
			Bundle bundle = FrameworkUtil.getBundle(this.getClass());
			String fileName = !name.endsWith(".sharky") ? name + ".sharky"
					: name;
			File file = bundle.getBundleContext().getDataFile(fileName);
			if (file.exists()) {
				file.delete();
			}
			try {
				file.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(builder.toString());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void reset() {
			lastTime = null;
			builder = new StringBuilder();
		}
	}
}
