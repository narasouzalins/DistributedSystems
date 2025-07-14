import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.jobhub.*;
import java.util.Arrays;
import java.util.List;

public class JobHubServer {   //this method is to initiate the server
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(8092)
                .addService( new JobHubService())
                .build()
                .start();
        System.out.println("JobHubService started on port 8092");
        server.awaitTermination();
    }
                   //service implementation
    static class JobHubService extends JobHubServiceGrpc.JobHubServiceImplBase {
        List<Mentorship> mentours = Arrays.asList(
                Mentorship.newBuilder()
                        .setMentorName("Ann Fanegan")
                        .setExpertise("Software Engineer")
                        .setContactInfo("ann@gmail.com")
                        .build()

        );    //database
        List<JobListing> jobs = Arrays.asList(
                JobListing.newBuilder()
                        .setJobTitle("Java Developer")
                        .setCompany("Google")
                        .setSalaryRange(5000)
                        .build()
        );

        @Override
        public void findMentorship(MentorshipQuery request, StreamObserver<Mentorship> responseObserver) {
            for (Mentorship m : mentours) {
                if (request.getInterestsList().isEmpty() || m.getExpertise().toLowerCase().contains(request.getInterestsList().get(0).toLowerCase())) {
                    responseObserver.onNext(m);
                }
            }
            responseObserver.onCompleted();
            System.out.println("Mentorship found");
        }
                //MatchJOb service implementation (unary)
        @Override
        public void matchJob(JobMatchRequest request, StreamObserver<JobListing> responseObserver) {
            System.out.println("Searching for job for: " + request.getUserId());
         //Search for the first vacancy that matches:
        // Salary >= minimum requested
        // Location contains the filter
            for (JobListing j : jobs) {
                if (j.getSalaryRange() >= request.getMinSalary() &&
                        (request.getLocation().isEmpty() ||
                        j.getCompany().toLowerCase()
                        .contains(request.getLocation().toLowerCase()))) {

                    //Found a matched job and send to user
                    responseObserver.onNext(j);
                    responseObserver.onCompleted();
                    System.out.println("Job found" + j.getJobTitle());
                    return;
                }
            }//if not matches found
            responseObserver.onCompleted();
            System.out.println("No jobs found");
        }
    }
}
