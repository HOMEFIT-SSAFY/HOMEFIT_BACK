package com.happyhome.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MemberSchemaMigration implements ApplicationRunner {

    private static final String MEMBERS_TABLE = "members";
    private static final String RENTAL_NOTICE_EMAIL_COLUMN = "rental_notice_email_enabled";

    private final DataSource dataSource;

    public MemberSchemaMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        addRentalNoticeEmailColumnIfMissing();
    }

    void addRentalNoticeEmailColumnIfMissing() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (hasColumn(connection, RENTAL_NOTICE_EMAIL_COLUMN)) {
                return;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        ALTER TABLE members
                        ADD COLUMN rental_notice_email_enabled BOOLEAN NOT NULL DEFAULT FALSE
                        """);
            }
        }
    }

    private boolean hasColumn(Connection connection, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        try (ResultSet columns = metaData.getColumns(catalog, null, MEMBERS_TABLE, columnName)) {
            return columns.next();
        }
    }
}
