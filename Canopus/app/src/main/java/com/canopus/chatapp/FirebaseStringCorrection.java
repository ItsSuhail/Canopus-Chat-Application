package com.canopus.chatapp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FirebaseStringCorrection {
    public static String Decode(String encodeStr){
        encodeStr = encodeStr.replace("<dot>", ".");
        encodeStr = encodeStr.replace("<hash>", "#");
        encodeStr = encodeStr.replace("<dol>", "$");
        encodeStr = encodeStr.replace("<sqb1>", "[");
        encodeStr = encodeStr.replace("<sqb2>", "]");

        return encodeStr;
    }

    public static String Encode(String decodeStr){
        decodeStr = decodeStr.replace(".", "<dot>");
        decodeStr = decodeStr.replace("#", "<hash>");
        decodeStr = decodeStr.replace("$", "<dol>");
        decodeStr = decodeStr.replace("[", "<sqb1>");
        decodeStr = decodeStr.replace("]", "<sqb2>");

        return decodeStr;
    }

    public static boolean IsValidName(String name){
        String [] keywords = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "{", "}",
                "[","]", ":", ";", "'", "\"", ",", "/", "|", "\\", "<", ">", "ERROR"};
        if(name.isEmpty()){return false;}
        for(String e:keywords){
            if(name.contains(e)){
                return false;
            }
        }
        return true;
    }

    public static boolean isValidMsg(String message){
        String [] keywords = {"|", "<", ">"};
        if(message.isEmpty()){return false;}
        for(String e:keywords){
            if(message.contains(e)){
                return false;
            }
        }
        return true;
    }

    public static String getEncodedMsg(String recChatMessages, String message, String username){
        message = Encode(message);
        String result;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        result = recChatMessages+username + "<|>" + message + "<|>" + currentDateandTime + "<<|||>>";

        return result;
    }

    public static String getCurTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        return currentDateandTime;
    }
}
