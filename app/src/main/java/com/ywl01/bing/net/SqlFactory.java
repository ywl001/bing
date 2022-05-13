package com.ywl01.bing.net;


import com.microsoft.maps.GeoboundingBox;
import com.ywl01.bing.activities.AddMarkerActivity;

import java.util.Map;

/**
 * Created by ywl01 on 2017/3/12.
 */

public class SqlFactory {
    public static String selectMarkersByBound(GeoboundingBox bound, double zoomLevel, String tableName) {
        double minY = bound.getSouth();
        double minX = bound.getWest();
        double maxY = bound.getNorth();
        double maxX = bound.getEast();

        String sql1 = "select * from " + tableName + " where" +
                " x > " + minX +
                " and x < " + maxX +
                " and y > " + minY +
                " and y < " + maxY +
                " and displayLevel < " + zoomLevel;
        System.out.println(sql1);
        return sql1;
    }

    public static String selectRoad(GeoboundingBox bound, double zoomLevel, String tableName) {
        double minY = bound.getSouth();
        double minX = bound.getWest();
        double maxY = bound.getNorth();
        double maxX = bound.getEast();

        String sql1 = "select name,width,ST_AsText(shape) shape from " + tableName + " where" +
                " x > " + minX +
                " and x < " + maxX +
                " and y > " + minY +
                " and y < " + maxY +
                " and displayLevel < " + zoomLevel;
        System.out.println(sql1);
        return sql1;
    }

    public static String selectMarkerBySearch(String keyword) {
        String sql = "select * from monitor where " +
                "monitorID like '%" + keyword + "%' or " +
                "name like '%" + keyword + "%' or " +
                "owner like '%" + keyword + "%'";
        return sql;
    }

    public static String selectMonitorImage(long monitorID) {
        String sql = "select * from monitor_image where monitorID = " + monitorID;
        return sql;
    }

    public static String selectCount() {
        String sql = "select count(*) count, 'house' tableName from house where insertUser = " + User.id + " UNION " +
                "select count(*) count, 'building' tableName from building where insertUser = " + User.id + " UNION " +
                "select count(*) count, 'marker' tableName from marker where insertUser = " + User.id;
        System.out.println(sql);
        return sql;
    }

    public static String selectUser(String userName, String password) {
        String sql = "select * from user where userName = '" + userName + "' and password = '" + password + "'";
        return sql;
    }

    public static String checkUser(String userName) {
        String sql = "select * from user where userName = '" + userName + "'";
        return sql;
    }

    public static String selectPeople(String pname) {
        String sql = "select * from people where name like '%" + pname + "%'";
        return sql;
    }

    public static String insert(String tableName, Map<String, String> data) {
        String sql = "insert into " + tableName + " (";
        for (String key : data.keySet()) {
            sql += key + ",";
        }
        sql = sql.substring(0, sql.length() - 1) + ") values (";

        for (String key : data.keySet()) {
            String value = data.get(key);
            if (value.equals("now()"))//php now（）函数，不能带引号
                sql += value + ",";
            else
                sql += "'" + value + "',";
        }

        sql = sql.substring(0, sql.length() - 1) + ")";
        System.out.println(sql);
        return sql;
    }

    public static String delete(String tableName, long id) {
        String sql = "delete from " + tableName + " where id = " + id;
        return sql;
    }

    public static String update(String tableName, Map<String, String> data, long id) {
        String sql = "update " + tableName + " set ";

        for (String key : data.keySet()) {
            String value = data.get(key);
            sql += (key + "='" + value + "',");
        }
        sql = sql.substring(0, sql.length() - 1) + " where id =" + id;
        System.out.println(sql);
        return sql;
    }
}
