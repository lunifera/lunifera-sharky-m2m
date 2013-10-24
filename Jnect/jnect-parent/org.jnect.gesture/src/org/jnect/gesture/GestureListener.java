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

import java.util.Collections;
import java.util.Set;

/**
 * This abstract implementation of the {@link GestureListener}. It can be
 * registered at the {@link GestureProxy}. If a {@link Gesture} is detected, a
 * relevant {@link GestureListener} will be called.
 * 
 * @author Eugen Neufeld
 * 
 */
public abstract class GestureListener {

	/**
	 * callback method, that gets called when a {@link Gesture} is detected
	 * 
	 * @param gesture
	 *            - the class of the {@link Gesture} that was detected
	 */
	public abstract void notifyGestureDetected(Class<? extends Gesture> gesture);

	/**
	 * {@link Set} of {@link Gesture}s this {@link GestureListener} listens to
	 * 
	 * @return {@link Set} of {@link Gesture}s to be notified about, can be
	 *         empty but not null
	 */
	public Set<Gesture> getGestures() {
		return Collections.emptySet();
	}

	/**
	 * Whether the {@link GestureListener} listens only to special
	 * {@link Gesture}s.
	 * 
	 * @return true if only the {@link Gesture}s provided in
	 *         {@link #getGestures()} should be provided to the listener, false
	 *         otherwise
	 */
	public boolean isFiltered() {
		return false;
	}
}
