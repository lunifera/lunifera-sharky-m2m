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
package org.jnect.gesture.util;

import java.util.LinkedList;
import java.util.Queue;
/**
 * This class helps to calculate a moving average, this way the data can be cleaned.
 * @author Philip Achenbach
 * @author Eugen Neufeld
 */
public class MovingAverageCalculator {

	private final Queue<Float> window = new LinkedList<Float>();
	private final int numPeriods;
	private float sum;
	
	/**
	 * instantiate the {@link MovingAverageCalculator} with a number of periods to build the average over
	 * @param numPeriods - number of values to build the average over
	 */
	public MovingAverageCalculator(int numPeriods) {
		this.numPeriods = numPeriods;
	}
	/**
	 * add a value to the moving average, if the number of periods is exceeded, then the oldest value is removed.  
	 * @param num - the value to add to the moving average
	 */
	public void addValue(float num) {
		sum += num;
		window.add(num);
		if (window.size() > numPeriods) {
			sum -= window.remove();
		}
	}
	/**
	 * get the current moving average
	 * @return - current value of the moving average
	 */
	public float getMovingAvg() {
		return (window.size() == 0) ? 0 : sum / window.size();
	}
}
