package jot.chapter3;

import java.io.IOException;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;

public class SevenSegment {

    public static final boolean COMMON_ANODE = true;
    public static final boolean COMMON_CATHODE = false;
    
    public static final int[] SEVEN_SEGMENT_PIN_NUM = 
                        {14, 15, 18, 24, 25, 8, 7, 23}; // A,B,C,D,E,F,G,DIP
    public static final boolean[][] SEVEN_SEGMENT_CONTROL = {
        {true, true, true, true, true, true, false}, // 0
        {false, true, true, false, false, false, false}, // 1
        {true, true, false, true, true, false, true}, // 2
        {true, true, true, true, false, false, true}, // 3
        {false, true, true, false, false, true, true}, // 4
        {true, false, true, true, false, true, true}, // 5
        {true, false, true, true, true, true, true}, // 6
        {true, true, true, false, false, false, false}, // 7
        {true, true, true, true, true, true, true}, // 8
        {true, true, true, true, false, true, true}, // 9
    };

    private final GPIOPin[] sevenSegmentPin = 
                        new GPIOPin[SEVEN_SEGMENT_PIN_NUM.length];
    private final boolean type;

    public SevenSegment(boolean type) throws IOException {
        this.type = type;
        for (int i = 0; i < SEVEN_SEGMENT_PIN_NUM.length; i++) {
            sevenSegmentPin[i] = 
                    (GPIOPin) DeviceManager.open(SEVEN_SEGMENT_PIN_NUM[i]);
            System.out.println("Successful open: " + 
                    sevenSegmentPin[i].getDescriptor().getName());
        }
        clear();
    }

    public void run() throws IOException{
        for(int i = 9; i > -1; i--) {
            try{
                display(i, true);
                Thread.sleep(1000);
            }catch(InterruptedException e){
                System.out.println("Error!! " + e.getMessage());
            }
        }
        
        close();
    }
    
    private void display(int number, boolean dip) throws IOException{
        System.out.println("Display a number: " + number);
        sevenSegmentPin[SEVEN_SEGMENT_PIN_NUM.length - 1]
                .setValue(type? !dip : dip);
        boolean[] control = SEVEN_SEGMENT_CONTROL[number];
        for (int i = 0; i < control.length; i++)
            sevenSegmentPin[i].setValue(type ? !control[i] : control[i]);
        
        System.out.println();
    }
    
    private void clear() throws IOException{
        for (GPIOPin pin : sevenSegmentPin) {
            pin.setValue(type);
        }
    }
    
    private void close() throws IOException{
        for (GPIOPin pin : sevenSegmentPin) {
            pin.setValue(type);
            pin.close();
        }
    }

    public static void main(String[] args) throws Exception {
        SevenSegment sevenSegment = new SevenSegment(COMMON_ANODE);
        sevenSegment.run();
    }
}