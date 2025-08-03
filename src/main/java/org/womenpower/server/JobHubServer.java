package org.womenpower.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.jobhub.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/*
 * The main server application for the Job Hub service.
 * This class sets up and starts the gRPC server, which provides
 * mentorship and job matching services.
 */
public class JobHubServer {

    private Server server;
    private static final int PORT = 8092;

    public static void main(String[] args) throws IOException, InterruptedException {
        final JobHubServer jobHubServer = new JobHubServer();
        jobHubServer.start();
        jobHubServer.blockUntilShutdown();
    }

    //Start the server on the port 8092
    private void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new JobHubServiceImpl())
                .build()
                .start();

        System.out.println("JobHub Server started, listening on port " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server gracefully.");
            if (server != null) {
                server.shutdown();
            }
        }));
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
        // This class handles all RPC calls defined in the .proto
    private static class JobHubServiceImpl extends JobHubServiceGrpc.JobHubServiceImplBase {

        // Mock database to test mentors
        private final List<Mentorship> mentors = Arrays.asList(
                Mentorship.newBuilder().setMentorName("Dr. Alice Smith").setExpertise("Cloud Computing").setContactInfo("alice.s@example.com").build(),
                Mentorship.newBuilder().setMentorName("Jane Doe").setExpertise("Data Science").setContactInfo("jane.d@example.com").build(),
                Mentorship.newBuilder().setMentorName("Sarah Jones").setExpertise("Cybersecurity").setContactInfo("sarah.j@example.com").build(),
                Mentorship.newBuilder().setMentorName("Lucy Chen").setExpertise("Project Management").setContactInfo("lucy.c@example.com").build()
        );

        // Mock database to test jobs
        private final JobListing job = JobListing.newBuilder()
                .setJobTitle("Software Developer")
                .setCompany("Tech Solutions Inc.")
                .setSalaryRange(85000)
                .build();

        //Method to find mentorship for a specific interest
        @Override
        public void findMentorship(MentorshipQuery request, StreamObserver<Mentorship> responseObserver) {
            System.out.println("FindMentorship request received for user ID: " + request.getUserId());
            System.out.println("Interests: " + request.getInterestsList());

            // sending all mentors
            for (Mentorship mentorship : mentors) {
                // send one mentor per time
                responseObserver.onNext(mentorship);


                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            responseObserver.onCompleted();
            System.out.println("FindMentorship stream completed.");
        }


        //matchJob implementation
        @Override
        public void matchJob(JobMatchRequest request, StreamObserver<JobListing> responseObserver) {
            System.out.println("MatchJob request received for user ID: " + request.getUserId());
            System.out.println("Location: " + request.getLocation());
            System.out.println("Min Salary: " + request.getMinSalary());

            responseObserver.onNext(job);
            responseObserver.onCompleted();
            System.out.println("MatchJob response sent.");
        }
    }
}