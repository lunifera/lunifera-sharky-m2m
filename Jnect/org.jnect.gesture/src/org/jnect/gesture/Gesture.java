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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.jnect.gesture.impl.GestureProxyCallback;

/**
 * A {@link Gesture} is implemented based on the {@link EContentAdapter}. This
 * way this class is notified about every change in the model. To implement your
 * own {@link Gesture} you have to override the
 * {@link #isGestureDetected(Notification)} method.
 * 
 * @author Eugen Neufeld
 * @see EContentAdapter
 */
public abstract class Gesture extends EContentAdapter {

	private GestureProxyCallback gestureProxy;

	/**
	 * DO NOT CALL THIS METHOD, IT WILL BE CALLED BY THE GESTUREPROXY
	 * 
	 * @param gestureProxy
	 *            the proxy to notify when a gesture is detected
	 */
	public void setGestureProxy(GestureProxyCallback gestureProxy) {
		this.gestureProxy = gestureProxy;
	}

	@Override
	public void notifyChanged(Notification notification) {
		if (gestureProxy != null && isGestureDetected(notification)) {
			this.gestureProxy.notifyGestureDetected(this.getClass());
		}
	}

	/**
	 * checks whether the searched gesture is detected
	 * 
	 * @param notification
	 *            the notification containing the model changes
	 * @return true if the gesture was detected
	 */
	protected abstract boolean isGestureDetected(Notification notification);
}
