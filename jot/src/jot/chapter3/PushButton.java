package jot.chapter3;

import java.io.IOException;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;

public class PushButton {

    private static final int LED_PIN_NUM = 18;
    private static final int LED_BUTTON_PIN_NUM = 23;
    private static final int EXIT_BUTTON_PIN_NUM = 24;

    private GPIOPin ledPin;
    private GPIOPin ledButtonPin;
    private GPIOPin exitButtonPin;

    public PushButton() throws IOException {
        ledPin = (GPIOPin) DeviceManager.open(LED_PIN_NUM);
        ledButtonPin = (GPIOPin) DeviceManager.open(LED_BUTTON_PIN_NUM);
        exitButtonPin = (GPIOPin) DeviceManager.open(EXIT_BUTTON_PIN_NUM);
        System.out.println("Successful open");
    }

    public void run() throws IOException {
        boolean exit = false;
        boolean led = false;
        try {
            while (!exit) {
                led = ledButtonPin.getValue();
                ledPin.setValue(led);
                System.out.println("LED: " + (led ? "ON" : "OFF"));
                exit = exitButtonPin.getValue();
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {
        }

        System.out.println("Exit");
        close();
    }

    private void close() throws IOException {
        ledPin.close();
        ledButtonPin.close();
        exitButtonPin.close();
    }

    public static void main(String[] args) throws IOException {
        PushButton pushButton = new PushButton();
        pushButton.run();
    }

}
