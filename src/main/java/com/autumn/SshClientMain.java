package com.autumn;

import java.util.Scanner;

public class SshClientMain {

    public static void main(String[] args) {
        String username = "";
        String password = "";
        String host = "";
        int port = 22;

        try (Scanner scanner = new Scanner(System.in)) {
            SshClientUtil sshClient = new SshClientUtil(username, password, host, port);
            sshClient.connect();

            while (true) {
                System.out.print(sshClient.getCurrentDirectory() + " $ ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                } else if (input.equalsIgnoreCase("clear")) {
                    sshClient.clearScreen();
                } else {
                    String[] parts = input.split(" ", 2);
                    if (parts[0].equalsIgnoreCase("cd") && parts.length > 1) {
                        sshClient.changeDirectory(parts[1]);
                    } else {
                        String result = sshClient.executeCommand(input);
                        System.out.println(result);
                    }
                }
            }

            sshClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
