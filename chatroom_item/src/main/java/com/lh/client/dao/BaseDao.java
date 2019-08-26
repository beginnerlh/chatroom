package com.lh.client.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.lh.util.CommUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * dao层基础类，封装数据源，获取连接、关闭资源等共有操作。
 */
public class BaseDao {
    private static DruidDataSource DATASOURCE;

    //1.加载数据源
    static {
        Properties pros = CommUtil.loadProperties("db.properties");
        try {
            DATASOURCE = (DruidDataSource) DruidDataSourceFactory.
                    createDataSource(pros);
        } catch (Exception e) {
            System.err.println("数据源加载失败");
            e.printStackTrace();
        }
    }
    //2.获取连接
    protected Connection getConnection(){
        try {
            return (Connection)DATASOURCE.getPooledConnection();
        } catch (SQLException e) {
            System.err.println("数据库获取失败");
            e.printStackTrace();
        }

        return null;
    }

    //3.关闭资源
    protected void closeResources(Connection connection, Statement statement){
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void closeResources(Connection connection, Statement statement, ResultSet resultSet){
        closeResources(connection,statement);
        if(resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
