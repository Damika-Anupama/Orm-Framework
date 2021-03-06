package lk.ijse.dep.orm;

import lk.ijse.dep.orm.annotations.Column;
import lk.ijse.dep.orm.annotations.Entity;
import lk.ijse.dep.orm.annotations.Id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author : Damika Anupama Nanayakkara <damikaanupama@gmail.com>
 * @since : 19/01/2021
 **/
public class ORMUtil {
    public static void init(Properties dbProperties, Class... entities) {
        String username = dbProperties.getProperty("javax.persistence.jdbc.username");
        String password = dbProperties.getProperty("javax.persistence.jdbc.password");
        String url = dbProperties.getProperty("javax.persistence.jdbc.url");
        String driverClassName = dbProperties.getProperty("javax.persistence.jdbc.driver_class");
        //get resources from applications.properties resource bundle

        Connection connection = null;
        String sqlScript = "";

        //normally mysql can run one query at once but "?allowMultiQueries = true" adding this to url we can input
        //multiple queries at once
        if (url.indexOf('?') == -1){
            url += "?allowMultiQueries=true";
        }else{
            if (!url.contains("allowMultiQueries=true")){
                url += "&allowMultiQueries=true";
            }
        }

        if (username == null || password == null || url == null || driverClassName == null) {
            throw new RuntimeException("Unable to initialize ORM framework without user-name, password, url, and driver");
        }

        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException throwables) {
            throw new RuntimeException("Failed to establish the connection", throwables);
        }

        for (Class entity : entities) {
            boolean pk = false;
            Annotation entityAnnotation = entity.getDeclaredAnnotation(Entity.class);

            if (entityAnnotation == null) {
                throw new RuntimeException("Invalid entity class: " + entity.getName());
            }

            String[] split = entity.getName().split("[.]");
            String ddl = "CREATE TABLE " +  split[split.length - 1] + " (\n";

            Field[] declaredFields = entity.getDeclaredFields();
            for (Field declaredField : declaredFields) {

                Column columnAnnotation = declaredField.getDeclaredAnnotation(Column.class);
                Id idAnnotation = declaredField.getDeclaredAnnotation(Id.class);

                if (columnAnnotation != null) {
                    String columnName = (columnAnnotation.name().trim().isEmpty()) ? declaredField.getName() : columnAnnotation.name();
                    String columnDef = null;
                    if (declaredField.getType() == String.class) {
                        columnDef = "VARCHAR(255)";
                    } else if (declaredField.getType() == int.class || declaredField.getType() == long.class ||
                            declaredField.getType() == short.class) {
                        columnDef = "INT";
                    }else if (declaredField.getType() == double.class || declaredField.getType() == float.class ||
                            declaredField.getType() == BigDecimal.class){
                        columnDef = "DECIMAL";
                    }else if (declaredField.getType() == boolean.class){
                        columnDef = "BOOLEAN";
                    }else{
                        throw new RuntimeException("Invalid data type; Supported Data Types are String, BigDecimal and primitive data types");
                    }
                    ddl += columnName + " " + columnDef;

                    if (pk && idAnnotation != null){
                        throw new RuntimeException("Composite keys are not supported yet");
                    }
                    if (idAnnotation != null){
                        pk = true;
                        ddl += " PRIMARY KEY";
                    }
                    ddl += ",\n";
                }
            }

            ddl = ddl.substring(0, ddl.length() - 2) +  ");\n";
            sqlScript += ddl;
        }

        try {
            Statement stm = connection.createStatement();
            System.out.println(sqlScript);
            stm.execute(sqlScript);
            connection.close();
        } catch (SQLException throwables) {
            throw new RuntimeException("Failed to create tables", throwables);
        }
    }

}
