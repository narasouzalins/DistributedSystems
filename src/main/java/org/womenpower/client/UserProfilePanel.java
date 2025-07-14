package org.womenpower.client;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.womenpower.userprofile.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

public class UserProfilePanel extends JPanel implements ActionListener {

    private UserProfileServiceGrpc.UserProfileServiceBlockingStub blockingStub;

                //Register session
    private JTextField nameField, emailField, professionField;  //field to data entry
    private JButton registerButton;                             // button to send register
    private JTextArea registerResultArea;                       // to show the register result

                //Skills session
    private JTextField userIdSkillField;        //
    private JButton getSkillsButton;            //
    private JTextArea skillResultArea;

    //constructor
    public UserProfilePanel(ManagedChannel channel){
        this.blockingStub = UserProfileServiceGrpc.newBlockingStub(channel);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10)); //add borders around the panel

                    //REGISTER USER SECTION
        JPanel registerSection = new JPanel();   //sub panel to group the register elements
        registerSection.setLayout(new BoxLayout(registerSection, BoxLayout.Y_AXIS));
        registerSection.setBorder(BorderFactory.createTitledBorder("Register new user profile"));

        nameField = new JTextField(15);
        emailField = new JTextField(15);
        professionField = new JTextField(15);

        //auxiliar method to add label fields
        registerSection.add(createLabelField("Name: ", nameField));
        registerSection.add(createLabelField("Email: ", emailField));
        registerSection.add(createLabelField("Profession: ", professionField));

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
    }

    private JPanel createLabelField(String labelText, JTextField textField){
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));     //to
        panel.add(new JLabel(labelText));
        panel.add(textField);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == registerButton){
            registerUser();
        }else if (e.getSource() == getSkillsButton){
           getSkillsForUser();
        }
    }
        //CALL gRPC registerUser()
    private void registerUser(){
        String name = nameField.getText();
        String email = emailField.getText();
        String profession = professionField.getText();

        if(name.isEmpty() || email.isEmpty()){
            registerResultArea.setText("Error: Name and email are required");
            return;
        }

        try {
            RegisterRequest request = RegisterRequest.newBuilder()
                    .setName(name)
                    .setEmail(email)
                    .setProfession(profession)
                    .build();
            RegisterResponse response = blockingStub.registerUser(request);

            if(!response.getUserId().isEmpty()){
                registerResultArea.setText("User successfully registered" + response.getUserId());
                nameField.setText("");
                emailField.setText("");
                professionField.setText("");
            } else {
                registerResultArea.setText("Error: Username and email are required");
            }
        } catch (Exception e) {
            registerResultArea.setText("Error: " + e.getMessage());
            System.out.println("Error registering user: " + e.getMessage());
            return;
        }
    }
    //CALL gRPC getSkillsForUser()
    private void getSkillsForUser(){
        String userId = userIdSkillField.getText();

        if(userId.isEmpty()){
            skillResultArea.setText("Error: User ID is required");
            return;
        }

        try{
            UserRequest request = UserRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            Iterator<Skill> skillIterator = blockingStub.getUserSkills(request);

            StringBuilder sb = new StringBuilder("Skills for user ID: " + userId + ":\n");
            boolean foundSkills = false;
            while(skillIterator.hasNext()){
                Skill skill = skillIterator.next();
                sb.append(skill.getName()).append("Level: ").append(skill.getLevel()).append("\n");
                foundSkills = true;
            }
            if(!foundSkills){
                sb.append("No skills found.\n");
            }
            skillResultArea.setText(sb.toString());

        }catch(StatusRuntimeException ex){
            skillResultArea.setText("Unexpected Error " + ex.getMessage());
            System.out.println("Unexpected Error getting skills: " + ex.getMessage());
        }
    }
}

