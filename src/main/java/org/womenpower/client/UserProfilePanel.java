package org.womenpower.client;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.womenpower.skillselection.AvailableSkill;
import org.womenpower.skillselection.AvailableSkillList;
import org.womenpower.skillselection.ListAvailableSkillsRequest;
import org.womenpower.skillselection.SkillSelectionServiceGrpc;
import org.womenpower.userprofile.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class UserProfilePanel extends JPanel implements ActionListener {

    private UserProfileServiceGrpc.UserProfileServiceBlockingStub userProfileServiceBlockingStub;

    private SkillSelectionServiceGrpc.SkillSelectionServiceBlockingStub skillSelectionBlockingStub;


                //Register session
    private JTextField nameField, emailField, professionField;  //field to data entry
    private JButton registerButton;                             // button to send register
    private JTextArea registerResultArea;                       // to show the register result

                //Skills session
    private JTextField userIdSkillField;        //
    private JButton getSkillsButton;            //
    private JTextArea skillResultArea;
    private JComboBox<String> skillsDropdown;
    private Map<String, String> skillNameToIdMap;

    //constructor
    public UserProfilePanel(ManagedChannel channel){
        this.userProfileServiceBlockingStub = UserProfileServiceGrpc.newBlockingStub(channel);
        this.skillSelectionBlockingStub = SkillSelectionServiceGrpc.newBlockingStub(channel);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10)); //add borders around the panel

                    //REGISTER USER SECTION
        JPanel registerSection = new JPanel();   //sub panel to group the register elements
        registerSection.setLayout(new BoxLayout(registerSection, BoxLayout.Y_AXIS));
        registerSection.setBorder(BorderFactory.createTitledBorder("Register new user profile"));

        nameField = new JTextField(15);
        emailField = new JTextField(15);
        professionField = new JTextField(15);
        skillsDropdown = new JComboBox<>();
        skillNameToIdMap = new HashMap<>();

        // method to add label fields
        registerSection.add(createLabelField("Name: ", nameField));
        registerSection.add(createLabelField("Email: ", emailField));
        registerSection.add(createLabelField("Profession: ", professionField));
        registerSection.add(createLabelField("Select Skill: ", skillsDropdown));

        registerButton = new JButton("Register user");
        registerButton.addActionListener(this);
        registerSection.add(registerButton);

        registerResultArea = new JTextArea(3, 15);  //
        registerResultArea.setEditable(false);                    //avoid users edition
        registerResultArea.setLineWrap(true);                     //
        registerResultArea.setWrapStyleWord(true);
        JScrollPane scrollPanelRegister = new JScrollPane(registerResultArea);   //add scroll bar
        registerSection.add(scrollPanelRegister);

        add(registerSection);                                     //add
        add(Box.createVerticalStrut(20));                          // add

        //SKILL SECTION
        JPanel skillSection = new JPanel();  //create a sub panel for sills
        skillSection.setLayout(new BoxLayout(skillSection, BoxLayout.Y_AXIS));
        skillSection.setBorder(BorderFactory.createTitledBorder("Skills"));

        userIdSkillField = new JTextField(15);
        skillSection.add(createLabelField("User ID", userIdSkillField));

        //button to get skills
        getSkillsButton = new JButton("Get Skills");
        getSkillsButton.addActionListener(this);
        skillSection.add(getSkillsButton);

        //to show skills
        skillResultArea = new JTextArea();
        skillResultArea.setEditable(false);
        skillResultArea.setLineWrap(true);
        skillResultArea.setWrapStyleWord(true);
        JScrollPane scrollPanelSkill = new JScrollPane(skillResultArea);
        skillSection.add(scrollPanelSkill);
        add(skillSection);

        loadAvailableSkills();
    }

    private JPanel createLabelField(String labelText, JComponent component){
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));     //to
        panel.add(new JLabel(labelText));
        panel.add(component);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == registerButton){
            registerUser();
        }else if (e.getSource() == getSkillsButton){
           getUSerSkills();
        }
    }
    //method to load the skills from skills selection service
    private void loadAvailableSkills(){
        skillsDropdown.removeAllItems(); //clean
        skillNameToIdMap.clear(); //clean map

        ListAvailableSkillsRequest request = ListAvailableSkillsRequest.newBuilder().build();
        try{
            AvailableSkillList response = skillSelectionBlockingStub.listAvailableSkills(request);

            SwingUtilities.invokeLater(() ->{
            for(AvailableSkill skill : response.getSkillsList()){
                skillsDropdown.addItem(skill.getName()); //add name to dropdown
                skillNameToIdMap.put(skill.getName(), skill.getId());
            }
            registerResultArea.append("Skills loaded successfully\n");

            skillsDropdown.revalidate();
            skillsDropdown.repaint();

            SwingUtilities.getWindowAncestor(this).revalidate();
            SwingUtilities.getWindowAncestor(this).repaint();
            });

        }catch (StatusRuntimeException e){
            System.err.println("Error loading skills:  " + e.getMessage()); // Log de erro
            SwingUtilities.invokeLater(() -> {
                registerResultArea.append("Error loading skills: " + e.getMessage() + "\n");
            });
        }
    }
        //CALL gRPC registerUser()
    private void registerUser(){
        String name = nameField.getText();
        String email = emailField.getText();
        String profession = professionField.getText();
        String selectedSkillName = (String) skillsDropdown.getSelectedItem();
        String selectedSkillId = null;
            if (selectedSkillName != null) {
                selectedSkillId = skillNameToIdMap.get(selectedSkillName);
        }
            if(name.isEmpty() || email.isEmpty() || profession.isEmpty() || selectedSkillId == null){
                registerResultArea.setText("Error: Name, email, profession and skill are required");
                return;
            }
            try{
                RegisterRequest.Builder requestBuilder = RegisterRequest.newBuilder()
                        .setUsername(name)
                        .setEmail(email)
                        .setProfession(profession);

                //add skills if so
                if(selectedSkillId != null){
                    requestBuilder.addSelectedSkillIds(selectedSkillId);
                }
                RegisterResponse response = userProfileServiceBlockingStub.registerUser(requestBuilder.build());

                if(response.getSuccess()){
                    registerResultArea.setText("User successfully registered " + response.getUserId());
                    nameField.setText("");
                    emailField.setText("");
                    professionField.setText("");
                    skillsDropdown.setSelectedIndex(-1);
                }else{
                    registerResultArea.setText("Error registering user: " + response.getMessage());
                }
            }catch (StatusRuntimeException e){
                registerResultArea.setText("Error registering user (gRPC issue): " + e.getMessage());
                System.err.println("gRPC error during user registration: " + e.getStatus() + " - " + e.getMessage());
                e.printStackTrace();
            }
    }
    //CALL gRPC getUSerSkills
    private void getUSerSkills(){
        String userId = userIdSkillField.getText();

        if(userId.isEmpty()){
            skillResultArea.setText("Error: User ID is required");
            return;
        }
        try{
            GetUserSkillsRequest request = GetUserSkillsRequest.newBuilder()
                    .setUserId(userId)
                    .build();
            GetUserSkillsResponse response = userProfileServiceBlockingStub.getUserSkills(request);

            StringBuilder sb = new StringBuilder("Skills for user ID: " + userId + ":\n");

            if (response.getSuccess()) {
                if (response.getSkillsList().isEmpty()) {
                    sb.append("No skills found.\n");
                } else {
                    for (UserSkill skill : response.getSkillsList()) {
                        sb.append("- ").append(skill.getName()).append(" (ID: ").append(skill.getId()).append(")\n");
                    }
                }
            } else {
                sb.append("Error retrieving skills: ").append(response.getMessage()).append("\n");
            }
            skillResultArea.setText(sb.toString());

        }catch(StatusRuntimeException ex){
            System.err.println("Error getting skills (gRPC issue): " + ex.getStatus() + " - " + ex.getMessage());
            skillResultArea.setText("Error getting skills: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

