package idv.neo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

import idv.neo.data.MessageData;
import idv.neo.data.MessageInfo;

/**
 * Created by Neo on 2017/4/18.
 */

public class Communication {
    private static final String TAG = "Communication";
    private Socket mSocket;

    public Communication(Socket client) {
        mSocket = client;
    }

    public void sendHelloPackage() {
        final String clientip = mSocket.getInetAddress().getHostAddress().replace("/", "");
        final String msgReply = "Hello from " + clientip + " Android, you are new client";
        final MessageInfo outinfo = new MessageInfo(MessageInfo.IDENTIFY, MessageInfo.MESSAGE_IDENTIFY_SERVER, msgReply);
        final MessageData outmessage = new MessageData(outinfo);
        serverRequest(outmessage.toString());
    }

    public void sendMessage(String message) {
        serverRequest3(message);
    }

    public String getResponse() {
        while (true) {
            if (mSocket != null) {
                while (mSocket.isConnected()) {
                    String response = readFromBufferedReader();
                    return response;
                }
            }
        }
    }

    private void serverRequest(String message) {
        try {
            final PrintStream sender = new PrintStream(mSocket.getOutputStream());
            sender.print(message + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serverRequest2(String message) {
        try {
            final PrintWriter sender = new PrintWriter(mSocket.getOutputStream(), true);
            sender.println(message + "\n");
            sender.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serverRequest3(String message) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
//            out.write(message + "\r\n");
            out.write(message + "\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serverRequest4(String message) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(mSocket.getOutputStream());
            out.writeUTF(message + "\n");
//            out.writeBytes(message + "\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFromBufferedReader() {
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readFromDataInputStream() {
        DataInputStream input = null;
        try {
            input = new DataInputStream(mSocket.getInputStream());
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readFromInputStream() {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        final byte[] buffer = new byte[1024];
        int bytesRead;
        InputStream input = null;
        String response = null;
        try {
            input = mSocket.getInputStream();
            while ((bytesRead = input.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
