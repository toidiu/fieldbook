package toidiu.com.fieldnotebook.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.models.NewFileObj;

/**
 * Created by toidiu on 1/18/15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    private static final String DATABASE_FILE_NAME = "SJ";
    private static final int VERSION = 1;

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private static DatabaseHelper instance;
    // @reminder Ordering matters, create foreign key dependant classes later
    private final Class[] tableClasses = new Class[]{FileObj.class, FileUploadObj.class, NewFileObj.class};

    private DatabaseHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            for (Class mTableClass : tableClasses) {
                TableUtils.createTable(connectionSource, mTableClass);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        for (int i = tableClasses.length - 1; i >= 0; i--) {
            Class tableClass = tableClasses[i];
            try {
                TableUtils.dropTable(connectionSource, tableClass, true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        onCreate(database, connectionSource);
    }

    public void runInTransaction(TransactionRunnable r) throws Exception {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            r.run();
            writableDatabase.setTransactionSuccessful();
        } finally {
            writableDatabase.endTransaction();
        }
    }

    public Dao<FileObj, Integer> getClaimProjDao() throws SQLException {
        return getDao(FileObj.class);
    }

    public Dao<FileUploadObj, Integer> getFileUploadDao() throws SQLException {
        return getDao(FileUploadObj.class);
    }

    public Dao<NewFileObj, Integer> getNewFileObjDao() throws SQLException {
        return getDao(NewFileObj.class);
    }
}
