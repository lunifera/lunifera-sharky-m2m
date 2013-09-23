/**
 * Copyright 2013 Lunifera GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lunifera.m2m.standalone.sharky.webclient;

import org.lunifera.m2m.standalone.sharky.commander.api.ISharkyController;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
@Theme(Reindeer.THEME_NAME)
@Push
public class SimplePushUI extends UI implements
		ServiceTrackerCustomizer<ISharkyController, ISharkyController> {
	private AbsoluteLayout main;
	private Slider speed;
	private ISharkyController controller;
	private Table log;
	private BeanItemContainer<LogEntry> logContainer;
	private BeanItemContainer<RecordName> recordNamesContainer;
	private TextField alarmFence;
	private Recorder recorder;
	private Label c_pitch;
	private Label c_rotation;
	private Label c_speed;
	private Label copyrights;
	private Embedded saveRecorded;
	private ComboBox recordName;
	private Embedded resetRecorder;
	private Embedded playRecorder;

	@Override
	protected void init(VaadinRequest request) {

		recorder = new Recorder();

		main = new AbsoluteLayout();
		setContent(main);
		main.setSizeFull();
		setStyleName(Reindeer.LAYOUT_WHITE);
		main.setStyleName(Reindeer.LAYOUT_WHITE);

		createButtonPanel();

		AbsoluteLayout recordPanel = new AbsoluteLayout();
		recordPanel.setWidth("250px");
		recordPanel.setHeight("320px");

		saveRecorded = new Embedded("Save", new ThemeResource("record.png"));
		saveRecorded.setWidth("40px");
		saveRecorded.setEnabled(false);
		recordPanel.addComponent(saveRecorded, "top:40px;left:20px");
		saveRecorded.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				RecordName name = (RecordName) recordName.getValue();
				recorder.save(name.getName());

				Notification.show("Saved");
			}
		});

		resetRecorder = new Embedded("Reset", new ThemeResource(
				"resetrecorder.png"));
		resetRecorder.setWidth("40px");
		recordPanel.addComponent(resetRecorder, "top:40px;left:80px");
		resetRecorder.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				recorder.reset();
				logContainer.removeAllItems();
				stop();
			}
		});

		playRecorder = new Embedded("Play", new ThemeResource(
				"playrecorder.png"));
		playRecorder.setWidth("40px");
		playRecorder.setEnabled(false);
		recordPanel.addComponent(playRecorder, "top:40px;left:140px");
		playRecorder.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				try {
					Notification.show("Play");

					RecordName name = (RecordName) recordName.getValue();
					recorder.play(name.getName(), controller);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		recordName = new ComboBox();
		recordNamesContainer = new BeanItemContainer<RecordName>(
				RecordName.class);
		for(String name : recorder.getStored()){
			recordNamesContainer.addBean(new RecordName(name));
		}
		recordName.setContainerDataSource(recordNamesContainer);
		recordName.setItemCaptionPropertyId("name");
		recordName.setNewItemsAllowed(true);
		recordName.setImmediate(true);
		recordName.setFilteringMode(FilteringMode.OFF);
		recordName.setNewItemHandler(new AbstractSelect.NewItemHandler() {
			@Override
			public void addNewItem(String newItemCaption) {
				RecordName id = new RecordName(newItemCaption);
				recordNamesContainer.addBean(id);
				recordName.select(id);
			}
		});
		recordName.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue() == null) {
					saveRecorded.setEnabled(false);
					playRecorder.setEnabled(false);
				} else {
					saveRecorded.setEnabled(true);
					playRecorder.setEnabled(true);
				}
			}
		});
		recordName.setWidth("150px");
		recordPanel.addComponent(recordName, "top:100px;left:20px");
		main.addComponent(recordPanel, "top:100px;left:650px");

		alarmFence = new TextField("Alarm fence in cm");
		alarmFence.setValue("-1");
		main.addComponent(alarmFence, "top:500px;left:100px");
		alarmFence.setImmediate(true);
		alarmFence.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (controller != null)
					controller.sharkAlarmDistance(Integer
							.valueOf((String) event.getProperty().getValue()),
							recorder);
			}
		});

		createLogTable();

		copyrights = new Label("Icons by: http://taytel.deviantart.com/");
		main.addComponent(copyrights, "bottom:50px;left:100px");

		ServiceTracker<ISharkyController, ISharkyController> tracker = new ServiceTracker<>(
				Activator.getContext(), ISharkyController.class, this);
		tracker.open();

	}

	private void createLogTable() {
		log = new Table("Event-Log");
		log.setHeight("260px");
		log.setWidth("500px");
		logContainer = new BeanItemContainer<LogEntry>(LogEntry.class);
		log.setContainerDataSource(logContainer);
		log.addItemSetChangeListener(new Container.ItemSetChangeListener() {
			@Override
			public void containerItemSetChange(
					Container.ItemSetChangeEvent event) {
				log.setCurrentPageFirstItemIndex(log.size() < 11 ? 1 : log
						.size() - 10);
			}
		});
		main.addComponent(log, "top:550px;left:100px");
	}

	private void createButtonPanel() {
		AbsoluteLayout buttonPanel = new AbsoluteLayout();
		buttonPanel.setWidth("500px");
		buttonPanel.setHeight("320px");

		final Embedded left = new Embedded("Left",
				new ThemeResource("left.png"));
		final Embedded top = new Embedded("Dive", new ThemeResource("dive.png"));
		final Embedded right = new Embedded("Right", new ThemeResource(
				"right.png"));
		final Embedded bottom = new Embedded("Arise", new ThemeResource(
				"arise.png"));
		final Embedded stop = new Embedded("Stop",
				new ThemeResource("stop.png"));

		left.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				left();
			}
		});

		right.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				right();
			}
		});

		top.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				top();
			}
		});

		bottom.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				bottom();
			}
		});

		stop.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				stop();
			}
		});

		speed = new Slider(0, 5);
		speed.setOrientation(SliderOrientation.VERTICAL);

		registerShortcuts();

		left.setWidth("75px");
		top.setWidth("75px");
		right.setWidth("75px");
		bottom.setWidth("75px");
		stop.setWidth("75px");

		speed.setHeight("220px");
		speed.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				foreward();
			}
		});

		buttonPanel.addComponent(left, "top:130px;left:0px");
		buttonPanel.addComponent(top, "top:40px;left:100px");
		buttonPanel.addComponent(right, "top:130px;left:200px");
		buttonPanel.addComponent(bottom, "top:230px;left:100px");
		buttonPanel.addComponent(stop, "top:130px;left:100px");
		buttonPanel.addComponent(speed, "top:60px;left:310px");

		// add the labels to show the current values
		c_pitch = new Label("pitch: 0");
		c_pitch.setStyleName(Reindeer.LABEL_H2);
		c_rotation = new Label("rotation: 0");
		c_rotation.setStyleName(Reindeer.LABEL_H2);
		c_speed = new Label("speed: 0");
		c_speed.setStyleName(Reindeer.LABEL_H2);
		buttonPanel.addComponent(c_pitch, "top:80px;right:60px");
		buttonPanel.addComponent(c_rotation, "top:120px;right:60px");
		buttonPanel.addComponent(c_speed, "top:160px;right:60px");

		main.addComponent(buttonPanel, "top:100px;left:100px");
	}

	private void registerShortcuts() {
		main.addShortcutListener(new ShortcutListener("Left",
				ShortcutAction.KeyCode.ARROW_LEFT, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				left();
			}
		});

		main.addShortcutListener(new ShortcutListener("Top",
				ShortcutAction.KeyCode.W, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				top();
			}
		});

		main.addShortcutListener(new ShortcutListener("Right",
				ShortcutAction.KeyCode.ARROW_RIGHT, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				right();
			}
		});

		main.addShortcutListener(new ShortcutListener("Bottom",
				ShortcutAction.KeyCode.S, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				bottom();
			}
		});

		main.addShortcutListener(new ShortcutListener("STOP",
				ShortcutAction.KeyCode.ESCAPE, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				stop();
			}
		});

		main.addShortcutListener(new ShortcutListener("Speed up",
				ShortcutAction.KeyCode.ARROW_UP, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				if (speed.getValue() < 5)
					speed.setValue(speed.getValue().doubleValue() + 1);
			}
		});

		main.addShortcutListener(new ShortcutListener("Speed down",
				ShortcutAction.KeyCode.ARROW_DOWN, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				if (speed.getValue() > 0)
					speed.setValue(speed.getValue().doubleValue() - 1);
			}
		});
	}

	private void left() {
		if (controller != null) {
			int value = controller.rotation(-1, recorder);
			refreshRotation(value);
		}
	}

	private void top() {
		if (controller != null) {
			int value = controller.pitch(-1, recorder);
			refreshPitch(value);
		}
	}

	private void right() {
		if (controller != null) {
			int value = controller.rotation(+1, recorder);
			refreshRotation(value);
		}
	}

	private void bottom() {
		if (controller != null) {
			int value = controller.pitch(+1, recorder);
			refreshPitch(value);
		}
	}

	private void refreshPitch(int value) {
		c_pitch.setValue(value < 0 ? "dive: " + value * -1 : "arise: " + value);
	}

	private void refreshRotation(int value) {
		c_rotation.setValue(value < 0 ? "left: " + value * -1 : "right: "
				+ value);
	}

	private void refreshSpeed(int value) {
		c_speed.setValue("speed: " + value);

		// reset rotation if speed is 0
		if (value == 0) {
			refreshRotation(0);
		}
	}

	private void stop() {
		if (controller != null)
			controller.stop(recorder);

		speed.setValue(0.0);
		refreshSpeed(0);
		refreshPitch(0);
		refreshRotation(0);
	}

	private void foreward() {
		if (controller != null) {
			int value = controller.speed(speed.getValue().intValue(), recorder);
			refreshSpeed(value);
		}
	}

	@Override
	public ISharkyController addingService(
			ServiceReference<ISharkyController> reference) {
		controller = Activator.getContext().getService(reference);
		return controller;
	}

	@Override
	public void modifiedService(ServiceReference<ISharkyController> reference,
			ISharkyController service) {
	}

	@Override
	public void removedService(ServiceReference<ISharkyController> reference,
			ISharkyController service) {
		if (service == controller) {
			controller = null;
		}
	}

	public static class LogEntry {
		private String message;

		public LogEntry(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	public static class RecordName {
		private String name;

		public RecordName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	private class Recorder extends ISharkyController.CommandRecorder {

		@Override
		public void record(String topic, final String command, boolean retain) {
			super.record(topic, command, retain);
			UI.getCurrent().accessSynchronously(new Runnable() {
				@Override
				public void run() {
					// add sent command to the log table
					logContainer.addBean(new LogEntry(command));
				}
			});
			
		}
	}
}
