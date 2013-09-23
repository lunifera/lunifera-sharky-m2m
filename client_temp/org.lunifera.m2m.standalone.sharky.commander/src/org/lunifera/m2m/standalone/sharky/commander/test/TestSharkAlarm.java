package org.lunifera.m2m.standalone.sharky.commander.test;

import org.junit.Test;
import org.lunifera.m2m.standalone.sharky.commander.Controller;
import org.lunifera.m2m.standalone.sharky.commander.api.ISharkyController;
import org.lunifera.m2m.standalone.sharky.commander.api.ISharkyController.CommandRecorder;

public class TestSharkAlarm {

	@Test
	public void test() throws InterruptedException {
		Thread.sleep(10000);
		ISharkyController.CommandRecorder recorder = new CommandRecorder();
		Controller c = new Controller();
		c.stop(recorder);
		c.pitch(1, recorder);
		c.speed(1, recorder);
		c.speed(1, recorder);
		c.speed(1, recorder);
		c.speed(1, recorder);
		c.speed(1, recorder);
		System.out.println("speed 3");
		Thread.sleep(3000);
		c.rotation(-1, recorder);
		c.rotation(-1, recorder);
		c.rotation(-1, recorder);
		c.rotation(-1, recorder);
		c.rotation(-1, recorder);
		System.out.println("rotation -5");
		Thread.sleep(10000);
		c.rotation(1, recorder);
		c.rotation(1, recorder);
		c.rotation(1, recorder);
		c.rotation(1, recorder);
		c.rotation(1, recorder);
		System.out.println("rotation 0");
		Thread.sleep(5000);
		c.stop(recorder);
	}

}
