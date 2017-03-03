package com.mljsgto222.cordova.plugin.audiorecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class echoes a string called from JavaScript.
 */
public class AudioRecorder extends CordovaPlugin {
    private static final String TAG = AudioRecorder.class.getName();

    private static final String OUT_SAMPLING_RATE = "outSamplingRate";
    private static final String OUT_BIT_RATE = "outBitRate";
    private static final String IS_CHAT_MODE = "isChatMode";
    private static final String IS_SAVE = "isSave";

    private MP3Recorder recorder;
    private CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("startRecord")) {
            startRecord(args, callbackContext);
            return true;
        }else if(action.equals("stopRecord")) {
            stopRecord(callbackContext);
            return true;
        }else if(action.equals("delRecord")) {
            delRecord(args,callbackContext);
            return true;
        } else if(action.equals("encodeBase64Record")) {
            encodeBase64Record(args,callbackContext);
           return true;
        }
        return false;
    }


    private boolean requestRecordPermission(){
        boolean isPermissionGranted = cordova.hasPermission(Manifest.permission.RECORD_AUDIO);
        if(!isPermissionGranted){
            cordova.requestPermission(this, 1, Manifest.permission.RECORD_AUDIO);
        }
        return isPermissionGranted;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        switch (requestCode){
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    try{
                        recorder.startRecord();
                        callback.success();
                    }catch (IOException ex){
                        Log.e(TAG, ex.getMessage());
                        callback.error(ex.getMessage());
                    }

                }else{
                    callback.error("user permission denied");
                }
                break;
            }
        }
    }

    private void startRecord(JSONArray args, CallbackContext callbackContext){
        if(recorder == null || !recorder.isRecording()){

            recorder = new MP3Recorder(this.cordova.getActivity());
            if(!args.isNull(0)){
                try{
                    JSONObject options = args.getJSONObject(0);
                    if(options.has(OUT_SAMPLING_RATE)){
                        recorder.setSamplingRate(options.getInt(OUT_SAMPLING_RATE));
                    }
                    if(options.has(OUT_BIT_RATE)){
                        recorder.setBitRate(options.getInt(OUT_BIT_RATE));
                    }
                    if(options.has(IS_CHAT_MODE)) {
                        recorder.setIsChatMode(options.getBoolean(IS_CHAT_MODE));
                    }
                    if(options.has(IS_SAVE)){
                        recorder.setIsSave(options.getBoolean(IS_SAVE));
                    }
                }catch (JSONException ex){
                    Log.e(TAG, ex.getMessage());
                }
            }
            try{
                callback = callbackContext;
                if(requestRecordPermission()){
                    recorder.startRecord();
                    callbackContext.success();
                }
            }catch (IOException ex){
                Log.e(TAG, ex.getMessage());
                callbackContext.error(ex.getMessage());
            }
        }else if(recorder.isRecording()){
            callbackContext.success();
        }
    }

    private void stopRecord(CallbackContext callbackContext){
        if(recorder != null){
            recorder.stopRecord();
            File file = recorder.getFile();
            if(file != null){
                Uri uri = Uri.fromFile(file);
                JSONObject fileJson = new JSONObject();
                try{
                    fileJson.put("name", file.getName());
                    fileJson.put("type", "audio/mpeg");
                    fileJson.put("uri", uri.toString());
                    fileJson.put("duration", recorder.getDuration());

                }catch(JSONException ex){
                    Log.e(TAG, ex.getMessage());
                }

                callbackContext.success(fileJson);
            }else{
                callbackContext.error("record file not found");
            }
        } else {
            callbackContext.error("AudioRecorder has not recorded yet");
        }
    }

    private void delRecord(JSONArray args, CallbackContext callbackContext){
        if(!args.isNull(0)) {
            try{
                String filePath = (String) args.get(0);


                File file = new File(filePath.substring(7));
                 boolean BB =  file.isFile();
                boolean KK =  file.exists();
                if (file.isFile() && file.exists()) {
                     file.delete();
                }
                callbackContext.success();
            }catch (JSONException ex){
                Log.e(TAG, ex.getMessage());
                callbackContext.error(ex.getMessage());
            }

        }

    }

    private  void encodeBase64Record(JSONArray args, CallbackContext callbackContext)  {


        if(!args.isNull(0)) {
            try {
                String path = (String) args.get(0);
                File file = new File(path.substring(7));
                boolean BB =  file.isFile();
                boolean KK =  file.exists();
                if (file.isFile() && file.exists()) {
                    FileInputStream inputFile = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    inputFile.read(buffer);
                    inputFile.close();
                    String result = Base64.encodeToString(buffer, Base64.DEFAULT);
                    callbackContext.success(result);
                } else {

                }
            } catch (IOException e) {
                callbackContext.error(e.getMessage());
            }catch (JSONException ex){
                callbackContext.error(ex.getMessage());
            }
        }

    }
}
