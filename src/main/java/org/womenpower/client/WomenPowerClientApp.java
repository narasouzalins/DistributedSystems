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
    private CourseServicePanel courseServicePanel;
    private JobHubPanel jobHubPanel;

    //constructor
    public WomenPowerClientApp() {
        super("WomenPower Platform - GUI Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 700);  //adjust size to fit better
        setLocationRelativeTo(null);

        //initialize gRPC channels
        userProfileChannel = ManagedChannelBuilder.forAddress("localhost", 8090).usePlaintext().build();
        courseChannel = ManagedChannelBuilder.forAddress("localhost", 8091).usePlaintext().build();
        jobHubChannel = ManagedChannelBuilder.forAddress("localhost", 8092).usePlaintext().build();

        setLayout(new BorderLayout());

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new GridLayout(1, 3));  //to place panels side-by-side
        UserProfilePanel userProfilePanel = new UserProfilePanel(userProfileChannel);
        mainContentPanel.add(userProfilePanel);

        courseServicePanel = new CourseServicePanel(courseChannel);
        mainContentPanel.add(courseServicePanel);

        add(mainContentPanel, BorderLayout.CENTER);
        setVisible(true);  //window get visible for users

        jobHubPanel  = new JobHubPanel(jobHubChannel);
        mainContentPanel.add(jobHubPanel);

        add(mainContentPanel, BorderLayout.CENTER);
        setVisible(true);

        // Add window listener for gRPC channel shutdown
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closing grpc channel...");
                userProfileChannel.shutdownNow();
                courseChannel.shutdownNow();
                jobHubChannel.shutdownNow();
            }
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WomenPowerClientApp(); // This line creates and displays your application frame
        });
    }
}

