package org.womenpower.client;
import io.grpc.stub.StreamObserver;
import org.womenpower.jobhub.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.womenpower.jobhub.JobHubServiceGrpc;
import org.womenpower.jobhub.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

public class JobHubPanel extends JPanel {
    private ManagedChannel channel;

    private JobHubServiceGrpc.JobHubServiceBlockingStub blockingStub;
    private JobHubServiceGrpc.JobHubServiceStub asyncStub;

    //components to findMentor
    private JTextField mentorshipUserIdField;
    private JTextField mentorshipInterestsField;
    private JTextArea mentorshipResultsArea;
    private JButton findMentorshipButton;

    //components for match job
    private JTextField jobUserIdField;
    private JTextField jobLocationField;
    private JTextField jobMinSalaryField;
    private JTextArea jobResultsArea;
    private JButton matchJobButton;

    public JobHubPanel(ManagedChannel jobHubChannel) {
        setLayout(new BorderLayout());
        this.channel = jobHubChannel;
        this.blockingStub = JobHubServiceGrpc.newBlockingStub(channel);
        this.asyncStub = JobHubServiceGrpc.newStub(channel);

        JPanel mentorshipPanel = new JPanel(new BorderLayout());
        mentorshipPanel.setBorder(BorderFactory.createTitledBorder("Find Mentorship"));

        JPanel mentorshipInputPanel = new JPanel(new GridLayout(3, 2));
        mentorshipInputPanel.add(new JLabel("User ID:"));
        mentorshipUserIdField = new JTextField();
        mentorshipInputPanel.add(mentorshipUserIdField);
        mentorshipInputPanel.add(new JLabel("Interests (comma-separated):"));
        mentorshipInterestsField = new JTextField();
        mentorshipInputPanel.add(mentorshipInterestsField);

        findMentorshipButton = new JButton("Find Mentorship");
        mentorshipInputPanel.add(findMentorshipButton);
        mentorshipPanel.add(mentorshipInputPanel, BorderLayout.NORTH);

        mentorshipResultsArea = new JTextArea(10, 40);
        mentorshipResultsArea.setEditable(false);
        JScrollPane mentorshipScrollPane = new JScrollPane(mentorshipResultsArea);
        mentorshipPanel.add(mentorshipScrollPane, BorderLayout.CENTER);

        // match job panel
        JPanel jobPanel = new JPanel(new BorderLayout());
        jobPanel.setBorder(BorderFactory.createTitledBorder("Match Job"));

        JPanel jobInputPanel = new JPanel(new GridLayout(4, 2));
        jobInputPanel.add(new JLabel("User ID:"));
        jobUserIdField = new JTextField();
        jobInputPanel.add(jobUserIdField);
        jobInputPanel.add(new JLabel("Location:"));
        jobLocationField = new JTextField();
        jobInputPanel.add(jobLocationField);
        jobInputPanel.add(new JLabel("Min Salary:"));
        jobMinSalaryField = new JTextField();
        jobInputPanel.add(jobMinSalaryField);

        matchJobButton = new JButton("Match Job");
        jobInputPanel.add(matchJobButton);
        jobPanel.add(jobInputPanel, BorderLayout.NORTH);

        jobResultsArea = new JTextArea(5, 40);
        jobResultsArea.setEditable(false);
        JScrollPane jobScrollPane = new JScrollPane(jobResultsArea);
        jobPanel.add(jobScrollPane, BorderLayout.CENTER);

        add(mentorshipPanel, BorderLayout.WEST);
        add(jobPanel, BorderLayout.EAST);

        findMentorshipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findMentorship();
            }
        });

        matchJobButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                matchJob();
            }
        });
    }
    private void findMentorship() {
        String userId = mentorshipUserIdField.getText().trim();
        String interestsText = mentorshipInterestsField.getText().trim();
        java.util.List<String> interests = Arrays.asList(interestsText.split("\\s*,\\s*"));

        MentorshipQuery.Builder requestBuilder = MentorshipQuery.newBuilder();
        if (!userId.isEmpty()) {
            requestBuilder.setUserId(userId);
        }
        if (!interests.isEmpty() && !(interests.size() == 1 && interests.get(0).isEmpty())) {
            requestBuilder.addAllInterests(interests);
        }

        MentorshipQuery request = requestBuilder.build();
        mentorshipResultsArea.setText("Searching for mentorships...\n");

        asyncStub.findMentorship(request, new StreamObserver<Mentorship>() {
            private StringBuilder results = new StringBuilder();
            private int count = 0;

            @Override
            public void onNext(Mentorship mentorship) {
                count++;
                results.append("Found Mentor: ").append(mentorship.getMentorName())
                        .append(" (").append(mentorship.getExpertise()).append(")\n")
                        .append("  Contact: ").append(mentorship.getContactInfo()).append("\n\n");
                SwingUtilities.invokeLater(() -> mentorshipResultsArea.setText(results.toString()));
            }

            @Override
            public void onError(Throwable t) {
                SwingUtilities.invokeLater(() -> {
                    mentorshipResultsArea.append("\nError finding mentorships: " + t.getMessage());
                    System.err.println("Error finding mentorships: " + t);
                    t.printStackTrace();
                });
            }

            @Override
            public void onCompleted() {
                SwingUtilities.invokeLater(() -> {
                    if (count == 0) {
                        mentorshipResultsArea.append("\nNo mentorships found based on criteria.");
                    } else {
                        mentorshipResultsArea.append("\nSearch complete. Found " + count + " mentorship(s).");
                    }
                });
            }
        });
    }

    private void matchJob() {
        String userId = jobUserIdField.getText().trim();
        String location = jobLocationField.getText().trim();
        int minSalary = 0;
        try {
            minSalary = Integer.parseInt(jobMinSalaryField.getText().trim());
        } catch (NumberFormatException ex) {
            jobResultsArea.setText("Error: Minimum salary must be a number.");
            return;
        }

        JobMatchRequest request = JobMatchRequest.newBuilder()
                .setUserId(userId)
                .setLocation(location)
                .setMinSalary(minSalary)
                .build();

        jobResultsArea.setText("Searching for a job...\n");

        new SwingWorker<JobListing, Void>() {
            @Override
            protected JobListing doInBackground() throws Exception {
                return blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS).matchJob(request);
            }

            @Override
            protected void done() {
                try {
                    JobListing job = get();
                    if (job != null && !job.getJobTitle().isEmpty()) {
                        jobResultsArea.setText("Job Found:\n" +
                                "  Title: " + job.getJobTitle() + "\n" +
                                "  Company: " + job.getCompany() + "\n" +
                                "  Salary: " + job.getSalaryRange());
                    } else {
                        jobResultsArea.setText("No job found matching criteria.");
                    }
                } catch (Exception ex) {
                    jobResultsArea.setText("Error searching for job: " + ex.getMessage());
                    System.err.println("Error matching job: " + ex);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}
