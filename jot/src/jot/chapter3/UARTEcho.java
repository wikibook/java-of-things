package jot.chapter3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Scanner;
import jdk.dio.DeviceManager;
import jdk.dio.uart.UART;
import jdk.dio.uart.UARTConfig;

public class UARTEcho {
    
    private UART uart;
    private BufferedReader in;
    private BufferedWriter out;
    
    public UARTEcho(String controllerName) throws IOException{
    	if(controllerName == null)
    		controllerName = "ttyAMA0";
    	
//        UARTConfig config = new UARTConfig.Builder()
//                .setControllerName(controllerName)
//                .setChannelNumber(1)
//                .setBaudRate(9600)
//                .setDataBits(UARTConfig.DATABITS_8)
//                .setParity(UARTConfig.PARITY_NONE)
//                .setStopBits(UARTConfig.STOPBITS_1)
//                .setFlowControlMode(UARTConfig.FLOWCONTROL_NONE)
//                .build();
        UARTConfig config = new UARTConfig(
                controllerName,
                1,
                9600,
                UARTConfig.DATABITS_8,
                UARTConfig.PARITY_NONE,
                UARTConfig.STOPBITS_1,
                UARTConfig.FLOWCONTROL_NONE
        );
        
        uart = (UART) DeviceManager.open(config);
        in = new BufferedReader(Channels.newReader(uart, "UTF-8"));
        out = new BufferedWriter(Channels.newWriter(uart, "UTF-8"));
        uart.setReceiveTimeout(100);
    }
    
    public void run() throws IOException{
        Scanner input = new Scanner(System.in);
        System.out.print("input message or 'q'(quit): ");
        byte[] message;
        for (String line = input.nextLine(); 
                !line.equals("q"); 
                line = input.nextLine()) {
            message = line.getBytes();
            out.write(line);
            out.newLine();
            out.flush();
            System.out.println("Echo: " + in.readLine());
            System.out.print("input message or 'q'(quit): ");
        }
        close();
    }
    
    private void close() throws IOException{
        in.close();
        out.close();
        uart.close();
    }
    
    public static void main(String[] args) throws Exception {
        UARTEcho echo = new UARTEcho(args.length == 1 ? args[0] : null);
        echo.run();
    }
}
