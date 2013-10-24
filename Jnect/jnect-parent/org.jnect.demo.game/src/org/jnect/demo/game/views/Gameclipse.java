/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugen Neufeld - initial API and implementation
 *******************************************************************************/
package org.jnect.demo.game.views;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.jnect.core.KinectManager;
import org.jnect.core.SpeechListener;
import org.jnect.demo.game.states.FifthState;
import org.jnect.demo.game.states.FinalState;
import org.jnect.demo.game.states.FourthState;
import org.jnect.demo.game.states.GameState;
import org.jnect.demo.game.states.IntroState;
import org.jnect.demo.game.states.SecondState;
import org.jnect.demo.game.states.SeventhState;
import org.jnect.demo.game.states.SixthState;
import org.jnect.demo.game.states.ThirdState;
import org.jnect.demo.game.timer.GameTimer;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.GestureListener;
import org.jnect.gesture.GestureProxy;
import org.jnect.gesture.detectors.CrouchGestureDetector;
import org.jnect.gesture.detectors.JumpGestureDetector;


/**
 * The Kinect eclipse game.
 * 
 * @author Deepak Srinathan
 * 
 */
public class Gameclipse extends ViewPart implements SelectionListener {

	public static final String ID = "org.jnect.demo.game.views.Gameclipse";

	// the list of game states. assumption is that the game state transition is
	// linear (one after the other)
	private List<GameState> gameStates = new LinkedList<GameState>();

	// the label resource which gets changed by every state
	private Label label = null;

	// the timer lable
	private Label label_time = null;

	// game start/stop/reset button
	private Button buttonStart = null;

	// variable to remember the current state
	private int index = 0;

	// flag to check if the game is currently running or not
	private boolean gameState = false;

	// this is to make the focus working
	private Composite thisComp = null;

	// holds the value of the current game state
	private GameState currentState = null;

	// the game timer
	private GameTimer gameTimer = null;

	// isKinectConnectedFlag
	private boolean isKinectConnected = false;

	// the gesture listener
	private GestureListener gestureListener = null;

	// the speech listener
	private SpeechListener speechListener = null;

	// String for speech recognition
	private static final String DEBUG_START = "Start debug mode";
	private static final String FIX_BUG = "Fix bug";

	/**
	 * Initialize the states ande its members and the state transitions
	 */
	private void initStates() {
		IntroState introState = new IntroState();
		SecondState secState = new SecondState();
		ThirdState thState = new ThirdState();
		FourthState fourthState = new FourthState();
		FifthState fifthState = new FifthState();
		SixthState sixthState = new SixthState();
		SeventhState seventhState = new SeventhState();
		FinalState finalState = new FinalState();

		gameStates.add(introState);
		gameStates.add(secState);
		gameStates.add(thState);
		gameStates.add(fourthState);
		gameStates.add(fifthState);
		gameStates.add(sixthState);
		gameStates.add(seventhState);
		gameStates.add(finalState);

	}

	/**
	 * Method to create the view
	 */
	@Override
	public void createPartControl(Composite parent) {
		thisComp = parent;

		// initialize the game states
		initStates();

		// Game not running
		gameState = false;

		// Define GridLayout
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginTop = 10;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		parent.setLayout(layout);

		// Add actions label
		label = new Label(parent, SWT.NONE);
		label.setFont(new Font(parent.getDisplay(), "Arial", 28, SWT.BOLD));
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		label.setLayoutData(gridData);

		// Add start/stop/reset button
		buttonStart = new Button(parent, SWT.None);
		buttonStart.setText("Start Game!");
		buttonStart
				.setFont(new Font(parent.getDisplay(), "Arial", 20, SWT.BOLD));

		// Add a label that shows the time
		label_time = new Label(parent, SWT.None);
		label_time
				.setFont(new Font(parent.getDisplay(), "Arial", 20, SWT.BOLD));
		label_time.setLayoutData(gridData);
		label_time.setText("0");
		label_time.setAlignment(SWT.CENTER);

		// Initialize first gameState
		currentState = gameStates.get(index);
		// currentState.paintScreen(label);
		label.setText("Click on Start Game to start...");

		buttonStart.addSelectionListener(this);

		gameTimer = new GameTimer(thisComp.getDisplay(), label_time);
		parent.setFocus();
	}

	/**
	 * Set focus to set the focus to the component inside the view
	 */
	@Override
	public void setFocus() {
		thisComp.setFocus();
	}

	/**
	 * Game start method, which also initializes the Kinect Gesture recognition
	 */
	private void start() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				currentState.paintScreen(label);
				gameTimer.startTimer();
			}
		});
	}

	/**
	 * Game stop method
	 */
	private void stop() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				KinectManager.INSTANCE.startSkeletonTracking();
				KinectManager.INSTANCE.startSpeechRecognition();
				// isCalibrated = false;
				gameState = false;
				gameTimer.stopTimer();
				label.setText("And your're done.. Good Job !!");
				buttonStart.setText("Reset Game");
			}
		});
	}

	/**
	 * Game reset method
	 */
	private void reset() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				KinectManager.INSTANCE.startSkeletonTracking();
				KinectManager.INSTANCE.startSpeechRecognition();
				gameTimer.reset();
				// isCalibrated = false;
				index = 0;
				currentState = gameStates.get(index);
				// currentState.paintScreen(label);
				label.setText("Click on Start Game to start...");
				buttonStart.setText("Start Game");
			}
		});
	}

	/**
	 * Action for Game button
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		// Button has been clicked, what's the current action?
		if (gameState) {
			// Stop
			stop();
		} else if (!gameState) {
			if (gameTimer.getTime() != 0) {
				// Reset
				reset();
			} else {
				// initialize the kinect
				intiKinect();
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						buttonStart.setText("Stop Game");
						gameState = true;
						setFocus();
						start();
						currentState.paintScreen(label);
					}
				});
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	/**
	 * Perform the operation on the current state
	 */
	private void performStateOperation() {
		if (gameState) {
			// perform action on the current state
			currentState.performAction();
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// paint the next state (and update the current state)
			currentState = gameStates.get(++index);
			currentState.paintScreen(label);

			if (index == gameStates.size() - 1) {
				index = 0;
				stop();
			}
		}
	}

	/**
	 * Initialize the connection to kinect and start the gesture and speech
	 * recognition modules
	 */
	private void intiKinect() {
		if (!isKinectConnected) {

			gestureListener = new GestureListener() {
				@Override
				public void notifyGestureDetected(
						Class<? extends Gesture> gesture) {
					if (currentState.isGestureEnabled()
							&& currentState.getRequiredGesture()
									.equals(gesture)) {
						performStateOperation();
					}
				}
			};

			speechListener = new SpeechListener() {

				@Override
				public void notifySpeech(String speech) {
					if (currentState.isSpeechEnabled()
							&& currentState.getRequiredSpeechString()
									.equalsIgnoreCase(speech)) {
						performStateOperation();
					}
				}

				@Override
				public Set<String> getWords() {
					Set<String> words = new HashSet<String>();
					words.add(DEBUG_START);
					words.add(FIX_BUG);
					words.add("bo");
					return words;
				}

			};

			KinectManager.INSTANCE.startKinect();
			KinectManager.INSTANCE.addSpeechListener(speechListener);
			GestureProxy.INSTANCE.addGestureListener(gestureListener);
			KinectManager.INSTANCE.startSkeletonTracking();
			KinectManager.INSTANCE.startSpeechRecognition();
			GestureProxy.INSTANCE.addGestureDetector(new JumpGestureDetector());
			GestureProxy.INSTANCE
					.addGestureDetector(new CrouchGestureDetector());
			isKinectConnected = true;
		}
	}
}