package org.womenpower.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.womenpower.courses.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class CourseServicePanel extends JPanel implements ActionListener {
    //enrollment course field
    private JTextField userIdEnrollField; //User ID input
    private JComboBox<String> courseDropdown;
    private Map<String, String> courseNameToIdMap;

    //generateCert field
    private JTextField enrollmentIdCertField;     // Enrollment ID input for certificate

    //buttons
    private JButton enrollCourseButton;
    private JButton generateCertificateButton;

    private JTextArea resultArea; //to show the results

    // gRPC Client Stub and Channel
    private CourseServiceGrpc.CourseServiceBlockingStub courseServiceStub; // Blocking stub for making gRPC calls
    private ManagedChannel channel; // gRPC channel to communicate with the server

    //constructor
    public CourseServicePanel(ManagedChannel channel) {
        this.channel = channel;
        this.courseServiceStub = CourseServiceGrpc.newBlockingStub(channel);

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

        //courses dropdown
        JPanel courseDropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        courseDropdownPanel.setBackground(new Color(240, 248, 255));
        courseDropdownPanel.add(new JLabel("Select Course:"));
        courseDropdown = new JComboBox<>();
        courseDropdown.setPreferredSize(new Dimension(200, 25));
        courseNameToIdMap = new HashMap<>();
        courseDropdownPanel.add(courseDropdown);
        enrollmentPanel.add(courseDropdownPanel);

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
        enrollmentIdCertPanel.add(new JLabel("Enrollment ID: "));
        enrollmentIdCertField = new JTextField(20);
        enrollmentIdCertPanel.add(enrollmentIdCertField);
        certificatePanel.add(enrollmentIdCertPanel);

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

        loadAvailableCourses();
    }
    public void shutdown() { //method to turn off the grpc channel
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
    private void loadAvailableCourses() {
        courseDropdown.removeAllItems();
        courseNameToIdMap.clear();

    ListCoursesRequest request = ListCoursesRequest.newBuilder().build();
        try {
        System.out.println("Attempting to load available courses...");
        CourseList response = courseServiceStub.listCourses(request);

        SwingUtilities.invokeLater(() -> {
            if (response.getSuccess()) {
                for (Course course : response.getCoursesList()) {
                    courseDropdown.addItem(course.getTitle());
                    courseNameToIdMap.put(course.getTitle(), course.getCourseId());
                }
                resultArea.append("Courses loaded successfully.\n");
            } else {
                resultArea.append("Error loading courses: " + response.getMessage() + "\n"); // Exibe mensagem do servidor
            }
            courseDropdown.revalidate();
            courseDropdown.repaint();
            CourseServicePanel.this.revalidate();
            CourseServicePanel.this.repaint();
        });

        }catch (StatusRuntimeException e) {
            System.err.println("gRPC error loading courses: " + e.getStatus() + " - " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                resultArea.append("Error loading courses (gRPC issue): " + e.getMessage() + "\n");
            });
        }
    }
    private void enrollCourse(){
        resultArea.setText("");
        String userId = userIdEnrollField.getText().trim(); //use trim to remove blank spaces
        String selectedCourseTitle = (String) courseDropdown.getSelectedItem(); //get the item selected
        String courseId = null;

        if (selectedCourseTitle != null && courseNameToIdMap.containsKey(selectedCourseTitle)) {
            courseId = courseNameToIdMap.get(selectedCourseTitle);
        }

        if (userId.isEmpty() || courseId == null || courseId.isEmpty()) { //validate user and id
            resultArea.append("User ID and a course selection are required.\n");
            return;
        }
        try {
            CourseEnrollment request = CourseEnrollment.newBuilder()
                    .setUserId(userId)
                    .setCourseId(courseId)
                    .build();

            EnrollmentStatus response = courseServiceStub.enrollCourse(request);

            if (response.getSuccess()) {
                resultArea.append("Enrollment successful!\n");
                resultArea.append("Enrollment ID: " + response.getEnrollmentId() + "\n");
                resultArea.append("Course Link: " + response.getCourseLink() + "\n");
                resultArea.append("Message: " + response.getMessage() + "\n");

                userIdEnrollField.setText("");
                courseDropdown.setSelectedIndex(-1); // Reset selection
            } else {
                resultArea.append("Enrollment failed: " + response.getMessage() + "\n");
            }
        } catch (StatusRuntimeException e) { // Use StatusRuntimeException para gRPC
            resultArea.append("Error during enrollment (gRPC issue): " + e.getMessage() + "\n");
            System.err.println("gRPC error during enrollment: " + e.getStatus() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void generateCertificate() {
        resultArea.setText("");
        String enrollmentId = enrollmentIdCertField.getText().trim();

        if (enrollmentId.isEmpty()) {
            resultArea.append("Enrollment ID is required to generate a certificate.\n");
            return;
        }
        try {
            CertificateRequest request = CertificateRequest.newBuilder()
                    .setEnrollmentId(enrollmentId)
                    .build();

            Certificate response = courseServiceStub.generateCertificate(request);

            if (response.getSuccess()) {
                resultArea.append("Certificate Generated!\n");
                resultArea.append("PDF URL: " + response.getPdfUrl() + "\n");
                resultArea.append("Message: " + response.getMessage() + "\n");
                enrollmentIdCertField.setText("");
            } else {
                resultArea.append("Error generating certificate: " + response.getMessage() + "\n");
            }
        } catch (StatusRuntimeException e) { // Use StatusRuntimeException to gRPC
            resultArea.append("Error generating certificate (gRPC issue): " + e.getMessage() + "\n");
            System.err.println("gRPC error during certificate generation: " + e.getStatus() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}