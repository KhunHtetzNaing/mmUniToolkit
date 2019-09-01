package com.htetznaing.unitoolkit.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.htetznaing.unitoolkit.Activity.CustomizeActivity;
import com.htetznaing.unitoolkit.Activity.MainActivity;
import com.htetznaing.unitoolkit.Constants;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Toolkit {
    private static ArrayList<String> paths,mimeType;
    static int check = 0;
    public static void changeFileNameToUnicode(Context context,File file,boolean force){
        if (file!=null){
            for (File f:file.listFiles()){
                if (f.isDirectory()){
                    File newFile = new File(f.getParentFile() + "/" + AIOmmTool.getUnicode(f.getName(),force));
                    if (f.renameTo(newFile)) {
                        changeFileNameToUnicode(context,newFile,force);
                    } else {
                        changeFileNameToUnicode(context,f,force);
                    }
                }else {
                    File newFile = new File(f.getParentFile()+"/"+AIOmmTool.getUnicode(f.getName(),force));
                    if (f.renameTo(newFile)){
                        if (isImageFile(f.toString()) || isVideoFile(f.toString()) || isAudioFile(f.toString())){
                            addPath(newFile);
                        }
                        if (isAudioFile(newFile.toString())){
                            editAudioTag(context,newFile,force,false);
                        }
                    }else {
                        if (isAudioFile(f.toString())){
                            editAudioTag(context,f,force,false);
                        }
                    }
                }
            }
        }
    }

    private static void addPath(File newFile){
        if(!newFile.isHidden()) {
            paths.add(newFile.toString());
            mimeType.add(getMimeType(newFile.toString()));
        }
    }

    private static String getExt(String filePath){
        int strLength = filePath.lastIndexOf(".");
        if(strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }

    public static void changeFileNameCustomExtension(Context context,File file,String extension,boolean changeFolderName,boolean force){
        if (file!=null){
            if (file.isFile()){
                changeCustom(context,file,extension,changeFolderName,force);
            }else
                for (File f:file.listFiles()){
                    changeCustom(context,f,extension,changeFolderName,force);
                }
        }
    }

    private static void changeCustom(Context context,File f,String extension,boolean changeFolderName,boolean force){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.CUSTOM,Context.MODE_PRIVATE);
        if (f.isDirectory()){
            if (changeFolderName){
                File newFile = new File(f.getParentFile()+"/"+AIOmmTool.getUnicode(f.getName(),force));
                if (f.renameTo(newFile)) {
                    changeFileNameCustomExtension(context,newFile,extension,changeFolderName,force);
                }else {
                    changeFileNameCustomExtension(context,f,extension,changeFolderName,force);
                }
            }else {
                changeFileNameCustomExtension(context,f,extension,changeFolderName,force);
            }
        }else {
            String fileExt = getExt(f.toString());

            if (extension==null || extension.isEmpty() || extension.length()<2){
                extension = fileExt;
            }

            assert extension != null;
            assert fileExt != null;
            if (extension.contains(fileExt)) {

                if (extension.equalsIgnoreCase(fileExt)){
                    extension=null;
                }

                File newFile = new File(f.getParentFile() + "/" + AIOmmTool.getUnicode(f.getName(),force));
                if (f.renameTo(newFile)) {
                    if (isImageFile(f.toString()) || isVideoFile(f.toString()) || isAudioFile(f.toString())){
                        addPath(newFile);
                    }
                    if (isAudioFile(newFile.toString())){
                        editAudioTag(context,newFile,force,false);
                    }

                    String file_id = fileToMD5(newFile.toString());
                    if (!sharedPreferences.contains(file_id)){
                        sharedPreferences.edit().putString(file_id,f.toString()).apply();
                    }
                }
            }
        }
    }

    public static void changeSD(DocumentFile file, String extension, boolean changeFolderName, boolean force){
        if (file!=null){
            if (file.isFile()){
                changeSDCustom(file,extension,changeFolderName,force);
            }else
                for (DocumentFile f:file.listFiles()){
                    changeSDCustom(f,extension,changeFolderName,force);
                }
        }
    }

    private static void changeSDCustom(DocumentFile f,String extension,boolean changeFolderName,boolean force){
        if (f!=null) {
            if (f.isDirectory()) {
                if (changeFolderName) {
                    f.renameTo(Objects.requireNonNull(AIOmmTool.getUnicode(f.getName(), force)));
                }
                changeSD(f, extension, changeFolderName, force);
            } else {
                String fileExt = getExt(f.toString());

                if (extension == null || extension.isEmpty() || extension.length() < 2) {
                    extension = fileExt;
                }

                if (extension.contains(fileExt)) {
                    if (extension.equalsIgnoreCase(fileExt)) {
                        extension = null;
                    }

                    if (f.renameTo(AIOmmTool.getUnicode(f.getName(), force))) {
                        if (isImageFile(f.getUri().getPath()) || isVideoFile(f.getUri().getPath()) || isAudioFile(f.getUri().getPath())) {
                            addPath(new File(f.getUri().getPath()));
                        }
//                        if (isAudioFile(f.getUri().getPath())) {
//                            editAudioTag(new File(f.getUri().getPath()), force);
//                        }
                    } else {
//                        if (isAudioFile(f.toString())) {
//                            editAudioTag(new File(f.getUri().getPath()), force);
//                        }
                    }
                }
            }
        }
    }

    public static void resetPathsAndMimeType() {
        paths = new ArrayList<>();
        mimeType = new ArrayList<>();
    }


    public static void audioFileNameToUnicode(Context context,File file,boolean force){
        if (file!=null){
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.AUDIOS,Context.MODE_PRIVATE);
            for (File f:file.listFiles()){
                if (f.isDirectory()){
                    audioFileNameToUnicode(context,f,force);
                }else {
                    if (isAudioFile(f.toString())) {
                        File newFile = new File(f.getParentFile() + "/" + AIOmmTool.getUnicode(f.getName(),force));
                        if (f.renameTo(newFile)){
                            editAudioTag(context,newFile,force,false);
                            addPath(newFile);
                            String file_id = fileToMD5(newFile.toString());
                            if (!sharedPreferences.contains(file_id)){
                                sharedPreferences.edit().putString(file_id,f.toString()).apply();
                                System.out.println("Backup: "+f.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    public static void videoFileNameToUnicode(Context context,File file,boolean force){
        if (file!=null){
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.VIDEOS,Context.MODE_PRIVATE);
            for (File f:file.listFiles()){
                if (f.isDirectory()){
                    videoFileNameToUnicode(context,f,force);
                }else {
                    if (isVideoFile(f.toString())) {
                        File newFile = new File(f.getParentFile() + "/" + AIOmmTool.getUnicode(f.getName(),force));
                        if (f.renameTo(newFile)){
                            addPath(newFile);
                            String file_id = fileToMD5(newFile.toString());
                            if (!sharedPreferences.contains(file_id)){
                                sharedPreferences.edit().putString(file_id,f.toString()).apply();
                            }
                        }
                    }
                }
            }
        }
    }

    public static void imageFileNameToUnicode(Context context,File file,boolean force){
        if (file!=null){
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.IMAGES,Context.MODE_PRIVATE);
            for (File f:file.listFiles()){
                if (f.isDirectory()){
                    imageFileNameToUnicode(context,f,force);
                }else {
                    if (isImageFile(f.toString())) {
                        File newFile = new File(f.getParentFile() + "/" + AIOmmTool.getUnicode(f.getName(),force));
                        if (f.renameTo(newFile)){
                            addPath(newFile);
                            String file_id = fileToMD5(newFile.toString());
                            if (!sharedPreferences.contains(file_id)){
                                sharedPreferences.edit().putString(file_id,f.toString()).apply();
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isVideoFile(String path) {
        return MediaFileTypeUtil.getInstance().isVideoFile(path);
    }

    private static boolean isAudioFile(String path) {
        return MediaFileTypeUtil.getInstance().isAudioFile(path);
    }

    private static boolean isImageFile(String path) {
        return MediaFileTypeUtil.getInstance().isImageFile(path);
    }

    public static void updateMEDIA(final Activity context){
        String[] p = new String[paths.size()];
        paths.toArray(p);

        String[] m = new String[mimeType.size()];
        mimeType.toArray(m);

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Update media..");
        if (m.length>1) {
            progressDialog.show();
        }else {
            if (context==MainActivity.instance) {
                MainActivity.instance.done(true);
            }else {
                CustomizeActivity.instance.done(true);
            }
        }

        MediaScannerConnection.scanFile(context, p, m, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                System.out.println("Scanned: "+path);
                if (check==paths.size()-1){
                    progressDialog.dismiss();
                    if (context==MainActivity.instance) {
                        MainActivity.instance.done(true);
                    }else {
                        CustomizeActivity.instance.done(true);
                    }
                    check=0;
                }else check++;
            }
        });
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private static void editAudioTag(Context context,File audio,boolean force,boolean restore){
        if (audio.exists() && audio.getName().endsWith(".mp3")) {
            String id = audio.toString().replace("/","_");
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MP3,Context.MODE_PRIVATE);
            AudioFile audioFile = null;
            try {
                audioFile = AudioFileIO.read(audio);
                Tag tag = audioFile.getTagOrCreateAndSetDefault();
                Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);

                if (restore){
                    fieldKeyValueMap = getData(sharedPreferences.getString(id,null));
                    if (fieldKeyValueMap==null){
                        return;
                    }
                    System.out.println(audio);
                    writeAudio(audioFile,tag,fieldKeyValueMap);
                }else {
                    putAudioField( FieldKey.TITLE, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.ALBUM, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.ARTIST, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.GENRE, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.YEAR, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.TRACK, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.LYRICS, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.ALBUM_ARTIST, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.ARTISTS, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.COMMENT, tag, fieldKeyValueMap, force);
                    putAudioField( FieldKey.COMPOSER, tag, fieldKeyValueMap, force);
                    writeAudioToUnicode(audioFile,tag,fieldKeyValueMap);
                }

                if (!sharedPreferences.contains(id)){
                    Gson gson = new Gson();
                    System.out.println(audio);
                    sharedPreferences.edit().putString(id, gson.toJson(fieldKeyValueMap)).apply();
                }


            } catch (CannotReadException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TagException e) {
                e.printStackTrace();
            } catch (ReadOnlyFileException e) {
                e.printStackTrace();
            } catch (InvalidAudioFrameException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<FieldKey, String> getData(String data){
        if (data!=null){
            Type type = new TypeToken<Map<FieldKey, String>>(){}.getType();
            Gson gson = new Gson();
            Map<FieldKey, String> out = gson.fromJson(data,type);
            return out;
        }
        return null;
    }

    private static void writeAudioToUnicode(AudioFile audioFile,Tag tag,Map<FieldKey, String> fieldKeyValueMap){
        for (Map.Entry<FieldKey, String> entry : fieldKeyValueMap.entrySet()) {
            try {
                if(tag.hasField(entry.getKey())) {
                    tag.setField(entry.getKey(), AIOmmTool.getUnicode(entry.getValue(),false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            audioFile.commit();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        }
    }

    private static void writeAudio(AudioFile audioFile,Tag tag,Map<FieldKey, String> fieldKeyValueMap){
        for (Map.Entry<FieldKey, String> entry : fieldKeyValueMap.entrySet()) {
            try {
                if(tag.hasField(entry.getKey())) {
                    tag.setField(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            audioFile.commit();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        }
    }

    private static void putAudioField(FieldKey key,Tag tag,Map<FieldKey, String> fieldKeyValueMap,boolean force){
        if (tag.getFirst(key)!=null && !tag.getFirst(key).isEmpty()) {
            fieldKeyValueMap.put(key, tag.getFirst(key));
        }
    }

    public static void changeContacts(Context context,boolean force) {
        SharedPreferences backup = context.getSharedPreferences(Constants.CONTACTS,Context.MODE_PRIVATE);
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                if (!backup.contains(id)){
                    System.out.println("Backup: "+name);
                    backup.edit().putString(id,name).apply();
                }
                updateContact(context,id,AIOmmTool.getUnicode(name,force));
            }
        }
        if(cur!=null){
            cur.close();
        }
    }

    public static boolean restoreContact(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.CONTACTS,Context.MODE_PRIVATE);
        Map<String,?> keys = sharedPreferences.getAll();
        if (keys.size()>0) {
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                String id = entry.getKey();
                String name = entry.getValue().toString();
                updateContact(context, id, name);
            }
            return true;
        }
        return false;
    }

    private static boolean updateContact(Context context,String contactID, String contactName) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE
                        + "=?", new String[]{contactID, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void scanMediaNormal(Context context){
        Bundle bundle = new Bundle();
        bundle.putString("volume", "external");
        context.startService(new Intent().setComponent(new ComponentName("com.android.providers.media", "com.android.providers.media.MediaScannerService")).putExtras(bundle));
        Toast.makeText(context, "Media scanner started", Toast.LENGTH_SHORT).show();
    }

    private static String fileToMD5(String filePath) {
        return filePath;
    }

    public static void restoreFiles(Context context,Map<String,?> data){
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue().toString();
            File f = new File(id);
            File newFile = new File(name);
            if (f.exists()){
                if (f.renameTo(newFile)){
                    if (isImageFile(f.toString()) || isVideoFile(f.toString()) || isAudioFile(f.toString())){
                        addPath(newFile);
                    }
                    if (isAudioFile(newFile.toString())){
                        editAudioTag(context,newFile,false,true);
                    }
                }
            }
        }
    }
}
