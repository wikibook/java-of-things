package jot.chapter4.coap.thing;

public class Main {
    public static void main(String[] args){
        final PointCoapServer server = new PointCoapServer();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                server.stop();
            }
        });
    }
}
