package org.womenpower.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.womenpower.skillselection.AvailableSkill;
import org.womenpower.userprofile.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserProfileServer {
    private static final ConcurrentHashMap<String, UserProfileData> userStorage = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(8090)
                .addService(new UserProfileServiceImpl(userStorage))
                .addService(new SkillSelectionServiceImpl())
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
        private List<String> skills;

        public UserProfileData(String id, String name, String email, String profession) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.profession = profession;
            this.skills = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getProfession() {
            return profession;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void addSkill(String s) {
            skills.add(s);
        }
    }

    static class UserProfileServiceImpl extends UserProfileServiceGrpc.UserProfileServiceImplBase {

        private final ConcurrentHashMap<String, UserProfileData> userStorage;

        public UserProfileServiceImpl(ConcurrentHashMap<String, UserProfileData> userStorage) {
            this.userStorage = userStorage;
        }

        //email format validation
        private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}$";
        private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

        @Override
        public void registerUser(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {

            String uuidPart = String.valueOf(Math.abs(UUID.randomUUID().getLeastSignificantBits())).substring(0, Math.min(8, String.valueOf(Math.abs(UUID.randomUUID().getLeastSignificantBits())).length()));
            String newUserId = "ID" + uuidPart;  // to generate an unique id for user
            String userName = request.getUsername();
            String userEmail = request.getEmail();
            String userProfession = request.getProfession();
            List<String> selectedSkillIds = request.getSelectedSkillIdsList();

            System.out.println("Registering user: " + userName);

            if (userName.isEmpty() || userEmail.isEmpty() || userProfession.isEmpty()) {
                System.out.println("Validation failed: Username, email, or profession is empty."); // Log mais específico
                responseObserver.onNext(RegisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Username, email, and profession are required.")
                        .setUserId("")
                        .build());
                responseObserver.onCompleted();
                return;
            }
            //email format validation
            Matcher matcher = EMAIL_PATTERN.matcher(userEmail);
            if (!matcher.matches()) {
                System.out.println("Validation failed: Invalid email format for " + userEmail); // Log mais específico
                responseObserver.onNext(RegisterResponse.newBuilder()
                        .setSuccess(false) //indicate failure
                        .setMessage("Invalid email format.") //error message to user
                        .setUserId("")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            UserProfileData userData = new UserProfileData(newUserId, userName, userEmail, userProfession);

            for (String skillId : selectedSkillIds) {
                userData.addSkill(skillId);
            }
            userStorage.put(newUserId, userData);

            RegisterResponse response = RegisterResponse.newBuilder()
                    .setSuccess(true)
                    .setUserId(newUserId)
                    .setMessage("User registered successfully. ID: " + newUserId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            System.out.println("Registered user: " + newUserId);
        }

        //get skills method
        @Override
        public void getUserSkills(GetUserSkillsRequest request, StreamObserver<GetUserSkillsResponse> responseObserver) {
            String userId = request.getUserId();
            UserProfileData userData = userStorage.get(userId);
            GetUserSkillsResponse.Builder responseBuilder = GetUserSkillsResponse.newBuilder();

            if (userData == null) {
                responseObserver.onNext(GetUserSkillsResponse.newBuilder()
                                .setSuccess(false)
                                .setMessage("User not found: " + userId)
                                .build());
                responseObserver.onCompleted();
                System.out.println("Error: User not found for ID: " + userId);
                return;
            }
            List<String> skills = userData.getSkills();//to get id skills
            System.out.println("Skills for user " + userId + ": " + skills);

            boolean isSuccess = true; //flag to verify all skills are valid
            for (String skillId : skills) {
                AvailableSkill availableSkill = SkillSelectionServiceImpl.getAvailableSkillMap().get(skillId);

                if (availableSkill != null) {
                    // ***** THIS IS THE CORRECTED BLOCK *****
                    UserSkill.Builder userSkillBuilder = UserSkill.newBuilder() // Start a new UserSkill builder
                            .setId(skillId)                                     // Set ID on this builder
                            .setName(availableSkill.getName());                 // Set Name on this builder
                    responseBuilder.addSkills(userSkillBuilder.build());        // Build the UserSkill and then add it to the response builder
                } else {
                    //if the skills is not valid
                    System.err.println("Warning: Skill ID " + skillId + " for user " + userId + " not found in available skills.");
                    isSuccess = false;
                }
            }
            responseBuilder.setSuccess(isSuccess);
            if (!isSuccess) {
                responseBuilder.setMessage("One or more skills are invalid.");
            } else if (skills.isEmpty()) {
                responseBuilder.setMessage("No skills selected for this user.");
            }else{
                responseBuilder.setMessage("Skills retrieved successfully.");
            }
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            System.out.println("Skills retrieved for user " + userId);
        }
    }
}
