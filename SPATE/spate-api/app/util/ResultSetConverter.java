package util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import play.api.libs.json.JsArray;
import play.api.libs.json.Json;
import scala.collection.immutable.List;
import scala.collection.mutable.LinkedList;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * Created by costantinos on 24/1/2016.
 */

public class ResultSetConverter {
    public static JSONArray convert(ResultSet rs)
            throws SQLException, JSONException {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();
        if (!rs.isBeforeFirst()) {
            do {

                int numColumns = rsmd.getColumnCount();
                JSONObject obj = new JSONObject();
                for (int i = 1; i < numColumns + 1; i++) {
                    String column_name = rsmd.getColumnName(i);

                    if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
                        obj.put(column_name, rs.getArray(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
                        obj.put(column_name, rs.getInt(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
                        obj.put(column_name, rs.getBoolean(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
                        obj.put(column_name, rs.getBlob(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
                        obj.put(column_name, rs.getDouble(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
                        obj.put(column_name, rs.getFloat(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
                        obj.put(column_name, rs.getInt(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
                        obj.put(column_name, rs.getNString(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
                        obj.put(column_name, rs.getString(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
                        obj.put(column_name, rs.getInt(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
                        obj.put(column_name, rs.getInt(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
                        obj.put(column_name, rs.getDate(column_name));
                    } else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                        obj.put(column_name, rs.getTimestamp(column_name));
                    } else {
                        obj.put(column_name, rs.getObject(column_name));
                    }
                }

                json.put(obj);
            } while (rs.next());
        }
        rs.close();
        return json;
    }


    public static Boolean checkTable(ResultSet rs) throws SQLException {
        return  rs.next();
    }

    public static JSONArray convertCoverageToArray(ResultSet rs)
            throws SQLException, JSONException {
//        ResultSetMetaData rsmd = rs.getMetaData();
        JSONArray listOfLists = new JSONArray();
//        int columns = rsmd.getColumnCount();
        while (rs.next()) {
            JSONArray list = new JSONArray();
            list.put(rs.getDouble(1));
            list.put(rs.getDouble(2));
            list.put(rs.getDouble(3));
            listOfLists.put(list);
        }
        rs.close();
        return listOfLists;
    }

}
