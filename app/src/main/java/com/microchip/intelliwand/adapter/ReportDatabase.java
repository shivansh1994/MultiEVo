package com.microchip.intelliwand.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by e1218969 on 2/13/2017.
 */
public class ReportDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "report1.db";
    public static final String TABLE_NAME = "devices_info";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String PCBA = "PCBA";
    public static final String UDID = "UDID";
    public static final String CONNECTION = "CONNECTION";
    public static final String DATA = "DATA";
    public static final String RESULT = "RESULT";


    public ReportDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table " + TABLE_NAME +"("+TIMESTAMP+" TEXT,"+PCBA+" TEXT,"+UDID+" TEXT,"+CONNECTION+" TEXT,"+DATA+" TEXT,"+RESULT+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String time,String pcba, String udid, String connection, String data, String result )
    {
        SQLiteDatabase sqLiteDatabase=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(TIMESTAMP,time);
        contentValues.put(PCBA,pcba);
        contentValues.put(UDID,udid);
        contentValues.put(CONNECTION,connection);
        contentValues.put(DATA,data);
        contentValues.put(RESULT,result);
        long bool= sqLiteDatabase.insert(TABLE_NAME,"myDb" ,contentValues);
        if(bool==-1) {
            return false;
        }
        else return true;
    }
}
