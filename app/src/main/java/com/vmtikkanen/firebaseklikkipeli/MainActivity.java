package com.vmtikkanen.firebaseklikkipeli;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private int startingPoints = 20;
    private long currentValue;
    private TextView nextReward_text;
    private TextView clicksLeft_text;
    private TextView playerName_text;
    private DatabaseReference gameDataRef;
    private DatabaseReference userDataRef;
    private DatabaseReference userDataUserNameRef;
    private static String currentUser;
    private String currentPoints;
    private String clicker;
    private String rewardCollected;
    private ImageView rewardView1, rewardView2, rewardView3;
    private int lastReward;
    private static DataSnapshot newSnapshot;
    private ValueEventListener userDataListener;
    private ValueEventListener gameDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GameData gameData = new GameData();
        UserData userData = new UserData();
        gameDataRef = FirebaseDatabase.getInstance().getReference().child("gamedata");
        userDataRef = FirebaseDatabase.getInstance().getReference().child("userdata");
        userDataUserNameRef = FirebaseDatabase.getInstance().getReference().child("userdata").child(currentUser);

        rewardView1 = findViewById(R.id.rewardView);
        rewardView2 = findViewById(R.id.rewardView2);
        rewardView3 = findViewById(R.id.rewardView3);

        nextReward_text = findViewById(R.id.nextRewardTextView);
        clicksLeft_text = findViewById(R.id.clicksLeft_text);
        playerName_text = findViewById(R.id.playerNameTextView);

        Button clickButton = findViewById(R.id.button);

        playerName_text.setText("Player name: " +currentUser);

        userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    userDataRef.child(currentUser).child("clicksLeft").setValue(startingPoints); //register classiin??
                }
                try {
                    currentPoints = dataSnapshot.child("clicksLeft").getValue().toString();
                    clicksLeft_text.setText(currentPoints);
                }catch (NullPointerException e){
                    Log.d(TAG, "onDataChange: " + e.getStackTrace());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        gameDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    try{
                        currentValue = (dataSnapshot.getChildrenCount());
                        clicker = dataSnapshot.child(String.valueOf(currentValue)).child("userName").getValue().toString();
                        setSnapshot(dataSnapshot);
                        notifyDataChange();
                    }catch (NullPointerException e){
                        Log.d(TAG, "onDataChange: " + e.getStackTrace());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };



        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.valueOf(currentPoints) > 0 ){
                    currentPoints = String.valueOf(Integer.valueOf(currentPoints)-1);
                    gameData.setUserName(currentUser); //eikai tätä tarvii käyttää jokaisessa klikissä?
                    gameData.setRewardCollected(false);
                    gameDataRef.child(String.valueOf(currentValue+1)).setValue(gameData);
                    userDataRef.child(currentUser).child("clicksLeft").setValue(Integer.valueOf(currentPoints));
                }else{
                    showNewGameDialog();
                }
            }
        });
    }

    private void showNewGameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("New game?")
                .setMessage("Game over! you lost all your points. Do you want to start over?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userDataRef.child(currentUser).child("clicksLeft").setValue(20);
                        Log.d(TAG, "onClick: 20 points added");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: clicked cancel");
                    }
                });
        builder.show();
    }


    private void isCurrentValueWinning(){
        Log.d(TAG, "isCurrentValueWinning: called with currentValue " + currentValue);
        lastReward = (int) (currentValue / 10) * 10;
        Log.d(TAG, "last reward was at: #" + lastReward);
        if((lastReward % 500) == 0){
            Log.d(TAG, "isCurrentValueWinning: 500 found");
            checkForWinner(lastReward, 250);
        }else if((lastReward % 100) == 0){
            Log.d(TAG, "isCurrentValueWinning: 100 found");
            checkForWinner(lastReward, 40);
        }else if((lastReward % 10) == 0){
            checkForWinner(lastReward, 5);
        }
    }


    private void setNextRewardText(){
        if(currentValue%10 != 0){
            long nextReward = 10 - (currentValue%10);
            nextReward_text.setText("Next reward after " + nextReward + " clicks");
        }else{
            nextReward_text.setText(("Next reward after 10 clicks"));
        }
    }


    public void checkForWinner(final int value, int rewardAmount) {

        Log.d(TAG, "checkForWinner: current value " + currentValue);
        try {
            rewardCollected = newSnapshot.child(String.valueOf(value)).child("rewardCollected").getValue().toString();
            Log.d(TAG, "checkForWinner: rewardCollected = " + rewardCollected + " clicker = " + clicker + " value = " +value);
            if (clicker.equals(currentUser) && rewardCollected.equals("false")) {

                Log.d(TAG, "checkForWinner: You won a price for the" + value + "th click! ");
                gameDataRef.child(String.valueOf(value)).child("rewardCollected").setValue(true);
                userDataRef.child(currentUser).child("clicksLeft").setValue(Integer.valueOf(currentPoints)+ rewardAmount );
                Log.d(TAG, "checkForWinner: Congratulations, you won " + rewardAmount + " points for the " + value + "th click!");
                Toast.makeText(this, "Congratulations, you won " + rewardAmount + " points for the " + value + "th click!", Toast.LENGTH_LONG).show();

            } else if (rewardCollected.equals("false")) {
                Log.d(TAG, "checkForWinner: SOMEONE ELSE WON #" + value + " :(!");
            } else {
                Log.d(TAG, "checkForWinner: Reward has already been collected for #" + value);
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "checkForWinner: error" + e.getStackTrace().toString());
        }

    }

    private void notifyDataChange(){
        isCurrentValueWinning();
        setNextRewardText();
        Log.d(TAG, "notifyDataChange: rewardCollected = " +rewardCollected + " /for #" +lastReward);
        Log.d(TAG, "notifyDataChange: current points = " + currentPoints);

    }

    @Override
    protected void onStart() {
        super.onStart();
        userDataUserNameRef.addValueEventListener(userDataListener);
        gameDataRef.addValueEventListener(gameDataListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cleanUp();
    }

    public static void setSnapshot(DataSnapshot snapshot){
        newSnapshot = snapshot;

    }

    public static void setCurrentUser(String user){
        currentUser = user;
    }

    public void cleanUp(){
        userDataUserNameRef.removeEventListener(userDataListener);
        gameDataRef.removeEventListener(gameDataListener);
    }

}
