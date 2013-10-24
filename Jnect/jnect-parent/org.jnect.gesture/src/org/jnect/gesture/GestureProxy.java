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
package org.jnect.gesture;

import org.jnect.gesture.impl.GestureProxyImpl;

/**
 * This interface provides functionality to add {@link Gesture}s and
 * {@link GestureListener}s. It is recommended to use this interface for doing
 * stuff containing gestures.
 * 
 * @author Eugen Neufeld
 * 
 */
public interface GestureProxy {
	/**
	 * an instance of the {@link GestureProxy} interface
	 */
	public GestureProxy INSTANCE = GestureProxyImpl.getInstance();

	/**
	 * adds a gesture listener
	 * 
	 * @param gestureListener
	 *            - the {@link GestureListener} to add
	 */
	public void addGestureListener(GestureListener gestureListener);

	/**
	 * removes a gesture listener
	 * 
	 * @param gestureListener
	 *            - the {@link GestureListener} to remove
	 */
	public void removeGestureListener(GestureListener gestureListener);

	/**
	 * adds a gesture detector
	 * 
	 * @param gestureDetector
	 *            - the {@link Gesture} to add
	 */
	public void addGestureDetector(Gesture gestureDetector);

	/**
	 * removes a gesture detector
	 * 
	 * @param gestureDetector
	 *            - the {@link Gesture} to remove
	 */
	public void removeGestureDetector(Gesture gestureDetector);
}