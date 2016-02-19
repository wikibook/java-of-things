package jot.chapter3;

import java.io.IOException;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

public class ConfigDIO {

	public static final int LED_PIN = 18;
	public static final int interval = 3000; // 3초
	
	public static void main(String[] args) {
		System.out.println("Accessing GPIO ...");
		try {
			GPIOPinConfig pinConfig = new GPIOPinConfig(GPIOPinConfig.DEFAULT, LED_PIN, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false);
			GPIOPin pin = (GPIOPin) DeviceManager.open(GPIOPin.class, pinConfig);
			boolean value = false;
			for (int i = 0; i < 10; i++) {
				pin.setValue(value = !value);
				if(value)
					System.out.println("ON");
				else
					System.out.println("OFF");
				
				Thread.sleep(interval);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
