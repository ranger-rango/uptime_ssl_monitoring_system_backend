package com.monitoringsystem.db_handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseOperationsHikari
{
    public static void dbInsert()
    {}

    public static void dbFetch()
    {}

    public static ResultSet dbQuery(Connection connection, String sql, List<Object> sqlParams) throws SQLException
    {
        ResultSet resultSet = null;

        try
        {
            connection.setAutoCommit(false);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (int i = 0; i < sqlParams.size(); i++)
            {
                pstmt.setObject(i + 1, sqlParams.get(i));
            }
            boolean hasResultSet = pstmt.execute();
            if (hasResultSet)
            {
                resultSet = pstmt.getResultSet();
            }
            else
            {
                int rowsAffected = pstmt.getUpdateCount();
                System.out.println(rowsAffected + "row(s) affected.");
            }
            connection.commit();
        }
        catch (SQLException e)
        {
            connection.rollback();
            e.printStackTrace();
        }

        return resultSet;
    }
    
}
