/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Eugen Neufeld - initial API and implementation
 *******************************************************************************/
package org.jnect.gesture.impl.m2m;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jnect.bodymodel.Body;
import org.jnect.core.KinectManager;
import org.jnect.core.impl.connection.m2m.M2MManagerImpl;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.GestureListener;
import org.jnect.gesture.GestureProxy;
import org.jnect.gesture.impl.GestureProxyCallback;

public class M2MGestureProxyImpl implements GestureProxyCallback, GestureProxy {

	private static final GestureProxy INSTANCE = new M2MGestureProxyImpl();

	public static GestureProxy getInstance() {
		return INSTANCE;
	}

	private KinectManager kinectManager = M2MManagerImpl.getInstance();

	private Map<Gesture, Set<GestureListener>> filteredGestureListeners = new HashMap<Gesture, Set<GestureListener>>();
	private Set<GestureListener> unfilteredGestureListeners = new HashSet<GestureListener>();

	@Override
	public void notifyGestureDetected(Class<? extends Gesture> gesture) {
		Set<GestureListener> listeners = new HashSet<GestureListener>(unfilteredGestureListeners);
		if (filteredGestureListeners.containsKey(gesture)) {
			listeners.addAll(filteredGestureListeners.get(gesture));
		}
		for (GestureListener listener : listeners) {
			listener.notifyGestureDetected(gesture);
		}
	}

	@Override
	public void addGestureListener(GestureListener gestureListener) {
		if (!gestureListener.isFiltered()) {
			unfilteredGestureListeners.add(gestureListener);
		} else {
			for (Gesture gesture : gestureListener.getGestures()) {
				if (!filteredGestureListeners.containsKey(gesture)) {
					filteredGestureListeners.put(gesture, new HashSet<GestureListener>());
				}
				filteredGestureListeners.get(gesture).add(gestureListener);
			}
		}
	}

	@Override
	public void removeGestureListener(GestureListener gestureListener) {
		unfilteredGestureListeners.remove(gestureListener);
		if (gestureListener.isFiltered()) {
			for (Gesture gesture : gestureListener.getGestures()) {
				if (filteredGestureListeners.containsKey(gesture)) {
					filteredGestureListeners.get(gesture).remove(gestureListener);
				}
			}
		}
	}

	@Override
	public void addGestureDetector(Gesture gestureDetector) {
		gestureDetector.setGestureProxy(this);
		Body body = kinectManager.getSkeletonModel();
		body.eAdapters().add(gestureDetector);
	}

	@Override
	public void removeGestureDetector(Gesture gestureDetector) {
		gestureDetector.setGestureProxy(null);
		Body body = kinectManager.getSkeletonModel();
		body.eAdapters().remove(gestureDetector);
	}

}
