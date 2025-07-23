import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.womenpower.userprofile.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        private List<Skill> skills;

        public UserProfileData(String id, String name, String email, String profession) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.profession = profession;
            this.skills = new ArrayList<Skill>();
        }

        public String getId() { return id;}
        public String getName() { return name;}
        public String getEmail() { return email; }
        public String getProfession() { return profession; }
        public List<Skill> getSkills() { return skills;}
        public void addSkill(Skill s) {skills.add(s);}
    }

    static class UserRegisterServiceImpl extends UserProfileServiceGrpc.UserProfileServiceImplBase {
        //to storage users
        private final ConcurrentHashMap<String, UserProfileData> userStorage = new ConcurrentHashMap<>();

        //email format validation
        private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}$";
        private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

        @Override
        public void registerUser(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {

            String uuidPart = String.valueOf(Math.abs(UUID.randomUUID().getLeastSignificantBits())).substring(0, Math.min(8, String.valueOf(Math.abs(UUID.randomUUID().getLeastSignificantBits())).length()));
            String newUserId = "ID" + uuidPart;  // to generate an unique id for user
            String userName = request.getName();
            String userEmail = request.getEmail();
            String userProfession = request.getProfession();

            System.out.println("Registering user: " + userName);

            if( userName.isEmpty() || userEmail.isEmpty() || userProfession.isEmpty() ) {
                responseObserver.onNext(RegisterResponse.newBuilder()
                        .setUserId("")
                        .build());
                responseObserver.onCompleted();
                System.out.println("Error: Username or email address required");
                return;
            }
                //email format validation
            Matcher matcher = EMAIL_PATTERN.matcher(userEmail);
            if (!matcher.matches()) {
                responseObserver.onNext(RegisterResponse.newBuilder()
                        .setUserId("") // Indicates failure
                        .build());
                responseObserver.onCompleted();
                System.out.println("Registration failed: Invalid email format for " + userEmail);
                return; // Exit the method if email format is invalid
            }

            UserProfileData userData = new UserProfileData(newUserId, userName, userEmail, userProfession);

            userData.addSkill(Skill.newBuilder().setName("Communication").setLevel("Beginner").build());
            userData.addSkill(Skill.newBuilder().setName("JavaScript").setLevel("Advanced").build());
            userStorage.put(newUserId, userData);

            RegisterResponse response = RegisterResponse.newBuilder()
                    .setUserId(newUserId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            System.out.println("Registered user: " + newUserId);
        }

        @Override
        public void getUserSkills(UserRequest request, StreamObserver<Skill> responseObserver) {
          String userId = request.getUserId();
          UserProfileData userData = userStorage.get(userId);

                    //validation: if the user not found
          if( userData == null ) {
              responseObserver.onError(Status.NOT_FOUND
              .withDescription("User not found")
                      .asRuntimeException());
              return;
          }

          List<Skill> skills = userData.getSkills();
          if (skills.isEmpty()) {
              System.out.println("User " + userId + " has no skills");
          } else {
              System.out.println("User " + userId + " has " + skills.size() + " skills. Sending now...");
              for (Skill skill : skills) {
                  responseObserver.onNext(skill);
              }
          }
          responseObserver.onCompleted();
        }
    }
}
