package com.icapps.niddler;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public class NiddlerServer extends WebSocketServer {

    private static final Logger LOG = Logger.getLogger(NiddlerServer.class.getName());

    public NiddlerServer(InetSocketAddress address) {
        super(address);
    }

    public NiddlerServer(final int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOG.log(Level.FINE, "Connection opened: " + handshake.getResourceDescriptor());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOG.log(Level.FINE, "Connection closed: " + conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOG.log(Level.FINEST, conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOG.log(Level.SEVERE, ex.toString());
    }

    /**
     * Sends a String message to all sockets
     *
     * @param message the message to be sent
     */
    public synchronized void sendToAll(String message) {
        Collection<WebSocket> connections = connections();
        for (WebSocket socket : connections) {
            socket.send(message);

        }
    }

}
