/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ${user} - initial API and implementation
 *******************************************************************************/
package org.jnect.demo.e4.handlers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.jnect.bodymodel.Body;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.core.KinectManager;
import org.jnect.core.SpeechListener;



public class OpenHandler {

	private static final String STOP_RESIZE = "Stop Resize";
	private static final String RESIZE = "Enable Resize";
	private Body skeletonModel;

	public class LeftHandAdapter extends AdapterImpl implements Adapter {

		private final MWindow window;

		public LeftHandAdapter(MWindow window) {
			this.window = window;
		}

		@Override
		public void notifyChanged(Notification msg) {
			PositionedElement pe=(PositionedElement)msg.getNotifier();
			window.setX(getX(pe));
			window.setY(getY(pe));
		}

	}
	public class RightHandAdapter extends AdapterImpl implements Adapter {
		private final MWindow window;

		public RightHandAdapter(MWindow window) {
			this.window = window;
		}

		@Override
		public void notifyChanged(Notification msg) {
			window.setWidth(getWidthBetweenHands());
			window.setHeight(getHeightBetweenHands());
		}
	}
	private Rectangle displayRect=Display.getDefault().getClientArea();
	@Execute
	public void execute(final MWindow window){
		
		final KinectManager kinectManager = KinectManager.INSTANCE;
		kinectManager.startKinect();
		final Adapter leftHandAdapter = new  LeftHandAdapter(window);
		final Adapter rightHandAdapter = new RightHandAdapter(window);
		
//		kinectManager.startSkeletonTracking();
//		skeletonModel=kinectManager.getSkeletonModel();
//		kinectManager.getSkeletonModel().getLeftHand().eAdapters().add(leftHandAdapter );
//		kinectManager.getSkeletonModel().getRightHand().eAdapters().add(rightHandAdapter );
		kinectManager.addSpeechListener(new SpeechListener() {
			

			@Override
			public void notifySpeech(String speech) {
				if(speech.equals(RESIZE)){
					resize();
				}
				else if(speech.equals(STOP_RESIZE)){
					stopresize();
				}
				
			}
			
			@Override
			public Set<String> getWords() {
				Set<String> ret = new HashSet<String>();
				ret.add(RESIZE);
				ret.add(STOP_RESIZE);
				return ret;
			}
			
			private void stopresize() {
				kinectManager.getSkeletonModel().getLeftHand().eAdapters().remove(leftHandAdapter );
				kinectManager.getSkeletonModel().getRightHand().eAdapters().remove(rightHandAdapter );
				kinectManager.stopSkeletonTracking();
				
			}

			private void resize() {
				kinectManager.startSkeletonTracking();
				skeletonModel=kinectManager.getSkeletonModel();
				kinectManager.getSkeletonModel().getLeftHand().eAdapters().add(leftHandAdapter );
				kinectManager.getSkeletonModel().getRightHand().eAdapters().add(rightHandAdapter );
				
			}

		});
		
		kinectManager.startSpeechRecognition();
		
	}

	public int getY(PositionedElement pe) {
		return displayRect.height/2-Math.round(pe.getY()*displayRect.height/2);
	}

	public int getX(PositionedElement pe) {
		return displayRect.width/2+Math.round(pe.getX()*displayRect.width/2);
	}

	public int getWidthBetweenHands() {
		return Math.round((skeletonModel.getRightHand().getX()-skeletonModel.getLeftHand().getX())/2*displayRect.width*1.1f);
	}
	public int getHeightBetweenHands() {
		return Math.round((skeletonModel.getLeftHand().getY()-skeletonModel.getRightHand().getY())/2*displayRect.height*1.1f);
	}
}
