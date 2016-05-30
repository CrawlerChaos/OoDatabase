package com.oo.database.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oo.database.BuildConfig;
import com.oo.database.entity.DaoMaster;
import com.oo.database.entity.DaoSession;


/**
 * Created by YangChao on 16/3/18.
 * DataBaseOpenHelper used to control database interface.
 */

public class DataBaseOpenHelper extends DaoMaster.OpenHelper {


    /**
     * this enum is used to define coloumn type in database
     */
    enum DataBaseColoumnType {
        COLUMN_TYPE_INT,
        COLUMN_TYPE_TEXT,
        COLUMN_TYPE_BOOLEAN,
        COLUMN_TYPE_FLOAT,
        COLUMN_TYPE_DATE

    }


    private DaoSession daoSession;
    private static DataBaseOpenHelper instances;
    private DaoMaster daoMaster;
    private static SQLiteDatabase db;

    private DataBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }


    /**
     * this is single tonge used to get instance of dataBaseHelper
     *
     * @param context applicationContext
     * @return DataBaseOpenHelper
     */
    public static DataBaseOpenHelper getInstances(Context context) {
        init(context);
        return instances;
    }

    public static void init(Context context) {
        if (instances == null)
            instances = new DataBaseOpenHelper(context.getApplicationContext(), BuildConfig.DATABASE_NAME, null);
        if (db == null || !db.isOpen())
            db = instances.getWritableDatabase();
    }

    private DaoMaster getMaster() {
        if (daoMaster == null)
            daoMaster = new DaoMaster(db);
        return daoMaster;
    }

    public DaoSession getSession() {
        if (daoSession == null)
            daoSession = getMaster().newSession();
        return daoSession;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*//sample  add coloumn
        if (!isColumnExist(db, EventDao.TABLENAME, EventDao.Properties.Version.columnName))
            addColumn(db, EventDao.TABLENAME, EventDao.Properties.Version.columnName, DataBaseColoumnType.COLUMN_TYPE_TEXT);
        //sample add table
        EventDao.createTable(db, true);
*/

    }

    private boolean isColumnExist(SQLiteDatabase db, String tableName,
                                  String columnName) {
        boolean result = false;
        if (tableName == null) {
            return false;
        }

        try {
            Cursor cursor;
            String sql = "select count(1) as c from sqlite_master where type ='table' and name ='"
                    + tableName.trim()
                    + "' and sql like '%"
                    + columnName.trim() + "%'";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private void addColumn(SQLiteDatabase db, String tableName, String column, DataBaseColoumnType columnType, boolean nullAble) {
        String nullAbleStr = nullAble ? "" : " NOT NULL ";
        db.execSQL(" ALTER TABLE " + tableName
                + " ADD Column " + column
                + getColumnType(columnType) + nullAbleStr);
    }

    private void addColumn(SQLiteDatabase db, String tableName, String column, DataBaseColoumnType columnType) {
        addColumn(db, tableName, column, columnType, true);
    }

    private String getColumnType(DataBaseColoumnType columnType) {
        switch (columnType) {
            case COLUMN_TYPE_INT:
            case COLUMN_TYPE_BOOLEAN:
            case COLUMN_TYPE_DATE:
                return " INTEGER ";
            case COLUMN_TYPE_TEXT:
                return " TEXT ";
            case COLUMN_TYPE_FLOAT:
                return " REAL ";
            default:
                return " TEXT ";
        }
    }


}
