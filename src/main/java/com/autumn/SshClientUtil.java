package com.autumn;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class SshClientUtil {

    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final SshClient client;
    private ClientSession session;
    private String currentDirectory;

    public SshClientUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.client = SshClient.setUpDefaultClient();
        this.client.start();
    }

    public void connect() throws Exception {
        ConnectFuture future = client.connect(username, host, port);
        future.await(10, TimeUnit.SECONDS);
        this.session = future.getSession();
        this.session.addPasswordIdentity(password);
        this.session.auth().verify(10, TimeUnit.SECONDS);
        this.currentDirectory = executeCommand("pwd").trim();
    }

    public String executeCommand(String command) throws Exception {
        try (ChannelExec channel = session.createExecChannel("cd " + currentDirectory + " && " + command)) {
            channel.open().verify(10, TimeUnit.SECONDS);
            try (InputStream in = channel.getInvertedOut()) {
                byte[] response = in.readAllBytes();
                return new String(response, StandardCharsets.UTF_8);
            } finally {
                channel.close(false);
            }
        }
    }

    public void changeDirectory(String directory) throws Exception {
        String result = executeCommand("cd " + directory + " && pwd");
        if (!result.isEmpty()) {
            this.currentDirectory = result.trim();
        }
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void disconnect() {
        if (session != null) {
            session.close(false);
        }
        client.stop();
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
