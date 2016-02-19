package jot.chapter4.coap;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class ObserveClient {

    public static void main(String[] args) throws Exception {
        String uri;
        if (args.length > 0) {
            uri = "coap://" + args[0] + ":5683";
        } else {
            uri = "coap://localhost:5683";
        }

        String resource = "/hello/count";
        
        CoapClient client = new CoapClient(uri + resource);

        CoapObserveRelation relation = client.observe(new CoapHandler() {

            @Override
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                System.out.println("arrived a changed of value: " + content);
            }

            @Override
            public void onError() {
                System.err.println("Error");
            }
        });

        Thread quit = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Press enter to exit: ");
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();
                relation.proactiveCancel();
            }
        });

        quit.start();
        quit.join();
    }
}
