package jot.chapter3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import jdk.dio.DeviceManager;
import jdk.dio.uart.UART;
import jdk.dio.uart.UARTConfig;

public class UARTEchoServer {
    
    private UART uart;
    private BufferedReader in;
    private BufferedWriter out;
    
    public UARTEchoServer(String controllerName) throws IOException{
    	if(controllerName == null)
    		controllerName = "ttyAMA0";
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
        in = new BufferedReader(new InputStreamReader(Channels.newInputStream(uart)));
        out = new BufferedWriter(new OutputStreamWriter(Channels.newOutputStream(uart)));
        uart.setReceiveTimeout(100);
    }
    
    public void run() throws IOException{
        System.out.println("Waiting message...");
        out.write("input message");
        out.newLine();
        out.flush();
        
        for (String line = in.readLine(); 
                line == null || !line.equals("quit"); 
                line = in.readLine()) {
            if(line == null)
                continue;
            
            System.out.println("Received message: " + line);
            out.write(line);
            out.newLine();
            out.flush();
        }
        
        close();
    }
    
    private void close() throws IOException{
        in.close();
        out.close();
        uart.close();
    }
    
    public static void main(String[] args) throws Exception {
        UARTEchoServer echo = new UARTEchoServer(args.length == 1 ? args[0] : null);
        echo.run();
    }
}
