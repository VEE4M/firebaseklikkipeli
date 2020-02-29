package com.vmtikkanen.firebaseklikkipeli;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private int startingPoints = 20;
    private long currentValue;
    private long currentPoints;
    private long rewardsWon;
    private long timestamp;
    private TextView nextReward_text;
    private TextView currentPoints_text;
    private TextView rewardsWon_text;
    private static String currentUser;
    private String clicker;
    private boolean rewardCollected;

    private final Map<String, Object> userData = new HashMap<>();
    private final Map<String, Object> gameData = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference gameDataRef = db.collection("gamedata").document("data");
    private DocumentReference userDataRef = db.collection("userdata").document(currentUser);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextReward_text = findViewById(R.id.nextRewardTextView);
        currentPoints_text = findViewById(R.id.currentPointsTextView);
        rewardsWon_text = findViewById(R.id.rewardsWonTextView);

        Button clickButton = findViewById(R.id.button);
        TextView playerName_text = findViewById(R.id.playerNameTextView);
        playerName_text.setText(getString(R.string.playerText) + currentUser);

        userDataRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.d(TAG, "onEvent: error: " + e.toString());
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    try {
                        currentPoints = documentSnapshot.getLong("POINTS");
                        rewardsWon = documentSnapshot.getLong("REWARDSWON");
                        Log.d(TAG, "onEvent: dadaaa " + currentPoints);
                    }catch (NullPointerException error){
                        Log.d(TAG, "onEvent: error " + error.toString());
                    }
                }else{
                    Log.d(TAG, "onEvent: userData does not exist, lets create it.");
                    createUserData();
                }
                currentPoints_text.setText(String.valueOf(currentPoints));

                rewardsWon_text.setText(getString(R.string.RewardsWonText) + rewardsWon);
            }
        });

        gameDataRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.d(TAG, "onEvent: error: " + e.toString());
                }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        try {
                            currentValue = documentSnapshot.getLong("CURRENTVALUE");
                            clicker = documentSnapshot.getString("CLICKER");
                            rewardCollected = documentSnapshot.getBoolean("REWARDCOLLECTED");
                            Log.d(TAG, "onEvent: currentValue = " + currentValue);
                            notifyDataChange();
                        } catch (NullPointerException error){
                            Log.d(TAG, "onEvent: error " + error.toString());
                            }
                    }else {
                        Log.d(TAG, "onEvent: gameData does not exist, lets create it.");
                        createGameData();
                    }
                }


        });

        timestamp = System.currentTimeMillis();

        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((System.currentTimeMillis() - timestamp) > 500) {
                    if (currentPoints > 0) {
                        timestamp = System.currentTimeMillis();
                        Log.d(TAG, "onClick: timeStamp " + timestamp);
                        db.runTransaction(new Transaction.Function<Void>() {
                            @Override
                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot snapshot = transaction.get(gameDataRef);
                                long newValue = snapshot.getLong("CURRENTVALUE") + 1;
                                Log.d(TAG, "apply currentValue: " + newValue);
                                transaction.update(gameDataRef, "CURRENTVALUE", newValue, "CLICKER", currentUser, "REWARDCOLLECTED", false);
                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Transaction success! currentValue updated");
                                userDataRef.update("POINTS", currentPoints - 1);
                                userDataRef.update("REWARDSWON", rewardsWon);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Transaction failure. currentValue not updated ", e);
                            }
                        });

                    } else {
                        showNewGameDialog();
                    }
                }else {
                    Log.d(TAG, "onClick: clicking too fast, result = " + (System.currentTimeMillis() - timestamp));
                }
            }
        });
    }

    private void showNewGameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.NewGameDialogTitle))
                .setMessage(getString(R.string.NewGameDialogMessage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createUserData();
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


    private void isCurrentValueWinning(long value){
        Log.d(TAG, "isCurrentValueWinning: called with currentValue " + currentValue);
        if (value > 9) {
            if ((value % 500) == 0) {
                Log.d(TAG, "isCurrentValueWinning: 500 found");
                checkForWinner(value, 250);
            } else if ((value % 100) == 0) {
                Log.d(TAG, "isCurrentValueWinning: 100 found");
                checkForWinner(value, 40);
            } else if ((value % 10) == 0) {
                checkForWinner(value, 5);
            }
        }
    }

    private void setNextRewardText(){
        if(currentValue%10 != 0){
            long nextReward = 10 - (currentValue%10);
            nextReward_text.setText(getString(R.string.ClicksNeededText) + nextReward);
        }else{
            nextReward_text.setText(getString(R.string.ClicksNeededText) + "10");
        }
    }


    public void checkForWinner(final long value, int rewardAmount) {

        Log.d(TAG, "checkForWinner: called, currentValue = " + currentValue);
        try {
            Log.d(TAG, "checkForWinner: rewardCollected = " + rewardCollected + "| clicker = " + clicker + "| value = " +value);
            if (clicker.equals(currentUser) && !rewardCollected ) {
                if(currentValue == value){
                    gameDataRef.update("REWARDCOLLECTED", true);
                }
                userDataRef.update("POINTS", currentPoints + rewardAmount);
                userDataRef.update("REWARDSWON", rewardsWon+1);
                Toast.makeText(this, getString(R.string.YouWonText) + rewardAmount + getString(R.string.PointsText), Toast.LENGTH_LONG).show();
            } else if (!rewardCollected) {
                Toast.makeText(this, R.string.SomeoneElseWonText, Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "checkForWinner: Reward has already been collected for #" + value);
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "checkForWinner: error" + e.toString());
        }

    }

    private void notifyDataChange(){
        isCurrentValueWinning(currentValue);
        setNextRewardText();
        Log.d(TAG, "notifyDataChange: rewardCollected = " +rewardCollected + " /for #");
        Log.d(TAG, "notifyDataChange: current points = " + currentPoints);

    }

    private void createUserData(){
        Log.d(TAG, "createUserData: called");
        userData.put("POINTS", startingPoints);
        userData.put("REWARDSWON", 0);
        userDataRef.set(userData);
    }


    private void createGameData(){
        Log.d(TAG, "createGameData: called");
        gameData.put("CLICKER", "null");
        gameData.put("CURRENTVALUE", 0);
        gameData.put("REWARDCOLLECTED", false);
        gameDataRef.set(gameData);
    }

    public static void setCurrentUser(String user){
        currentUser = user;
    }

}
