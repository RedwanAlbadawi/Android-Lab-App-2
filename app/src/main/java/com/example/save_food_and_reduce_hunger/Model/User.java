package com.example.save_food_and_reduce_hunger.Model;




// User POJO to store user information
public class User{
    private String name;
    private String phone;
    private String role;

    public User(){

    }

    public User(String name, String phone, String role) {
        this.name = name;
        this.phone = phone;
        this.role = role;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
