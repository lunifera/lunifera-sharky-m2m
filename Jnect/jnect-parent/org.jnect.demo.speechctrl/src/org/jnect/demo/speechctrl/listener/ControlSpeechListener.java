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
package org.jnect.demo.speechctrl.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.jnect.core.SpeechListener;



public class ControlSpeechListener extends SpeechListener {
	
	private static final Map<String, Command> speechCommandMap = new HashMap<String, Command>();
	
	private static final Set<String> speechCommands = new HashSet<String>();
	private static final Set<String> unexecutableCommands = new HashSet<String>();
	
	private static ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
	private static IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
	
	private static final Command[] commands = commandService.getDefinedCommands();
	
	static {		
		String cmdName;
		for (Command c : commands){
			try {
				// Erase all dots 
				cmdName = trimCommandName(c.getName());
				
				speechCommandMap.put(cmdName, c);
				speechCommands.add(cmdName);
				
			} catch (NotDefinedException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
	
	private static String trimCommandName(String name){ 
		name.replaceAll("\\.", "");
		name.trim();
		
		return name;
	}
	
	@Override
	public Set<String> getWords() {
		return speechCommands;
	}

	@Override
	public void notifySpeech(String speech) {
		System.out.println("Speech recongized: " + speech);
		
		if (speechCommandMap.containsKey(speech)){
			final String speechCmd = speech;
			
//			if (unexecutableCommands.contains(speechCmd)){
//				System.out.println("Command \"" + speechCmd + "\" can not be executed via speech recognition.");
//				return;
//			}
			
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					Command cmd = speechCommandMap.get(speechCmd);
					try{
						ParameterizedCommand paramCommand = new ParameterizedCommand(cmd, null);
						handlerService.executeCommand(paramCommand, null);
					}catch(Exception e){
						unexecutableCommands.add(speechCmd);
						
						System.out.println("Command \"" + speechCmd + "\" could not be executed via speech recognition.");
					}
				}
			});
			
		}		
		
	}	
}
