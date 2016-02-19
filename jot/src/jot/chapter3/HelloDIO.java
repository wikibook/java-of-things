package jot.chapter3;

import java.io.IOException;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;

public class HelloDIO {

    public static final int LED_PIN = 18;
    public static final int interval = 3000;

    public static void main(String[] args) {
        System.out.println("Accessing GPIO ...");
        try {
            GPIOPin pin = (GPIOPin) DeviceManager.open(LED_PIN);
            boolean value = false;
            for (int i = 0; i < 10; i++) {
                pin.setValue(value = !value);
                if (value) {
                    System.out.println("ON");
                } else {
                    System.out.println("OFF");
                }

                Thread.sleep(interval);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
