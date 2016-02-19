package jot.chapter4.coap;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class AsynchronousClient {

	public static void main(String[] args) {
		CoapClient client = new CoapClient("coap://" + args[0] + ":5683/hello?text=everybody");
		client.get(new CoapHandler() {

			@Override
			public void onLoad(CoapResponse response) {
				System.out.println(
						"asynchronous get: " + response.getResponseText());
			}

			@Override
			public void onError() {
				System.err.println("Cannot get the response asynchronously");
			}
		});

		client.post(new CoapHandler() {

			@Override
			public void onLoad(CoapResponse response) {
				System.out.println(
						"asynchronous post: " + response.getResponseText());
			}

			@Override
			public void onError() {
				System.err.println("Cannot get the response asynchronously");
			}
		}, "everybody", MediaTypeRegistry.TEXT_PLAIN);

		System.out.println("Press enter to exit: ");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
	}
}
