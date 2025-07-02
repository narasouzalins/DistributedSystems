import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.userprofile.*;
import java.util.UUID;

public class UserProfileServer {
    public static void main(String[] args) {
        Server server = ServerBuilder.forPort(8090).addService(new UserRegisterServiceImpl()).build();
        System.out.println("Starting User Profile Server");
        System.out.println("Listening on port 8090");
    }
    static class UserProfileData {
        String id;
        String name;
        String email;
        String profession;

        public UserProfileData(String id, String name, String email, String profession) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.profession = profession;
        }
    }
    static class UserRegisterServiceImpl extends UserProfileServiceGrpc.UserProfileServiceImplBase {
        String newUserId = UUID.randomUUID().toString();  // to generate an unique id for use

        @Override
        public void registerUser(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
            super.registerUser(request, responseObserver);

            String userName = request.getName();
            String userEmail = request.getEmail();
            String userProfession = request.getProfession();
            System.out.println("Registering user: " + userName);

            UserProfileData userData = new UserProfileData(newUserId, userName, userEmail, userProfession);
            responseObserver.onNext(RegisterResponse.newBuilder().setUserId("NARA")
            ;        .setSuccess(true)
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
