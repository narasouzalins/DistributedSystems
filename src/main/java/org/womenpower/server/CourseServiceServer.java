package org.womenpower.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.courses.*;

import java.util.UUID;

public class CourseServiceServer { //method to initiate the server
    public static void main(String[] args) throws Exception {
        Server sv = ServerBuilder.forPort(8091)
                .addService(new CourseEnrollmentImpl()) //to register implementation
                .build();
        System.out.println( "Starting Course Service Server");
        System.out.println("Listening on port 8091");

        sv.start(); //to initiate server
        sv.awaitTermination();  //keep the server running
    }

            //CourseService implementation
    static class CourseEnrollmentImpl extends CourseServiceGrpc.CourseServiceImplBase {

                /*
                 * enrollment implementation
                 * @param request userId and courseId
                 */
        @Override
        public void enrollCourse(CourseEnrollment request, StreamObserver<EnrollmentStatus> responseObserver) {

            String userId = request.getUserId();
            String courseId = request.getCourseId();
            // to validate user
            if(userId.isEmpty() || courseId.isEmpty()) {
                responseObserver.onNext(EnrollmentStatus.newBuilder()
                        .setEnrollmentId("Error: UserId or CourseId are required")
                        .build());
                responseObserver.onCompleted();
                return;
            }
                    // to generate an unique ID
            String enrollmentId = "ID" + UUID.randomUUID().toString();
            String courseLink = "CourseLink" + courseId;

            responseObserver.onNext(EnrollmentStatus.newBuilder()
                    .setEnrollmentId(userId)
                    .setCourseLink(courseId)
                    .build());
            responseObserver.onCompleted();
        }
                    /* generateCert implementation
                    @param request enrollmentID and AssessmentScore
                     */
        @Override
        public void generateCertificate(CertificateRequest request, StreamObserver<Certificate> responseObserver) {
            String enrollmentId = request.getEnrollmentId();
            String assessmentScore = request.getAssessmentScore();

            //validation
            if(enrollmentId.isEmpty() || assessmentScore.isEmpty()) {
                responseObserver.onNext(Certificate.newBuilder()
                        .setPdfUrl("Error: Missing enrollment ID or assessment score")
                        .build());
                responseObserver.onCompleted();
                return;
            }
            //verify if the score is only numbers
            if (!assessmentScore.matches("\\d+")){
                responseObserver.onNext(Certificate.newBuilder()
                .setPdfUrl("Error: The score must be a number")
                        .build());
                responseObserver.onCompleted();
                return;
            }
            // Convert to number
            int score = Integer.parseInt(assessmentScore);

            //verify min score
            if(score < 70) {
                responseObserver.onNext(Certificate.newBuilder()
                        .setPdfUrl("Error: The score must be at least 70")
                        .build());
            }else{
                //to generate certificate for approved people
                String pdfUrl = "http://localhost:8090/pdfs/" + enrollmentId + ".pdf";
                responseObserver.onNext(Certificate.newBuilder()
                        .setPdfUrl(pdfUrl)
                        .build());
            }
            responseObserver.onCompleted();
        }

    }
}