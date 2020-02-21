package com.vmtikkanen.firebaseklikkipeli;

public class GameData {

    private String userName;
    private boolean rewardCollected;

    public GameData(String userName, boolean rewardCollected) {
        this.userName = userName;
        this.rewardCollected = rewardCollected;
    }

    public GameData() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isRewardCollected() {
        return rewardCollected;
    }

    public void setRewardCollected(boolean rewardCollected) {
        this.rewardCollected = rewardCollected;
    }
}
