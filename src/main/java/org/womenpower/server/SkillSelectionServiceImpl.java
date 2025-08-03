package org.womenpower.server;

import io.grpc.stub.StreamObserver;
import org.womenpower.skillselection.AvailableSkill;
import org.womenpower.skillselection.AvailableSkillList;
import org.womenpower.skillselection.ListAvailableSkillsRequest;
import org.womenpower.skillselection.SkillSelectionServiceGrpc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SkillSelectionServiceImpl extends SkillSelectionServiceGrpc.SkillSelectionServiceImplBase {

    private static final Map<String, AvailableSkill> availableSkills;


    //Mock Database of available skills
    static{
        availableSkills = new HashMap<>();
        availableSkills.put("java", AvailableSkill.newBuilder().setId("java").setName(" Java").build());
        availableSkills.put("python", AvailableSkill.newBuilder().setId("python").setName("Python").build());
        availableSkills.put("agile", AvailableSkill.newBuilder().setId("agile").setName("Agile").build());
        availableSkills.put("frontend", AvailableSkill.newBuilder().setId("frontend").setName("Frontend Development").build());
        availableSkills.put("backend", AvailableSkill.newBuilder().setId("backend").setName("Backend Development").build());
        availableSkills.put("cloud", AvailableSkill.newBuilder().setId("cloud").setName("Cloud Compute").build());
        System.out.println("SkillSelectionServiceImpl");
    }
    public static Map<String, AvailableSkill> getAvailableSkillMap(){
        return Collections.unmodifiableMap(availableSkills);
    }

    //Method that allow user to fetch availables skills
    @Override
    public void listAvailableSkills(ListAvailableSkillsRequest request, StreamObserver<AvailableSkillList> responseObserver) {
        System.out.println("Request received for listing available skills");
        AvailableSkillList.Builder responseBuilder = AvailableSkillList.newBuilder();
        for (AvailableSkill skill : availableSkills.values()) {
            responseBuilder.addSkills(skill);
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
        System.out.println("List of " + availableSkills.size() + "skills sent.");
    }
}