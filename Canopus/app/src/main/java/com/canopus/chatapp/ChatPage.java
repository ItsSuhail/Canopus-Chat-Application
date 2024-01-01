package com.canopus.chatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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


    TextView starLbl, starMembersLbl;
    ImageView backIv;
    RecyclerView chatsRv;
    EditText messageEdt;
    ImageView sendMessageIv;

    FirebaseDatabase starDb;
    DatabaseReference starChatRef, starMembersRef;
    ValueEventListener starChatRefL, starMembersRefL;

    Intent cIntent;
    boolean doCheckChat = true;
    boolean doCheckMem = true;
    boolean chatChange = false;

    String TAG = "com.canopus.tag";

    String cStarChat, cStarMembers, cStar;
    String cMessage, cUser;

    String [] cStarUsers;
    String [] cStarMessages;
    String [] cStarMessageTimings;
    String [] cChats;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                doCheckMem = false;
                doCheckChat = false;
                if(starChatRefL!=null){
                    starChatRef.removeEventListener(starChatRefL);
                }
                if(starMembersRefL!=null){
                    starMembersRef.removeEventListener(starMembersRefL);
                }

                // Heading to Main page
                Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(MainPage);
                finish();
            }
        });


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
        starLbl = findViewById(R.id.lblStar);
        starMembersLbl = findViewById(R.id.lblStarMembers);
        messageEdt = findViewById(R.id.edtMessage);
        backIv = findViewById(R.id.ivBack);
        chatsRv = findViewById(R.id.rvChats);
        sendMessageIv = findViewById(R.id.ivSendMessage);

        // Initializing mediaplayer
        mediaPlayer = MediaPlayer.create(this, R.raw.received);

        // Initializing Firebase DB
        starDb = FirebaseDatabase.getInstance();
        starChatRef = starDb.getReference("starChats");
        starMembersRef = starDb.getReference("starMembers");

        // Setting properties for views
        starMembersLbl.setSelected(true);


        // Getting intent info
        cIntent = getIntent();
        if(cIntent.hasExtra("com.canopus.app.username") && cIntent.hasExtra("com.canopus.app.star")){
            Log.d(TAG, "getting star and username");

            cStar = cIntent.getStringExtra("com.canopus.app.star");
            cUser = cIntent.getStringExtra("com.canopus.app.username");
            starLbl.setText(cStar);

            Log.d(TAG, cStar);
            Log.d(TAG, cUser);

            // Getting chats
            starChatRefL = starChatRef.child(cStar).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckChat){
                        // checking if snapshot exists
                        if(snapshot.exists()){
                            cStarChat = String.valueOf(snapshot.getValue());
                            cChats = cStarChat.split(Pattern.quote("<<|||>>"));

                            cStarUsers = new String[cChats.length];
                            cStarMessages = new String[cChats.length];
                            cStarMessageTimings = new String[cChats.length];

                            // Getting user, msg and date time from cChat list
                            for (int i = 0; i < cChats.length; i++) {
                                cStarUsers[i] = cChats[i].split(Pattern.quote("<|>"))[0];
                                cStarMessages[i] = cChats[i].split(Pattern.quote("<|>"))[1];
                                cStarMessages[i] = FirebaseStringCorrection.Decode(cStarMessages[i]);

                                cStarMessageTimings[i] = cChats[i].split(Pattern.quote("<|>"))[2];
                                Log.d(TAG, cChats[i]);
                            }

                            // Media player
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
                                chatChange = true; // This is for the first time, the first time we load chats it will not start a sound, but every time chat updates, it will make a sound.
                            }

                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                            linearLayoutManager.setStackFromEnd(true);
                            chatsRv.setLayoutManager(linearLayoutManager);
                            ChatAdapter adapter = new ChatAdapter(cStarUsers, cStarMessages, cStarMessageTimings, cUser);
                            chatsRv.setAdapter(adapter);
                            chatsRv.scrollToPosition(adapter.getItemCount()-1);
                        }

                        else{
                            Log.d(TAG, "Some error occurred while fetching star chats. Star no longer exists");
                            Toast.makeText(ChatPage.this, "Some error occurred. STAR no longer exists.", Toast.LENGTH_SHORT).show();

                            doCheckChat = false;
                            doCheckMem = false;

                            sendMessageIv.setEnabled(false);
                            messageEdt.setEnabled(false);
                            chatsRv.setVisibility(View.INVISIBLE);

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
                    Log.d(TAG, "Some error occurred while fetching star chats: "+error.getDetails());

                    Toast.makeText(ChatPage.this, "Some error occurred while fetching STAR Chats. Try again.", Toast.LENGTH_LONG).show();

                    doCheckChat = false;
                    doCheckMem = false;

                    sendMessageIv.setEnabled(false);
                    messageEdt.setEnabled(false);
                    chatsRv.setVisibility(View.INVISIBLE);

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });

            // Getting members
            starMembersRefL = starMembersRef.child(cStar).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckMem){
                        // If snapshot exists.
                        if(snapshot.exists()){
                            cStarMembers = String.valueOf(snapshot.getValue());
                            cStarMembers = cStarMembers.replace("<<|||>>", ", "); // Suhail, Someone
                            cStarMembers = cStarMembers.substring(0, cStarMembers.lastIndexOf(", "));
                            starMembersLbl.setText(cStarMembers);
                        }

                        // If snapshot does not exist
                        else{
                            Log.d(TAG, "Star exists but has no members.");
                            Toast.makeText(ChatPage.this, "so such STAR exists.", Toast.LENGTH_LONG).show();

                            doCheckChat = false;
                            doCheckMem = false;

                            sendMessageIv.setEnabled(false);
                            messageEdt.setEnabled(false);
                            chatsRv.setVisibility(View.INVISIBLE);

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
                    Log.d(TAG, "Some error occurred while fetching star members: "+error.getDetails());

                    Toast.makeText(ChatPage.this, "Some error occurred while fetching STAR details. Try again.", Toast.LENGTH_LONG).show();

                    doCheckChat = false;
                    doCheckMem = false;

                    sendMessageIv.setEnabled(false);
                    messageEdt.setEnabled(false);
                    chatsRv.setVisibility(View.INVISIBLE);

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });
        }
        else{
            Log.d(TAG, "Some error occurred. No star name or username found");

            Toast.makeText(ChatPage.this, "Some error occurred while joining the chat. Try again", Toast.LENGTH_LONG).show();

            doCheckChat = false;
            doCheckMem = false;

            sendMessageIv.setEnabled(false);
            messageEdt.setEnabled(false);
            chatsRv.setVisibility(View.INVISIBLE);

            // Heading to MainPage
            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        }

        // When pressed on send message
        sendMessageIv.setOnClickListener(v -> {
            cMessage = messageEdt.getText().toString();

            if(FirebaseStringCorrection.isValidMsg(cMessage)){
                starChatRef.child(cStar).setValue(FirebaseStringCorrection.getEncodedMsg(cStarChat, cMessage, cUser));
                messageEdt.setText("");
            }
            else{
                if(!cMessage.isEmpty()){
                    Toast.makeText(this, "Invalid use of characters in your message.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // When pressed on back button
        backIv.setOnClickListener(v -> {
            doCheckChat = false;
            doCheckMem = false;
            hideKeyboard(ChatPage.this);

            Intent MainPage = new Intent(getApplicationContext(), com.canopus.chatapp.MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        });

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}