package jot.chapter3;

import java.io.IOException;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.PinListener;
import jdk.dio.gpio.PinEvent;

public class PushButtonEvent implements Runnable {

    private static final int LED_PIN_NUM = 18;
    private static final int LED_BUTTON_PIN_NUM = 23;
    private static final int EXIT_BUTTON_PIN_NUM = 24;

    private GPIOPin ledPin;
    private GPIOPin ledButtonPin;
    private GPIOPin exitButtonPin;
    private volatile boolean exit = false;

    public PushButtonEvent() throws IOException {
        ledPin = (GPIOPin) DeviceManager.open(LED_PIN_NUM);
        ledButtonPin = (GPIOPin) DeviceManager.open(LED_BUTTON_PIN_NUM);
        exitButtonPin = (GPIOPin) DeviceManager.open(EXIT_BUTTON_PIN_NUM);
        System.out.println("Successful open");

        ledButtonPin.setInputListener(new PinListener() {
            public void valueChanged(PinEvent event) {
                try {
                    boolean led = event.getValue();
                    ledPin.setValue(led);
                    System.out.println("LED: " + (led ? "ON" : "OFF"));
                } catch (IOException e) {
                    System.out.println("Error! " + e.getMessage());
                    exit = true;
                }
            }
        });

        exitButtonPin.setInputListener(new PinListener() {
            public void valueChanged(PinEvent event) {
                if (!exit) {
                    exit = event.getValue();
                }
            }
        });
    }

    public void run() {
        try {
            while (!exit) {
                Thread.sleep(500);
            }
            close();
        } catch (InterruptedException ignored) {
        } catch (IOException ignored) {}

        System.out.println("Exit");
    }

    private void close() throws IOException {
        ledPin.close();
        ledButtonPin.close();
        exitButtonPin.close();
    }

    public static void main(String[] args) throws IOException {
        PushButtonEvent pushButton = new PushButtonEvent();
        Thread thread = new Thread(pushButton);
        thread.start();
    }
}
