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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatPage extends AppCompatActivity {


    TextView starLbl, starMembersLbl;
    ImageView backIv;
    RecyclerView chatsRv;
    EditText messageEdt;
    ImageView sendMessageIv;

    FirebaseDatabase starDb;
    DatabaseReference starRef, usersRef;
    ValueEventListener starChatRefL, starMembersRefL, starMembersEmailRefL, usersRefL;

    Intent cIntent;
    boolean doCheckChat = true;
    boolean doCheckMem = true;
    boolean chatChange = false;
    boolean fetchToken = false;

    String TAG = "com.canopus.tag";

    String cStarChat, cStarMembers, cStar, cStarMembersEmail;
    String cMessage, cUser;

    String [] cStarUsers;
    String [] cStarMessages;
    String [] cStarMessageTimings;
    String [] cChats;
    String [] starEmails;

    ArrayList<String> fcmTokens;

    FirebaseAuth mAuth;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doCheckChat = true;
        doCheckMem = true;
        setContentView(R.layout.activity_chat_page);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                doCheckMem = false;
                doCheckChat = false;
                if(starChatRefL!=null){
                    starRef.removeEventListener(starChatRefL);
                }
                if(starMembersRefL!=null){
                    starRef.removeEventListener(starMembersRefL);
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
        mAuth = FirebaseAuth.getInstance();

        fcmTokens = new ArrayList<>();

        // Initializing mediaplayer
        mediaPlayer = MediaPlayer.create(this, R.raw.received);

        // Initializing Firebase DB
        starDb = FirebaseDatabase.getInstance();
        starRef = starDb.getReference("star");
        usersRef = starDb.getReference("users");

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
            starChatRefL = starRef.child(cStar).child("chats").addValueEventListener(new ValueEventListener() {
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
                            Log.d(TAG, "Some error occurred while fetching star chats. Star no longer exists (snapshot doesnt exist)");
                            Toast.makeText(ChatPage.this, "Some error occurred. STAR no longer exists.", Toast.LENGTH_SHORT).show();

                            doCheckChat = false;
                            doCheckMem = false;

                            if(starChatRefL!=null){
                                starRef.removeEventListener(starChatRefL);
                            }
                            if(starMembersRefL!=null){
                                starRef.removeEventListener(starMembersRefL);
                            }

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
                    doCheckChat = false;
                    doCheckMem = false;

                    if(starChatRefL!=null){
                        starRef.removeEventListener(starChatRefL);
                    }
                    if(starMembersRefL!=null){
                        starRef.removeEventListener(starMembersRefL);
                    }

                    Log.d(TAG, "Some error occurred while fetching star chats: "+error.getDetails());

                    Toast.makeText(ChatPage.this, "Some error occurred while fetching STAR Chats. Try again.", Toast.LENGTH_LONG).show();

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
            starMembersRefL = starRef.child(cStar).child("members").addValueEventListener(new ValueEventListener() {
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
                            doCheckChat = false;
                            doCheckMem = false;

                            if(starChatRefL!=null){
                                starRef.removeEventListener(starChatRefL);
                            }
                            if(starMembersRefL!=null){
                                starRef.removeEventListener(starMembersRefL);
                            }

                            Log.d(TAG, "Star exists but has no members.");
                            Toast.makeText(ChatPage.this, "so such STAR exists.", Toast.LENGTH_LONG).show();

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
                    doCheckChat = false;
                    doCheckMem = false;

                    if(starChatRefL!=null){
                        starRef.removeEventListener(starChatRefL);
                    }
                    if(starMembersRefL!=null){
                        starRef.removeEventListener(starMembersRefL);
                    }

                    Log.d(TAG, "Some error occurred while fetching star members: "+error.getDetails());

                    Toast.makeText(ChatPage.this, "Some error occurred while fetching STAR details. Try again.", Toast.LENGTH_LONG).show();

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

            // For getting tokens, by getting all the emails for members

            Log.d(TAG, "Getting email addresses of star members");

            // Getting members
            starMembersEmailRefL = starRef.child(cStar).child("membersEmail").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckMem){
                        // If snapshot exists.
                        if(snapshot.exists()){
                            cStarMembersEmail = String.valueOf(snapshot.getValue());
                            cStarMembersEmail = cStarMembersEmail.replace(FirebaseStringCorrection.Encode(mAuth.getCurrentUser().getEmail())+"<<|||>>", "");

                            Log.d(TAG, "Email addresses of members: "+cStarMembersEmail);
                            starEmails = cStarMembersEmail.split(Pattern.quote("<<|||>>"));


                            for (String emailAddress:starEmails) {
//                                fetchToken = true;
                                Log.d(TAG, "Inside for-loop: "+emailAddress);
                                usersRefL = usersRef.child(emailAddress).child("fcmtoken").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                        if(fetchToken){
//                                            fetchToken = false;
                                        if(snapshot.exists()){

                                            fcmTokens.add(snapshot.getValue().toString());
                                            Log.d(TAG, "TOKEN: "+snapshot.getValue().toString());
                                        }

                                        // Removing listener
                                        if(usersRefL!=null){
                                            usersRef.removeEventListener(usersRefL);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d(TAG, "Some error occurred while fetching tokens of star members: "+error.toString());
                                        fetchToken = false;

                                        if(usersRefL!=null){
                                            usersRef.removeEventListener(usersRefL);
                                        }
                                    }
                                });
                            }
                        }

                        // If snapshot does not exist
                        else{
                            doCheckChat = false;
                            doCheckMem = false;

                            if(starChatRefL!=null){
                                starRef.removeEventListener(starChatRefL);
                            }
                            if(starMembersRefL!=null){
                                starRef.removeEventListener(starMembersRefL);
                            }

                            Log.d(TAG, "Star exists but has no members.");
                            Toast.makeText(ChatPage.this, "so such STAR exists.", Toast.LENGTH_LONG).show();

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
                    doCheckChat = false;
                    doCheckMem = false;

                    if(starChatRefL!=null){
                        starRef.removeEventListener(starChatRefL);
                    }
                    if(starMembersRefL!=null){
                        starRef.removeEventListener(starMembersRefL);
                    }

                    Log.d(TAG, "Some error occurred while fetching star members: "+error.getDetails());

                    Toast.makeText(ChatPage.this, "Some error occurred while fetching STAR details. Try again.", Toast.LENGTH_LONG).show();

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
                starRef.child(cStar).child("chats").setValue(FirebaseStringCorrection.getEncodedMsg(cStarChat, cMessage, cUser));
                sendNotifications(cMessage);
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

    public void sendNotifications(String message){
        Log.d(TAG, "Sending notifications");
        for(int i = 0; i<fcmTokens.size(); i++){
            try{
                JSONObject jsonObject = new JSONObject();

                JSONObject notificationObject = new JSONObject();
                notificationObject.put("title", cUser + " in: "+cStar);
                notificationObject.put("body",message);
                notificationObject.put("icon", "ic_canopus_icon");
                notificationObject.put("channel_id", "message");
                notificationObject.put("android_channel_id", "message");
                notificationObject.put("sound", "received");

                JSONObject dataObject = new JSONObject();
                dataObject.put("StarName", cStar);

                jsonObject.put("notification", notificationObject);
                jsonObject.put("data", dataObject);
                jsonObject.put("to", fcmTokens.get(i));

                Log.d(TAG, "Notification To: "+ fcmTokens.get(i));

                callFirebaseApi(jsonObject);
            }
            catch(Exception e){
                Log.d(TAG, "Some error occurred while sending notification: "+e.toString());
            }
        }
    }

    public void callFirebaseApi(JSONObject jsonObject){
        Log.d(TAG, "Sending notification: " + jsonObject.toString());
        MediaType json = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String fcmApiUrl = "https://fcm.googleapis.com/fcm/send";
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), json);
        Request request = new Request.Builder()
                .url(fcmApiUrl)
                .post(requestBody)
                .header("Authorization", "Bearer *key*")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        doCheckMem = false;
        doCheckChat = false;
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        doCheckMem = false;
        doCheckChat = false;
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onStart() {
        super.onStart();

        doCheckMem = true;
        doCheckChat = true;
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();

        doCheckChat = true;
        doCheckMem = true;
        Log.d(TAG, "onResume: ");
    }
}