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
package org.jnect.demo.game.states;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.jnect.demo.game.GameHelper;
import org.jnect.gesture.Gesture;


public class SeventhState implements GameState {

	@Override
	public Class<? extends Gesture> getRequiredGesture() {
		return null;
	}

	@Override
	public void performAction() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				GameHelper.runCommand("org.eclipse.debug.ui.commands.Resume");
			}
		});
		showText("Hoooray !! Eclipse has solved the bug!!");
	}

	@Override
	public void paintScreen(final Label label) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				label.setText("Say \"Fix bug\" to fix the bug..");
			}
		});
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

	@Override
	public String getRequiredSpeechString() {
		return "Fix Bug";
	}

	@Override
	public boolean isGestureEnabled() {
		return false;
	}

	@Override
	public boolean isSpeechEnabled() {
		return true;
	}

}
