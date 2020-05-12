package com.example.save_food_and_reduce_hunger.Model;
import java.io.Serializable;

import lombok.Data;


/* Donation model to store the donation information such as donor name,location,donation image,
donation quantity etc..
 */
public class Donation implements Serializable {
    private String quantity,imageUrl;
    private Double lat,lng;
    private boolean fastFood,fruits,meals;
    private String donorName,donorEmail,donorPhone;

    public Donation(){

    }

    public Donation(String quantity, String imageUrl, Double lat, Double lng, boolean fastFood, boolean fruits, boolean meals, String donorName, String donorEmail, String donorPhone) {
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.lat = lat;
        this.lng = lng;
        this.fastFood = fastFood;
        this.fruits = fruits;
        this.meals = meals;
        this.donorName = donorName;
        this.donorEmail = donorEmail;
        this.donorPhone = donorPhone;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public boolean isFastFood() {
        return fastFood;
    }

    public void setFastFood(boolean fastFood) {
        this.fastFood = fastFood;
    }

    public boolean isFruits() {
        return fruits;
    }

    public void setFruits(boolean fruits) {
        this.fruits = fruits;
    }

    public boolean isMeals() {
        return meals;
    }

    public void setMeals(boolean meals) {
        this.meals = meals;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorEmail() {
        return donorEmail;
    }

    public void setDonorEmail(String donorEmail) {
        this.donorEmail = donorEmail;
    }

    public String getDonorPhone() {
        return donorPhone;
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }
}
