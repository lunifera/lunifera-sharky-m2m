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
package org.jnect.demo.debug.listener;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.jnect.core.KinectManager;
import org.jnect.core.SpeechListener;
import org.jnect.demo.debug.DebugHelper;


public class DebugSpeechListener extends SpeechListener {

	private static final String HELLO = "Hello Eclipse";
	private static final String DEBUG_START = "Start debug mode";
	private static final String STEP_OVER = "Step over";
	private static final String STEP_INTO = "Step into";
	private static final String STEP_RETURN = "Step return";
	private static final String RESUME = "Resume";
	private static final String JAVA_PERSPECTIVE = "Open Java perspective";
	private static final String EXIT = "Bye Bye Eclipse";
	private static final String STOP_SPEECH_RECOGNITION = "Stop speech recognition";
	private static final String OPEN_TYPE = "Open Type";
	
	private static final Set<String> words = new HashSet<String>();
	
	private static boolean isOpenTypeDialogOpen = false;
	
	static {
		words.add(DEBUG_START);
		words.add(EXIT);
		words.add(HELLO);
		words.add(STEP_OVER);
		words.add(STEP_INTO);
		words.add(STEP_RETURN);
		words.add(RESUME);
		words.add(JAVA_PERSPECTIVE);
//		words.add(STOP_SPEECH_RECOGNITION);
		words.add(OPEN_TYPE);
	}
	
	@Override
	public Set<String> getWords() {
		return words;
	}

	@Override
	public void notifySpeech(String speech) {
		System.out.println("Speech recongized: " + speech);
		
		if (HELLO.equalsIgnoreCase(speech)) {
			showText("Welcome back. What would you like to do?");
		} else if (DEBUG_START.equalsIgnoreCase(speech)) {
			DebugHelper.runLastDebug();
		} else if (STEP_OVER.equalsIgnoreCase(speech)) {
			DebugHelper.stepOver();
		} else if (STEP_INTO.equalsIgnoreCase(speech)) {
			DebugHelper.stepInto();
		} else if (STEP_RETURN.equalsIgnoreCase(speech)) {
			DebugHelper.stepReturn();
		} else if (RESUME.equalsIgnoreCase(speech)) {
			DebugHelper.resume();
		} else if (JAVA_PERSPECTIVE.equalsIgnoreCase(speech)) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
					Command showView = commandService.getCommand("org.eclipse.ui.perspectives.showPerspective");
					try {
						IParameter persIdParm = showView.getParameter("org.eclipse.ui.perspectives.showPerspective.perspectiveId");

						Parameterization parm = new Parameterization(persIdParm, "org.eclipse.jdt.ui.JavaPerspective");
						ParameterizedCommand parmCommand = new ParameterizedCommand(showView, new Parameterization[] { parm });

						handlerService.executeCommand(parmCommand, null);
					} catch (Exception e) {
						
					}
				}
			});
		} else if (EXIT.equalsIgnoreCase(speech)) {
			showText("See you soon!");
		} else if (STOP_SPEECH_RECOGNITION.equalsIgnoreCase(speech)) {
			KinectManager.INSTANCE.stopSpeechRecognition();
		} else if (OPEN_TYPE.equalsIgnoreCase(speech)) {
			openTypeDialog();
		}
		
	}

	private void showText(final String text) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Shell shell = new Shell(Display.getDefault());
				Label label = new Label(shell, SWT.BORDER);
				Menu menu = new Menu(shell, SWT.POP_UP);
				label.setText(text);
				label.setBounds(0, 0, 300, 25);
				menu.setVisible(true);
				shell.setMenu(menu);
				shell.setSize(300, 70);
				shell.open();
			}
		});
	}
	
	private void openTypeDialog() {
		if (!isOpenTypeDialogOpen) {
			isOpenTypeDialogOpen = true;
			Display.getDefault().syncExec(new Runnable() {
				@SuppressWarnings("restriction")
				public void run() {
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					OpenTypeSelectionDialog dialog = new OpenTypeSelectionDialog(win.getShell(), true, PlatformUI.getWorkbench().getProgressService(), null, IJavaSearchConstants.TYPE);
					dialog.setTitle(JavaUIMessages.OpenTypeAction_dialogTitle);
					dialog.setMessage(JavaUIMessages.OpenTypeAction_dialogMessage);
					dialog.setBlockOnOpen(false);
					dialog.open();
					isOpenTypeDialogOpen = false;
				}
			});
		}
	}
}
