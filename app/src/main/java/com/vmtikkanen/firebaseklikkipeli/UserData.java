package com.vmtikkanen.firebaseklikkipeli;

public class UserData {

   // String userName;
    int currentPoints;
    int rewards500;
    int rewards100;
    int rewards10;

    public UserData(int currentPoints) {
        this.currentPoints = currentPoints;
    }

    public UserData() {
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
    }
}
