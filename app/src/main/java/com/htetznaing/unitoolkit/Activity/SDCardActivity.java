package com.htetznaing.unitoolkit.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.htetznaing.unitoolkit.R;
import com.htetznaing.unitoolkit.Utils.AIOmmTool;
import com.htetznaing.unitoolkit.Utils.Toolkit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import mabbas007.tagsedittext.TagsEditText;

public class SDCardActivity extends AppCompatActivity {
    int REQUEST_CODE = 100;
    DocumentFile pickedDir = null;

    TextView textView;
    TagsEditText tagsEditText;
    Switch changeFolderName;
    ProgressDialog progressDialog;
    LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdcard);

        if (getActionBar()!=null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressDialog = new ProgressDialog(this);
        tagsEditText = findViewById(R.id.tagsEditText);
        textView = findViewById(R.id.tv_paths);
        changeFolderName = findViewById(R.id.changeFolderName);
        rootLayout = findViewById(R.id.rootLayout);

        checkPermissions();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && requestCode==REQUEST_CODE){
            Uri treeUri = resultData.getData();
            if (treeUri!=null){
                pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (pickedDir.exists()){
                    textView.setText(pickedDir.getName());
                }else {
                    textView.setText("");
                    pickedDir = null;
                }
                return;
            }
        }
        pickedDir = null;
        Toast.makeText(this, "ဘာမှမရွေးချယ်ထားပါ။", Toast.LENGTH_SHORT).show();
    }


    private void change(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("သတိပြုရန်!!")
                .setMessage("သင်ပြောင်းမည့်အရာသည် အားလုံးဇော်ဂျီဖြစ်နေရင်\n" +
                        "* မဖြစ်မနေပြောင်းမည် * ကိုရွေးချယ်ပါ။\n" +
                        "ဇော်ဂျီနှင့်ယူနီကုဒ်ရောထွေးနေပါက * အလိုအလျောက် * ကိုရွေးချယ်ပါ။\n" +
                        "အဘယ်ကြောင့်ဆိုသော် ယူနီကုဒ်ဖြင့်ရေးထားသည့်စာများပါဝင်နေပါက\n" +
                        "ယူနီကုဒ်သို့မဖြစ်မနေပြောင်းသောအခါ စာများလုံးဝလွဲသွားမည်ဖြစ်သည်။")
                .setPositiveButton("အလိုအလျောက်", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        letWork(false);
                    }
                })
                .setNegativeButton("မဖြစ်မနေပြောင်းမည်", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        letWork(true);
                    }
                })
                .setNeutralButton("မလုပ်တော့ပါ",null);
        builder.show();
    }

    private void letWork(final boolean force){
        if (pickedDir!=null) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    Toolkit.resetPathsAndMimeType();
                    progressDialog.setMessage("Converting...");
                    progressDialog.show();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    String extension = tagsEditText.getText().toString();
                    if (changeFolderName.isChecked()) {
                        if (pickedDir.isDirectory()){
                            pickedDir.renameTo(AIOmmTool.getUnicode(pickedDir.getName(),force));
                        }
                    }
                    Toolkit.changeSD(pickedDir, extension, changeFolderName.isChecked(), force);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    pickedDir = null;
                    textView.setText("");
                    progressDialog.dismiss();
                    done(false);
                }
            }.execute();
        }else Toast.makeText(this, "ဘာမှရွေးမထားပါ။", Toast.LENGTH_SHORT).show();
    }

    public void done(final boolean done){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (done){
                    Snackbar.make(rootLayout, "ပြီးပါပြီ", Snackbar.LENGTH_LONG)
                            .setAction("ဟုတ်ပြီ", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            })
                            .show();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SDCardActivity.this)
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
                                    Toolkit.scanMediaNormal(SDCardActivity.this);
                                }
                            })
                            .setNegativeButton("Force Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toolkit.updateMEDIA(SDCardActivity.this);
                                }
                            })
                            .setNeutralButton("Done", null);
                    builder.show();
                }
            }
        });
    }

    private void checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final List<String> listPermissionsNeeded = new ArrayList<>();

        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "You need to allow this permission!", Toast.LENGTH_SHORT).show();
                        checkPermissions();
                        break;
                    }
                }
            } else {
                checkPermissions();
                Toast.makeText(this, "You need to allow this permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void chooseFileOrFolder(View view) {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE);
    }

    public void changeNow(View view) {
        change();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
}
