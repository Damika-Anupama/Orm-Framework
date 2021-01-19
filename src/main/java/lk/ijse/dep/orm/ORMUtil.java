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
        String username = dbProperties.getProperty("javafx.persistence.jdbc.username");
        String password = dbProperties.getProperty("javafx.persistence.jdbc.password");
        String url = dbProperties.getProperty("javafx.persistence.jdbc.url");
        String driverClassName = dbProperties.getProperty("javafx.persistence..jdbc.driver_class");

        Connection connection =null;
        String sqlScript = "";

        if(!url.contains("?")){
            url += "?allowMultiQueries = true";
        }else {
            if (url.contains("allowMultiQueries = true")){
                url +="&allowMultiQueries = true";
            }
        }

        if (username == null || password == null || url == null || driverClassName == null) {
            throw new RuntimeException("Unable to initialize orm framework without username,password ,url,driverClassName");
        }

        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throw new RuntimeException("Failed to establish connection ",throwables);
        }
        for (Class entity : entities
        ) {
            boolean pk = false;
            Annotation entityAnnotation = entity.getDeclaredAnnotation(Entity.class);
            /*if (entityAnnotation == null) {
                throw new RuntimeException("Invalid entity class:" + entity.getName());
            }*/
            /////////////////creating table////////////////////////
            String[] split = entity.getName().split("[.]");
            String ddl = "CREATE TABLE" + split[split.length - 1] + "(\n";
            Field[] declaredFields = entity.getDeclaredFields();
            for (Field declaredField : declaredFields
            ) {
                Column columnAnnotation = declaredField.getDeclaredAnnotation(Column.class);
                Id idAnnotation = declaredField.getDeclaredAnnotation(Id.class);

                if (columnAnnotation != null) {
                    String columnName = (columnAnnotation.name().trim().isEmpty()) ? (declaredField.getName()) : (columnAnnotation.name());
                    String columnDefinition = null;
                    if (declaredField.getType() == String.class) {
                        columnDefinition = "VARCHAR(255)";
                    } else if (declaredField.getType() == int.class || declaredField.getType() == long.class ||
                            declaredField.getType() == short.class) {
                        columnDefinition = "INT";
                    } else if (declaredField.getType() == float.class || declaredField.getType() == double.class ||
                            declaredField.getType() == BigDecimal.class) {
                        columnDefinition = "DECIMAL";
                    } else {
                        throw new RuntimeException("Invalid data type");
                    }
                    ddl += columnName + " " + columnDefinition;

                    if (pk && idAnnotation != null) {
                        throw new RuntimeException("Composite keys are not supported yet");
                    }
                    if (idAnnotation != null) {
                        ddl += " PRIMARY KEY";
                    }
                    ddl += ",\n";
                }

                ddl += ddl.substring(0,ddl.length()-2)+");";
//                System.out.println(ddl);
                sqlScript+=ddl;
            }


        }
        Statement stm = null;
        try {
            stm = connection.createStatement();
            stm.execute(sqlScript);
            connection.close();
        } catch (SQLException throwables) {
            throw  new RuntimeException("Failed to create tables",throwables);
        }
    }
}
