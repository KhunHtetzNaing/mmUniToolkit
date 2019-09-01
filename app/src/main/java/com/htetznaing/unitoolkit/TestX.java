package com.htetznaing.unitoolkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.htetznaing.unitoolkit.Utils.AIOmmTool;

import java.util.ArrayList;
import static android.database.Cursor.FIELD_TYPE_STRING;

public class TestX extends AppCompatActivity {
    private SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_x);

        String path = Environment.getExternalStorageDirectory()+"/backups/apps/DamhaDB.db";
        database = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(path).getAbsolutePath(), null);

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                for (String a:getTable()){
                    printTableData(a);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(TestX.this, "Done", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private ArrayList<String> getTable(){
        ArrayList<String> tb = new ArrayList<>();
        Cursor cursor = database.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            String table = cursor.getString(0);
            if (!table.equals("android_metadata") && !table.equals("sqlite_sequence")){
                tb.add(table);
            }
        }
        cursor.close();
        return tb;
    }

    public void printTableData(String table_name){
        Cursor cur = database.rawQuery("SELECT * FROM " + table_name, null);
        if(cur.getCount() != 0){
            cur.moveToFirst();
            do{
                for(int i = 0 ; i < cur.getColumnCount(); i++){
                    if (cur.getType(i)==FIELD_TYPE_STRING) {
                        String key = cur.getColumnNames()[i];
                        System.out.println(key);
                        String data = cur.getString(i);
                        System.out.println(data);
                        update(table_name, key, data);
                    }
                }
            }while (cur.moveToNext());
        }
    }

    public void update(String DB_NAME,String key,String data) {
        String where = key+"=?";
        String[] whereArgs = new String[] {
                String.valueOf(data)
        };

        ContentValues values = new ContentValues();
        values.put(key, AIOmmTool.zawgyi2Unicode(data));
        database.update(DB_NAME, values, where, whereArgs);
    }

    private ArrayList<String> getColumns(String table) {
        ArrayList<String> columns = new ArrayList<>();
        Cursor cursor = database.rawQuery("PRAGMA table_info(" + table + ")", null);
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1));
        }
        cursor.close();
        return columns;
    }
}
