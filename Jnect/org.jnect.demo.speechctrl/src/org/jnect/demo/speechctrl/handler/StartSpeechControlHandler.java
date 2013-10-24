/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Bayha - initial API and implementation
 *******************************************************************************/
package org.jnect.demo.speechctrl.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.jnect.core.KinectManager;
import org.jnect.demo.speechctrl.listener.ControlSpeechListener;


public class StartSpeechControlHandler extends AbstractHandler {

	private static boolean addedListener = false;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!addedListener) {
			KinectManager.INSTANCE.addSpeechListener(new ControlSpeechListener());
			addedListener = true;
		}
		KinectManager.INSTANCE.startSpeechRecognition();
		return null;
	}

}
