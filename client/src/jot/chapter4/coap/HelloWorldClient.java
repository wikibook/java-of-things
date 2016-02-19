package jot.chapter4.coap;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class HelloWorldClient {

	public static void main(String[] args){
		CoapClient client = new CoapClient();
        CoapResponse response;
        String uri;
        
        Scanner input = new Scanner(System.in);
        System.out.print("Enter Path (or 'q'): ");
        for (String line = input.nextLine(); 
                !line.equals("q"); 
                line = input.nextLine()) {
			uri = "coap://" + args[0] + ":5683" + line + "?text=everybody";
			client.setURI(uri);
			response = client.get();
			if(response != null){
				System.out.println("code: " + response.getCode());
				System.out.println("options: " + response.getOptions());
				System.out.println("payload: " + Utils.toHexString(response.getPayload()));
				System.out.println("text: " + response.getResponseText());
				System.out.println("advanced: " + Utils.prettyPrint(response));
			}
			
			uri = "coap://" + args[0] + ":5683" + line;
			client.setURI(uri);
			System.out.println("Post: " + client.post("everybody", MediaTypeRegistry.TEXT_PLAIN).getResponseText());
			System.out.println("Enter Path (or 'q'): ");
        }
	}
}
