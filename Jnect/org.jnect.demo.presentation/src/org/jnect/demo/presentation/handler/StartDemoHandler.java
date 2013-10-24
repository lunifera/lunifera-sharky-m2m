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
package org.jnect.demo.presentation.handler;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.program.Program;
import org.jnect.core.KinectManager;
import org.jnect.core.SpeechListener;



public class StartDemoHandler extends AbstractHandler {

	private static String START_DEMO="Start Presentation";
	private static String FILE_PATH="c:/jnect.pdf";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		KinectManager kinectManager=KinectManager.INSTANCE;
		kinectManager.startKinect();
		kinectManager.addSpeechListener(new SpeechListener() {
			
			@Override
			public void notifySpeech(String speech) {
				if(START_DEMO.equalsIgnoreCase(speech)){
					Program.launch(FILE_PATH);
				}
			}
			
			@Override
			public Set<String> getWords() {
				Set<String> words=new HashSet<String>();
				words.add(START_DEMO);
				
				return words;
			}
		});
		kinectManager.startSpeechRecognition();
		return null;
	}

	

}
