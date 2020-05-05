package lesson7.server;

import lesson7.server.networkserver.MyServer;

public class ServerApp {

    private static final int PORT = 8189;

    public static void main(String[] args) {
        MyServer server = new MyServer(8189);
        server.start();
    }
}
