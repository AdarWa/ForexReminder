package net.adarw;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Server {

    public ZContext context = null;
    public ZMQ.Socket socket = null;

    public Server(ZContext context){
        this.context = context;
        socket = context.createSocket(SocketType.REP);
        socket.bind("tcp://*:5555");
    }

    public boolean isActive(){
        return !Thread.currentThread().isInterrupted();
    }

    public String receiveMessage(){
        byte[] msg = socket.recv(0);
        return new String(msg, ZMQ.CHARSET);
    }

    public void sendMessage(String msg){
        socket.send(msg.getBytes(ZMQ.CHARSET), 0);
    }

}
