package org.womenpower.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.womenpower.courses.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;


public class CourseServicePanel extends JPanel implements ActionListener {
    //enrollment course field
    private JTextField userIdEnrollField; //User ID input
    private JTextField courseIdEnrollField; //course id input

    //generateCert field
    private JTextField enrollmentIdCertField;     // Enrollment ID input for certificate
    private JTextField assessmentScoreCertField;   // Assessment Score input for certificate

    //buttons
    private JButton enrollCourseButton;
    private JButton generateCertificateButton;

    private JTextArea resultArea; //to show the results

    // gRPC Client Stub and Channel
    private CourseServiceGrpc.CourseServiceBlockingStub courseServiceStub; // Blocking stub for making gRPC calls
    private ManagedChannel channel; // gRPC channel to communicate with the server


    //constructor
    public CourseServicePanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Add padding around the panel
        setBackground(new Color(240, 248, 255));     // Set a light background color

        //label
        JLabel titleLabel = new JLabel("Course Service");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);  //add title to main panel
        add(Box.createVerticalStrut(15)); //space

        //enrollment panel
        JPanel enrollmentPanel = new JPanel();
        enrollmentPanel.setLayout(new BoxLayout(enrollmentPanel, BoxLayout.LINE_AXIS));
        enrollmentPanel.setBackground(new Color(240, 248, 255));
        enrollmentPanel.setBorder(BorderFactory.createTitledBorder("Enroll in Course"));   //title

        //user id for enroll
        JPanel userIdEnrollPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userIdEnrollPanel.setBackground(new Color(240, 248, 255));
        userIdEnrollPanel.add(new JLabel("User ID:   "));
        userIdEnrollField = new JTextField(20);
        userIdEnrollPanel.add(userIdEnrollField);
        enrollmentPanel.add(userIdEnrollPanel);
        enrollmentPanel.add(Box.createVerticalStrut(5));

        // course id for enroll
        JPanel courseIdEnrollPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        courseIdEnrollPanel.setBackground(new Color(240, 248, 255));
        courseIdEnrollPanel.add(new JLabel("Course ID:   "));
        courseIdEnrollField = new JTextField(20);
        courseIdEnrollPanel.add(courseIdEnrollField);
        enrollmentPanel.add(courseIdEnrollPanel);
        enrollmentPanel.add(Box.createVerticalStrut(10));

        //enroll button
        enrollCourseButton = new JButton("Enroll Course");
        enrollCourseButton.addActionListener(this);
        JPanel enrollButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        enrollButtonPanel.setBackground(new Color(240, 248, 255));
        enrollButtonPanel.add(enrollCourseButton);
        enrollmentPanel.add(enrollButtonPanel);

        add(enrollmentPanel);
        add(Box.createVerticalStrut(15));

        //GenerateCert panel
        JPanel certificatePanel = new JPanel();
        certificatePanel.setLayout(new BoxLayout(certificatePanel, BoxLayout.PAGE_AXIS));
        certificatePanel.setBackground(new Color(240, 248, 255));
        certificatePanel.setBorder(BorderFactory.createTitledBorder(" Generate Certificate"));

        //enroll id for cert
        JPanel enrollmentIdCertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enrollmentIdCertPanel.setBackground(new Color(240, 248, 255));
        enrollmentIdCertPanel.add(new JLabel("Enrollment ID:   "));
        enrollmentIdCertField = new JTextField(20);
        enrollmentIdCertPanel.add(enrollmentIdCertField);
        certificatePanel.add(enrollmentIdCertPanel);
        certificatePanel.add(Box.createVerticalStrut(5));

        //assess score cert
        JPanel assessmentScoreCertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assessmentScoreCertPanel.setBackground(new Color(240, 248, 255));
        assessmentScoreCertPanel.add(new JLabel("Assessment Score:   "));
        assessmentScoreCertField = new JTextField(20);
        assessmentScoreCertPanel.add(assessmentScoreCertField);
        certificatePanel.add(assessmentScoreCertPanel);
        certificatePanel.add(Box.createVerticalStrut(5));

        //generate cert button
        generateCertificateButton = new JButton("Generate Certificate");
        generateCertificateButton.addActionListener(this);
        JPanel certificateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        certificateButtonPanel.setBackground(new Color(240, 248, 255));
        certificateButtonPanel.add(generateCertificateButton);
        certificatePanel.add(certificateButtonPanel);

        add(certificatePanel);
        add(Box.createVerticalStrut(20));

        //result area
        resultArea = new JTextArea(10, 40); //initialize text area
        resultArea.setEditable(false); //make it read only
        resultArea.setMargin(new Insets(5, 5, 5, 5));  //add padding
        JScrollPane scrollPane = new JScrollPane(resultArea); //add a scroll
        add(scrollPane);
        add(Box.createVerticalGlue());

        //initialize gRPC client
        initializeGrpcClient();
    }

    //method to initialize gRPC client
    private void initializeGrpcClient() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
        courseServiceStub = CourseServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() {       //method to turn off the grpc channel
        try {
            if (channel != null) {
                //try to turn off the channel and wait 5 seconds
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            //if interrupted the turned off...
            System.out.println("Shut down interrupted");
            Thread.currentThread().interrupt();
        }
    }

    // ActionListener implementation for button clicks
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enrollCourseButton) {  //check if the button was clicked
            enrollCourse();
        } else if (e.getSource() == generateCertificateButton) {
            generateCertificate();
        }
    }

    private void enrollCourse() {
        resultArea.setText("");
        String userId = userIdEnrollField.getText(); //
        String courseId = courseIdEnrollField.getText();

        if (userId.isEmpty() || courseId.isEmpty()) {
            resultArea.append("UserID and Course ID are required");
            return;
        }
        try {
            CourseEnrollment request = CourseEnrollment.newBuilder()
                    .setUserId(userId)
                    .setCourseId(courseId)
                    .build();

            EnrollmentStatus response = courseServiceStub.enrollCourse(request);

            resultArea.append("Enrollment successful!\n");
            resultArea.append("Enrollment ID: " + response.getEnrollmentId() + "\n");
            resultArea.append("Course Link: " + response.getCourseLink() + "\n");
        } catch (Exception e) {
            resultArea.append("Error during enrollment: " + e.getMessage() + "\n");
            e.printStackTrace(); //print complete
        }
    }
    private void generateCertificate() {
        resultArea.setText("");
        String enrollmentId = enrollmentIdCertField.getText(); //
        String assessmentScore = assessmentScoreCertField.getText();

        if(enrollmentId.isEmpty() || assessmentScore.isEmpty()) {
            resultArea.append("Enrollment ID and Assessment Score are required");
            return;
        }
        try{
            CertificateRequest request = CertificateRequest.newBuilder()
                    .setEnrollmentId(enrollmentId)
                    .setAssessmentScore(assessmentScore)
                    .build();
            Certificate response = courseServiceStub.generateCertificate(request);

            resultArea.append("Certificate Generated!\n");
            resultArea.append("PDF URL: " + response.getPdfUrl() + "\n");
        }catch(Exception e){
            resultArea.append("Error generating certificate!\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}