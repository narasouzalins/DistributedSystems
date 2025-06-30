import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.userprofile.*;

public class UserProfileServer {
    public static void main(String[] args) {
        Server server = ServerBuilder.forPort(8090).addService(new UserRegisterServiceImpl()).build();
        System.out.println("Starting User Profile Server");
        System.out.println("Listening on port 8090");
    }

    static class UserRegisterServiceImpl extends UserProfileServiceGrpc.UserProfileServiceImplBase {


        @Override
        public void registerUser(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
            super.registerUser(request, responseObserver);


            //TODO: implementar registerUser
            System.out.println("Registering user");

            responseObserver.onNext(RegisterResponse.newBuilder().setUserId("NARA")
                    .setSuccess(true)
                    .build());
            responseObserver.onCompleted();

        }
            //TODO: implement
        @Override
        public void getUserSkills(UserRequest request, StreamObserver<Skill> responseObserver) {
            super.getUserSkills(request, responseObserver);
        }
    }
}
