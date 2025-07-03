import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.womenpower.userprofile.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserProfileServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(8090)
                .addService(new UserRegisterServiceImpl())
                .build();
        server.start();
        System.out.println("Starting User Profile Server");
        System.out.println("Listening on port 8090");
        server.awaitTermination();
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
        //to storage users
        private final ConcurrentHashMap<String, UserProfileData> userStorage = new ConcurrentHashMap<>();

        @Override
        public void registerUser(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {

            String newUserId = UUID.randomUUID().toString();  // to generate an unique id for user
            String userName = request.getName();
            String userEmail = request.getEmail();
            String userProfession = request.getProfession();

            System.out.println("Registering user: " + userName);

            UserProfileData userData = new UserProfileData(newUserId, userName, userEmail, userProfession);
            userStorage.put(newUserId, userData);

            RegisterResponse response = RegisterResponse.newBuilder()
                    .setUserId(newUserId)  // Use the generated ID
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getUserSkills(UserRequest request, StreamObserver<Skill> responseObserver) {
           responseObserver.onNext(Skill.newBuilder()
                   .setName("HTML")
                   .setLevel("Basic")
           .build());

           responseObserver.onCompleted();
        }
    }
}
