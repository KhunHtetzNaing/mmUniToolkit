package com.htetznaing.unitoolkit.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.htetznaing.app_updater.AppUpdater;
import com.htetznaing.unitoolkit.Constants;
import com.htetznaing.unitoolkit.R;
import com.htetznaing.unitoolkit.Utils.CheckInternet;
import com.htetznaing.unitoolkit.Utils.Toolkit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    int WHAT=0;
    final int VIDEO=2,AUDIO=3,IMAGE=4,CONTACTS=5;
    LinearLayout rootLayout;
    SharedPreferences sharedPreferences;
    String zgORuni = null;
    public static MainActivity instance;
    //Update
    AppUpdater appUpdater;
    CheckInternet checkInternet;

    TextView dev_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        rootLayout = findViewById(R.id.rootLayout);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        dev_text = findViewById(R.id.dev_text);
        dev_text.append(" [v"+getVersion()+"]");
        appUpdater = new AppUpdater(this,"https://myappupdateserver.blogspot.com/2019/07/mmunicodetookit.html");
        checkInternet = new CheckInternet(this);

        if (sharedPreferences.getBoolean("first_time",true)){
            sharedPreferences.edit().putBoolean("first_time",false).apply();
            showAbout();
        }
    }

    private String getVersion(){
        String version = "1.0";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_contact = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int write_contacts = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);

        final List<String> listPermissionsNeeded = new ArrayList<>();

        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (read_contact != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }

        if (write_contacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_CONTACTS);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }

        return true;
    }

    private ArrayList<String> getStorage() {
        ArrayList<String> path = new ArrayList<>();
        path.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        return path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100) {
            if (grantResults.length > 0) {
                boolean how = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        how = false;
                        Toast.makeText(this, "You need to allow this permission!", Toast.LENGTH_SHORT).show();
                        checkPermissions();
                        break;
                    }
                }
                if (how){
                    next();
                }
            } else {
                checkPermissions();
                Toast.makeText(this, "You need to allow this permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void next(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("သတိပြုရန်!!")
                .setMessage("သင့်ဖုန်းတွင်ယူနီကုဒ်ဖောင့်အမှန်ဖတ်နိုင်ရန်ဦးစွာလုပ်ဆောင်ပါ။\n" +
                        "ထိုမှသာသင်ပြောင်းလဲလိုက်သော အမည်များကိုမှန်ကန်စွာမြင်ရမည်ဖြစ်သည်။\n" +
                        "သင့်ဖိုင်များလျှင်များသလို အချိန်ကြာနိုင်သည်\n" +
                        "ပြောင်းနေတုန်း ဤစာမျက်နှာမှထွက်မသွားပါနှင့်!")
                .setPositiveButton("စလုပ်မည်", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (WHAT){
                            case AUDIO:changeAudio(false);break;
                            case VIDEO:changeVideo(false);break;
                            case IMAGE:changeImage(false);break;
                            case CONTACTS:changeContacts(false);break;
                        }
                    }
                })
                .setNeutralButton("မလုပ်တော့ပါ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public void changeAudio(View view) {
        WHAT = AUDIO;
        alert(AUDIO);
    }

    private void changeAudio(final boolean force) {
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toolkit.resetPathsAndMimeType();
                progressDialog.setMessage("အသံဖိုင်များ...");
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                ArrayList<String > path = getStorage();
                for (String p:path){
                    Toolkit.audioFileNameToUnicode(MainActivity.this,new File(p),force);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                done(false);
            }
        }.execute();
    }

    public void changeVideo(View view) {
        WHAT = VIDEO;
        alert(VIDEO);
    }

    private void changeVideo(final boolean force) {
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toolkit.resetPathsAndMimeType();
                progressDialog.setMessage("ဗီဒီယိုဖိုင်များ...");
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                ArrayList<String > path = getStorage();
                for (String p:path){
                    Toolkit.videoFileNameToUnicode(MainActivity.this,new File(p),force);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                done(false);
            }
        }.execute();
    }

    public void changeImage(View view) {
        WHAT = IMAGE;
        alert(IMAGE);
    }

    private void changeImage(final boolean force) {
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toolkit.resetPathsAndMimeType();
                progressDialog.setMessage("ဓာတ်ပုံများ...");
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                ArrayList<String > path = getStorage();
                for (String p:path){
                    Toolkit.imageFileNameToUnicode(MainActivity.this,new File(p),force);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                done(false);
            }
        }.execute();
    }

    public void changeContacts(View view) {
        WHAT = CONTACTS;
        alert(CONTACTS);
    }

    private void changeContacts(final boolean force){
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setMessage("အဆက်အသွယ်များ...");
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Toolkit.changeContacts(MainActivity.this,force);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                done(true);
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                showAbout();break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.restore:
                showRestoreDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("အသိပေးချက်")
                .setMessage(Html.fromHtml("<p>သင့်ဖုန်းအတွင်းရှိဇော်ဂျီဖြင့်ရေးသားထားသမျှကို<br />ယူနီကုဒ်သို့ပြောင်းရွေ့ရန်ကူညီပေးနိုင်သော<br />Application ဖြစ်ပါသည်။</p>\n" +
                        "<pre>သင့်ဖုန်းတွင်ယူနီကုဒ်ဖောင့်အမှန်ဖတ်နိုင်ရန်ဦးစွာလုပ်ဆောင်ပါ။<br />ထိုမှသာသင်ပြောင်းလဲလိုက်သော အမည်များကိုမှန်ကန်စွာမြင်ရမည်ဖြစ်သည်။<br />သင့်ဖိုင်များလျှင်များသလို အချိန်ကြာနိုင်သည်<br />ပြောင်းနေတုန်း စောင့်နေပါ ဘယ်မှထွက်မသွားပါနှင့်!</pre>\n" +
                        "<p><span style=\"color: #ff0000;\"><strong>အားလုံး</strong></span><br />သင့်ဖုန်းအတွင်းရှိသမျှ ဇော်ဂျီနဲ့ရေးထားတာတွေကို<br />အကုန်ယူနီကုဒ်နာမည်နဲ့ပြောင်းပေးသွားမှာပါ။<br />သီချင်း၊ ဗီဒီယို၊ ဓာတ်ပုံ၊ ဖုန်းအဆက်အသွယ်နှင့်<br />အခြားဖုန်းအတွင်းရှိဖိုင်အားလုံးကိုတစ်ချက်တည်းနဲ့ပြောင်းပေးသွားမှာပါ။</p>\n" +
                        "<p><span style=\"color: #ff0000;\"><strong>အသံဖိုင်များ</strong></span><br />အသံဖိုင် (Mp3, m4a, wav, etc..) အားလုံးကို<br />ယူနီကုဒ်နာမည်နဲ့ပြောင်းပေးသွားမှာပါ။<br />အဆိုတော်၊ သီချင်း၊ အယ်လ်ဘမ်အမည်အားလုံးကိုပြောင်းပေးမှာပါ။</p>\n" +
                        "<p><strong><span style=\"color: #ff0000;\">ဗီဒီယိုဖိုင်များ</span></strong><br />Video ဖိုင်အားလုံး (Mp4, 3pg, etc..) အားလုံးကို<br />ယူနီကုဒ်နာမည်နဲ့ပြောင်းပေးသွားမှာပါ။</p>\n" +
                        "<p><span style=\"color: #ff0000;\"><strong>ဓာတ်ပုံများ</strong></span><br />ဓာတ်ပုံ (jpg, png, etc...) အားလုံးကို<br />ယူနီကုဒ်နာမည်နဲ့ပြောင်းပေးသွားမှာပါ။</p>\n" +
                        "<p><strong><span style=\"color: #ff0000;\">ဖုန်းအဆက်အသွယ်များ</span></strong><br />သင့်ဖုန်း Contacts အတွင်းရှိ<br />ဇော်ဂျီဖြင့်ရေးသားထားသောအမည်များအားလုံးကို<br />ယူနီကုဒ်ဖြင့်ပြောင်းပေးသွားမည်။</p>\n" +
                        "<p><strong><span style=\"color: #ff0000;\">အခြား</span></strong><br />အပိုအနေနဲ့ယူနီကုဒ်ထည့်သွင်းရန်လိုအပ်သော<br />zFont - Custom Font Installer ကိုလင့်ခ်ချိတ်ပေးထားပါတယ်။<br /><br /></p>\n" +
                        "<p><span style=\"color: #ff0000;\"><strong>Special thank &amp; credits</strong></span><br />=================<br /><strong>Rabbit</strong><br /><a href=\"https://github.com/Rabbit-Converter/Rabbit\">https://github.com/Rabbit-Converter/Rabbit</a></p>\n" +
                        "<p><strong>Myanmar Tools</strong><br /><a href=\"https://github.com/google/myanmar-tools\">https://github.com/google/myanmar-tools</a></p>\n" +
                        "<p><strong>Jaudiotagger</strong><br /><a href=\"https://github.com/AdrienPoupa/jaudiotagger\">https://github.com/AdrienPoupa/jaudiotagger</a></p>\n" +
                        "<p><strong>NoNonsense-FilePicker</strong><br /><a href=\"https://github.com/spacecowboy/NoNonsense-FilePicker\">https://github.com/spacecowboy/NoNonsense-FilePicker</a></p>"))
                .setPositiveButton("ဟုတ်ပြီ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    private void showRestoreDialog() {
        CharSequence [] items = {"အသံဖိုင်များ","ဗီဒီယိုဖိုင်များ","ဓာတ်ပုံများ","ဖုန်းအဆက်အသွယ်များ"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("မူလအတိုင်းပြန်လည်ရှိမည်")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restore(which);
                    }
                })
                .setPositiveButton("ဟုတ်ပြီ",null);
        builder.show();
    }


    private void restore(final int what){
        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toolkit.resetPathsAndMimeType();
                progressDialog.setMessage("မူလအတိုင်းပြန်လည်ထားရှိနေသည်..");
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                SharedPreferences sharedPreferences;
                Map<String,?> keys;
                switch (what){
                    case 0:
                        //Audio
                        sharedPreferences = getSharedPreferences(Constants.AUDIOS,MODE_PRIVATE);
                        keys = sharedPreferences.getAll();
                        if (keys.size()>0) {
                            Toolkit.restoreFiles(MainActivity.this,keys);
                            return true;
                        }
                        return false;
                    case 1:
                        //Video
                        sharedPreferences = getSharedPreferences(Constants.VIDEOS,MODE_PRIVATE);
                        keys = sharedPreferences.getAll();
                        if (keys.size()>0) {
                            Toolkit.restoreFiles(MainActivity.this,keys);
                            return true;
                        }
                        return false;
                    case 2:
                        //Photo
                        sharedPreferences = getSharedPreferences(Constants.IMAGES,MODE_PRIVATE);
                        keys = sharedPreferences.getAll();
                        if (keys.size()>0) {
                            Toolkit.restoreFiles(MainActivity.this,keys);
                            return true;
                        }
                        return false;
                    case 3:
                        //Contact
                        return Toolkit.restoreContact(MainActivity.this);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                if (aVoid) {
                    if (what==3){
                        done(true);
                    }else done(false);
                }else {
                    Toast.makeText(MainActivity.this, "အရံသိမ်းဆည်းထားခြင်းမရှိသေးပါ။", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void alert(final int WHATz){
        String title = "သတိပေးချက်";
        String message = "";

        switch (WHATz){
            case AUDIO:message="အသံဖိုင်";break;
            case VIDEO:message="ဗီဒီယိုဖိုင်";break;
            case IMAGE:message="ဓာတ်ပုံဖိုင်";break;
            case CONTACTS:message="အဆက်အသွယ်";break;
        }

        if (zgORuni==null) {
            message += "များအားလုံးကို\nယူနီကုဒ်အမည်ဖြင့်ပြောင်းလဲပေးမည်။";
        }else if (zgORuni.equalsIgnoreCase("zawgyi")){
            message += "များအားလုံးကို\nဇော်ဂျီအမည်ဖြင့်ပြောင်းလဲပေးမည်။";
        }else if (zgORuni.equalsIgnoreCase("unicode")){
            message += "များအားလုံးကို\nယူနီကုဒ်အမည်ဖြင့်ပြောင်းလဲပေးမည်။";
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ဆက်လုပ်ပါ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkPermissions()){
                            next();
                        }
                    }
                })
                .setNegativeButton("မလုပ်ပါ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    public void done(final boolean contacts){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contacts) {
                    Snackbar.make(rootLayout, "ပြီးပါပြီ", Snackbar.LENGTH_LONG)
                            .setAction("ဟုတ်ပြီ", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            })
                            .show();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                            .setTitle("အားလုံးပြောင်းပြီးပါပြီ။")
                            .setMessage("အချို့ Player နှင့် Gallery များတွင်ပြောင်းထားတာမြင်နိုင်ရင်\n" +
                                    "Media ဖိုင်များ Update လုပ်ပေးရန်လိုအပ်ပါသည်။\n" +
                                    "သို့မဟုတ်ဖုန်းကို Restart(ပိတ်ပြီးပြန်ဖွင့်) လုပ်၍လည်းရပါသည်။\n" +
                                    "အကောင်းဆုံးကတော့ဖုန်းကို Restart လုပ်လိုက်ပါ။\n" +
                                    "သို့မဟုတ် အောက်ပါ Normal Update နဲ့လုပ်လိုက်ပါ။\n" +
                                    "\n" +
                                    "Force Update ကတော့\n" +
                                    "ပြောင်းခဲ့သမျှဖိုင်များအားလုံးကို Media အမည် Scan ပေးသွားမည်ဖြစ်သည်။\n" +
                                    "ထိုကြောင့်တချို့ Hidden ဖိုင်များလည်း Media အဖြစ် Update လုပ်ပေးသောကြောင့်\n" +
                                    "မလိုအပ်လျှင်မသုံးပါနှင့်!\n")
                            .setPositiveButton("Normal Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toolkit.scanMediaNormal(instance);
                                }
                            })
                            .setNegativeButton("Force Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toolkit.updateMEDIA(instance);
                                }
                            })
                            .setNeutralButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public void zfont(View view) {
        openPlayStore("com.mgngoe.zfont");
    }


    private void openPlayStore(String appPackageName){
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        }
    }

    private void openFb(String userId){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setData(Uri.parse("fb://profile/"+userId));
            startActivity(intent);
        } catch (Exception e) {
            intent.setData(Uri.parse("https://m.facebook.com/"+userId));
            startActivity(intent);
        }
    }

    public void dev(View view) {
        openFb("100030031876000");
    }

    public void customize(View view) {
        startActivity(new Intent(this, CustomizeActivity.class));
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("အသိပေးချက်")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("ထွက်တော့မှာလား ?")
                .setPositiveButton("ဟုတ်", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("မထွက်သေးပါ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkInternet.isInternetOn()){
            appUpdater.check(false);
        }
    }

    public void sdcard(View view) {
        startActivity(new Intent(this,SDCardActivity.class));
    }
}
