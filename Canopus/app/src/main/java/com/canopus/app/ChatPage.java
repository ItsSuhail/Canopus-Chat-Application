package com.canopus.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class ChatPage extends AppCompatActivity {

    TextView star, starMembers;
    ImageView backIV_star;
    RecyclerView chatsRecView;
    EditText message;
    ImageView sendMessageBtn;

    FirebaseDatabase starDb;
    DatabaseReference starChatRef, starMembersRef;
    ValueEventListener starChatRefL, starMembersRefL;

    boolean doCheckChat = true;
    boolean doCheckMem = true;
    boolean chatChange = false;

    String curStarChat, curStarMembers, curStar;
    String curMessage, curUser;

    String [] curStarUser;
    String [] curStarMsg;
    String [] curStarMsgTime;
    String [] curChat;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        /*
        Structure of ChatPage

        get intent info-->

            if got info-->
                    >> get chat
                    >> get members
                    >> start chatting

            else-->
                    >> error go boom!
         */

        doCheckChat = true;
        doCheckMem = true;
        // Getting views
        star = findViewById(R.id.star);
        starMembers = findViewById(R.id.starMembers);
        message = findViewById(R.id.message);
        backIV_star = findViewById(R.id.backIV_star);
        chatsRecView = findViewById(R.id.chatsRecView);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);

        // Initializing mediaplayer
        mediaPlayer = MediaPlayer.create(this, R.raw.received);

        // Initializing Firebase DB
        starDb = FirebaseDatabase.getInstance();
        starChatRef = starDb.getReference("starChats");
        starMembersRef = starDb.getReference("starMembers");

        // Setting some properties
        starMembers.setSelected(true);

        // Getting intent info
        Intent cIntent = getIntent();
        if(cIntent.hasExtra("com.canopus.app.username") && cIntent.hasExtra("com.canopus.app.star")){
            Log.d("APP_WORK", "getting star and username");

            curStar = cIntent.getStringExtra("com.canopus.app.star");
            curUser = cIntent.getStringExtra("com.canopus.app.username");
            star.setText(curStar);

            Log.d("APP_WORK", curStar);
            Log.d("APP_WORK", curUser);

            // Getting chats
            starChatRefL = starChatRef.child(curStar).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckChat){
                        // checking if snapshot exists
                        if(snapshot.exists()){
                            curStarChat = String.valueOf(snapshot.getValue());
                            curChat = curStarChat.split(Pattern.quote("<<|||>>"));
//                            doesStarExists = true;

                            curStarUser = new String[curChat.length];
                            curStarMsg = new String[curChat.length];
                            curStarMsgTime = new String[curChat.length];

                            // Getting user and msg from curChat list
                            for (int i = 0; i < curChat.length; i++) {
                                curStarUser[i] = curChat[i].split(Pattern.quote("<|>"))[0];
                                curStarMsg[i] = curChat[i].split(Pattern.quote("<|>"))[1]; // Suhail<|>Hi<|>27-6-2021
                                curStarMsg[i] = FirebaseStringCorrection.Decode(curStarMsg[i]);

                                curStarMsgTime[i] = curChat[i].split(Pattern.quote("<|>"))[2];
                                Log.d("APP_WORK", curChat[i]);
                            }

                            // Recycler view
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.received);

                            if(chatChange){
                                if(mediaPlayer!=null){
                                    mediaPlayer.start();

                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            mp.release();
                                        }
                                    });
                                }
                            }
                            else{
                                chatChange = true;
                            }
                            LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
                            lm.setStackFromEnd(true);
                            chatsRecView.setLayoutManager(lm);
                            ChatAdapter adapter = new ChatAdapter(curStarUser, curStarMsg, curStarMsgTime, curUser);
                            chatsRecView.setAdapter(adapter);
                            chatsRecView.scrollToPosition(adapter.getItemCount()-1);
                        }
                        else{
                            Log.d("APP_WORK", "Error, while fetching star chats. star not longer exists");
                            Toast.makeText(ChatPage.this, "SOME ERROR OCCURRED! STAR NO LONGER EXISTS", Toast.LENGTH_SHORT).show();

                            doCheckChat = false;
                            doCheckMem = false;
//                            doesStarExists = false;

                            sendMessageBtn.setEnabled(false);
                            message.setEnabled(false);
                            chatsRecView.setVisibility(View.INVISIBLE);

                            // Heading to MainPage
                            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(MainPage);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("APP_WORK", "Error, while fetching star chats: "+error.getDetails());

                    Toast.makeText(ChatPage.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();

                    doCheckChat = false;
                    doCheckMem = false;
//                    doesStarExists = false;

                    sendMessageBtn.setEnabled(false);
                    message.setEnabled(false);
                    chatsRecView.setVisibility(View.INVISIBLE);

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });

            // Getting members
            starMembersRefL = starMembersRef.child(curStar).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckMem){
                        if(snapshot.exists()){
                            curStarMembers = String.valueOf(snapshot.getValue());
                            curStarMembers = curStarMembers.replace("<<|||>>", ", "); // Suhail, Someone
                            curStarMembers = curStarMembers.substring(0, curStarMembers.lastIndexOf(", "));
                            starMembers.setText(curStarMembers);
//                            doesStarExists = true;
                        }
                        else{
                            Log.d("APP_WORK", "Error, while fetching star chats. star not longer exists");
                            Toast.makeText(ChatPage.this, "SOME ERROR OCCURRED! STAR NO LONGER EXISTS", Toast.LENGTH_SHORT).show();

                            doCheckChat = false;
                            doCheckMem = false;
//                            doesStarExists = false;

                            sendMessageBtn.setEnabled(false);
                            message.setEnabled(false);
                            chatsRecView.setVisibility(View.INVISIBLE);

                            // Heading to MainPage
                            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(MainPage);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("APP_WORK", "Error, while fetching star members: "+error.getDetails());

                    Toast.makeText(ChatPage.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();

                    doCheckChat = false;
                    doCheckMem = false;
//                    doesStarExists = false;

                    sendMessageBtn.setEnabled(false);
                    message.setEnabled(false);
                    chatsRecView.setVisibility(View.INVISIBLE);

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });
        }
        else{
            Log.d("APP_WORK", "Error, cannot fetch starname");
            Log.d("APP_WORK", "Error, cannot fetch username");

            Toast.makeText(ChatPage.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();

            doCheckChat = false;
            doCheckMem = false;
//            doesStarExists = false;

            sendMessageBtn.setEnabled(false);
            message.setEnabled(false);
            chatsRecView.setVisibility(View.INVISIBLE);

            // Heading to MainPage
            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        }

        // When pressed on send message
        sendMessageBtn.setOnClickListener(v -> {
            curMessage = message.getText().toString();

            if(FirebaseStringCorrection.isValidMsg(curMessage)){
//                if(doesStarExists){
                starChatRef.child(curStar).setValue(FirebaseStringCorrection.getEncodedMsg(curStarChat, curMessage, curUser));
                message.setText("");
//                }
//                else{
//                    Log.d("APP_WORK", "Error, while fetching star chats. star not longer exists");
//                    Toast.makeText(ChatPage.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
//
//                    doCheckChat = false;
//                    doCheckMem = false;
//                    doesStarExists = false;
//
//                    // Heading to MainPage
//                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
//                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(MainPage);
//                    finish();
//                }
            }
            else{
                if(!curMessage.isEmpty()){
                    Toast.makeText(this, "Message couldn't be send!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //
        backIV_star.setOnClickListener(v -> {
            Intent MainPage = new Intent(getApplicationContext(), com.canopus.app.MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            doCheckChat = false;
            doCheckMem = false;
            startActivity(MainPage);
            finish();
        });

//        // Recycler view
//        chatsRecView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//        ChatAdapter adapter = new ChatAdapter(curStarUser, curStarMsg);
//        chatsRecView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent MainPage = new Intent(getApplicationContext(), com.canopus.app.MainPage.class);
        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        doCheckChat = false;
        doCheckMem = false;
        startActivity(MainPage);
        finish();
    }
}