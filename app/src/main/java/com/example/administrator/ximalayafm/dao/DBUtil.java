package com.example.administrator.ximalayafm.dao;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库操作类
 *
 * @author Administrator
 */
public class DBUtil {
    private static DBUtil mInstance;
    private DBHelper mSQLHelp;
    private SQLiteDatabase mSQLiteDatabase;

    private DBUtil(Context context) {
        mSQLHelp = new DBHelper(context);
        mSQLiteDatabase = mSQLHelp.getWritableDatabase();
    }

    private DBUtil(Context context, int version) {
        mSQLHelp = new DBHelper(context,"","",version);
        mSQLiteDatabase = mSQLHelp.getWritableDatabase();
    }

    /**
     * 初始化数据库操作DBUtil类
     */
    public static DBUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBUtil(context);
        }
        return mInstance;
    }
    public static DBUtil getUpVersionInstance(Context context, int version) {
        if (mInstance == null) {
            mInstance = new DBUtil(context, version);
        }
        return mInstance;
    }

    /**
     * 关闭数据库
     */
    public void close() {
        mSQLHelp.close();
        mSQLHelp = null;
        mSQLiteDatabase.close();
        mSQLiteDatabase = null;
        mInstance = null;
    }

    /**
     * 添加数据
     */
    public long insertData(ContentValues values, String table) {
        long res = mSQLiteDatabase.insert(table, null, values);
        return res;
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return mSQLiteDatabase;
    }

    /**
     * 更新数据
     *
     * @param values
     * @param whereClause
     * @param whereArgs
     */
    public void updateData(String table, ContentValues values, String whereClause, String[] whereArgs) {
        System.out.println("updateData+++++++++++++++++");
        mSQLiteDatabase.update(table, values, whereClause, whereArgs);
    }

    /**
     * 删除数据
     *
     * @param whereClause
     * @param whereArgs
     */
    public int deleteData(String table, String whereClause, String[] whereArgs) {
        int del = mSQLiteDatabase.delete(table, whereClause, whereArgs);
        return del;
    }

    /**
     * 查询数据
     *
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @return
     */
    public Cursor selectData(String table, String[] columns, String selection, String[] selectionArgs, String groupBy,
                             String having, String orderBy) {
        Cursor cursor = null;
        if (mSQLiteDatabase != null) {
            cursor = mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);

        }
        return cursor;
    }


}