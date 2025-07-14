package org.womenpower.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WomenPowerClientApp extends JFrame {

    private ManagedChannel userProfileChannel;
    private ManagedChannel courseChannel;
    private ManagedChannel jobHubChannel;

    //constructor
    public WomenPowerClientApp() {
        super("WomenPower Platform - GUI Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        userProfileChannel = ManagedChannelBuilder.forAddress("localhost", 8090).usePlaintext().build();
        courseChannel = ManagedChannelBuilder.forAddress("localhost", 8091).usePlaintext().build();
        jobHubChannel = ManagedChannelBuilder.forAddress("localhost", 8092).usePlaintext().build();

        setLayout(new BorderLayout());
        UserProfilePanel userProfilePanel = new UserProfilePanel(userProfileChannel);        add(userProfilePanel, BorderLayout.CENTER);
        add(userProfilePanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);                   //window get visible for users

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                System.out.println("Closing gRPC channels");
                userProfileChannel.shutdown();
                courseChannel.shutdown();
                jobHubChannel.shutdown();
            }
        });
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> new WomenPowerClientApp());
    }
}
