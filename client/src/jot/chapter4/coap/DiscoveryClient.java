package jot.chapter4.coap;

import java.util.Scanner;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.ResourceAttributes;

public class DiscoveryClient {

    public static void main(String[] args) {
        String uri;
        if (args.length > 0) {
            uri = "coap://" + args[0] + ":5683";
        } else {
            uri = "coap://localhost:5683";
        }

        String dicoveryPath = "/.well-known/core";

        CoapClient client = new CoapClient(uri + dicoveryPath);

        client.get(new CoapHandler() {

            @Override
            public void onLoad(CoapResponse response) {
                System.out.println("asynchronous get: " + response.getResponseText());
                if (response.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
                    Set<WebLink> links = LinkFormat.parse(response.getResponseText());
                    for (WebLink link : links) {
                        System.out.println(link.getURI());
                        ResourceAttributes attrs = link.getAttributes();
                        Set<String> keys = attrs.getAttributeKeySet();
                        for (String key : keys) {
                            System.out.println(key + ": " + attrs.getAttributeValues(key));
                        }
                        System.out.println("========================================");
                    }
                }
            }

            @Override
            public void onError() {
                System.err.println("Cannot dicovery");
            }
        });

        System.out.println("Press enter to exit: ");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
