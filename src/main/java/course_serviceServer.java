import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.courses.*;

public class course_serviceServer {
    public static void main(String[] args) {
        Server sv = ServerBuilder.forPort(8091).addService(new CourseEnrollmentImpl()).build();
        System.out.println( "Starting Course Service Server");
    }

    static class CourseEnrollmentImpl extends CourseServiceGrpc.CourseServiceImplBase {

        //TODO implement
        @Override
        public void enrollCourse(CourseEnrollment request, StreamObserver<EnrollmentStatus> responseObserver) {
            super.enrollCourse(request, responseObserver);
        }

        //TODO implment

        @Override
        public void generateCertificate(CertificateRequest request, StreamObserver<Certificate> responseObserver) {
            super.generateCertificate(request, responseObserver);
        }
    }
}
