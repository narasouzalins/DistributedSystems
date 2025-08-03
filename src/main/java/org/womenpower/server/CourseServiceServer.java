package org.womenpower.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.courses.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
/*
 * Main server application for the Course Service.
 * It manages course listings, user enrollments, and certificate generation.
 */
public class CourseServiceServer { //method to initiate the server
    //simulate a database for courses
    private static final ConcurrentHashMap<String, Course> availableCourses = new ConcurrentHashMap<>();

    //database simulating enrollment
    private static final ConcurrentHashMap<String, EnrollmentData> enrollments = new ConcurrentHashMap<>();

    static class EnrollmentData {
        String enrollmentId;
        String userId;
        String courseId;
        boolean isCompleted;

        public EnrollmentData(String enrollmentId, String userId, String courseId) {
            this.enrollmentId = enrollmentId;
            this.userId = userId;
            this.courseId = courseId;
            this.isCompleted = isCompleted;
        }

        public boolean isCompleted() {
            return isCompleted;
        }
        public void setCompleted(boolean isCompleted) {
            this.isCompleted = isCompleted;
        }
        public String getCourseId() {
            return courseId;
        }
        public String getUserId() {
            return userId;
        }
    }// Initializes a set of mock courses for the server to offer.
    public static void main(String[] args) throws Exception {
        initializeAvailableCourses();

        Server sv = ServerBuilder.forPort(8091)
                .addService(new CourseEnrollmentImpl()) //to register implementation
                .build();
        System.out.println( "Starting Course Service Server");
        System.out.println("Listening on port 8091");

        sv.start(); //to initiate server
        sv.awaitTermination();  //keep the server running
    }
            //method to initialize the courses
    private static void initializeAvailableCourses(){
        availableCourses.put("java_intro", Course.newBuilder()
                .setCourseId("java_intro")
                .setTitle("Introduction to Java")
                .setDescription("Oriented Object Programming.")
                .setLecturer("Dra. Mary Hues")
                .build());
        availableCourses.put("web_dev", Course.newBuilder()
                .setCourseId("web_dev")
                .setTitle(" Fullstack Web Development")
                .setDescription("HTML, CSS, JavaScript, React and Node.js.")
                .setLecturer("Prof. Carlos Mendes")
                .build());
        availableCourses.put("ml_basics", Course.newBuilder()
                .setCourseId("ml_basics")
                .setTitle("Machine Learning Basics")
                .setDescription("IA, algorithms and models.")
                .setLecturer("Dr. George Smith")
                .build());
        System.out.println("Initialized " + availableCourses.size() + " courses.");
    }
    //course service impl
    static class CourseEnrollmentImpl extends CourseServiceGrpc.CourseServiceImplBase {

        //Here I list all courses
        @Override
        public void listCourses(ListCoursesRequest request, StreamObserver<CourseList> responseObserver){
            CourseList.Builder responseBuilder = CourseList.newBuilder();

            if(availableCourses.isEmpty()){
                responseBuilder.setSuccess(false).setMessage("No courses available.");
            }else{
                for(Course course : availableCourses.values()){
                    responseBuilder.addCourses(course);
                }
                responseBuilder.setSuccess(true).setMessage("Available courses listed");
                System.out.println("Sent " + availableCourses.size() + " courses.");
            }
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }

        //Method that enroll a user to a course and save on user information
        @Override
        public void enrollCourse(CourseEnrollment request, StreamObserver<EnrollmentStatus> responseObserver) {

            String userId = request.getUserId();
            String courseId = request.getCourseId();
            String enrollmentId = "ID" + UUID.randomUUID().toString().substring(0, 8); //unique id
            String courseLink = " ";
            enrollments.put(enrollmentId, new EnrollmentData(enrollmentId, userId, courseId));
            System.out.println("Enrollment created with ID: " + enrollmentId);
            System.out.println("Enrolling user " + userId + " in course " + courseId);

            // to validate user
            if (userId.isEmpty() || courseId.isEmpty()) {
                responseObserver.onNext(EnrollmentStatus.newBuilder()
                        .setSuccess(false)
                        .setEnrollmentId("")
                        .setCourseLink("")
                        .setMessage("Error: User ID and Course ID are required.")
                        .build());
                responseObserver.onCompleted();
                System.out.println("Enrollment failed: User ID or Course ID is empty.");
                return;
            }
            responseObserver.onNext(EnrollmentStatus.newBuilder()
                    .setSuccess(true)
                    .setEnrollmentId(enrollmentId)
                    .setCourseLink(courseId)
                    .setMessage("Enrollment successful.")
                    .build());
            responseObserver.onCompleted();
        }


        //Generate a mock Certificate
        @Override
        public void generateCertificate(CertificateRequest request, StreamObserver<Certificate> responseObserver) {
            String enrollmentId = request.getEnrollmentId();

            System.out.println("Generating certificate for enrollment ID: " + enrollmentId);

            //validation
            if(enrollmentId.isEmpty()) {
                responseObserver.onNext(Certificate.newBuilder()
                        .setSuccess(false)
                        .setPdfUrl(" ")
                        .setMessage("Error: Enrollment ID is required.")
                        .build());
                responseObserver.onCompleted();
                return;
            }
            //verify if the course is completed
            EnrollmentData enrollmentData = enrollments.get(enrollmentId);
            if (enrollmentData == null) {
                responseObserver.onNext(Certificate.newBuilder()
                        .setSuccess(false)
                        .setPdfUrl("")
                        .setMessage("Error: Enrollment ID not found.")
                        .build());
                responseObserver.onCompleted();
                return;
            }
            //use as a test the user completed the course
            enrollmentData.setCompleted(true);

            if(!enrollmentData.isCompleted()) {
                responseObserver.onNext(Certificate.newBuilder()
                        .setSuccess(false)
                        .setPdfUrl("")
                        .setMessage("Error: Enrollment ID not completed.")
                        .build());
                responseObserver.onCompleted();
                return;
            }
            String pdfUrl = "http://localhost:8091/certificates/" + UUID.randomUUID().toString() + ".pdf";
            responseObserver.onNext(Certificate.newBuilder()
                    .setSuccess(true)
                    .setPdfUrl(pdfUrl)
                    .setMessage("Certificate generated successfully! PDF available at: " + pdfUrl)
                    .build());
            responseObserver.onCompleted();
            System.out.println("Certificate generated for enrollment ID: " + enrollmentId);
        }
    }
}