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
package org.jnect.core.m2m;

public class M2MProtocolConstants {

	public static final String DEFAULT_HOST = "tcp://192.168.178.40:1883";
	
	public static final String SPEECH = "RECOGNIZED: ";
	public static final String SKELETON = "SKELETON: ";

	public static final String SKELETON_TOPIC = "skeleton";
	public static final String SPEECH_TOPIC = "speech";
	
	private M2MProtocolConstants() {
		// Should not be instantiated
	}
}
