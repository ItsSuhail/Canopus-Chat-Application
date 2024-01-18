package com.canopus.chatapp;

public class StarModel {
    private String name;
    private String password;
    private String members;
    private String membersEmail;
    private String chats;
    private String adminUser;

    public StarModel(String name, String password, String members, String chats, String adminUser, String membersEmail){
        this.name = name;
        this.password = password;
        this.members = members;
        this.chats = chats;
        this.adminUser = adminUser;
        this.membersEmail = membersEmail;
    }

    public StarModel(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getChats() {
        return chats;
    }

    public void setChats(String chats) {
        this.chats = chats;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getMembersEmail() {
        return membersEmail;
    }

    public void setMembersEmail(String membersEmail) {
        this.membersEmail = membersEmail;
    }
}
