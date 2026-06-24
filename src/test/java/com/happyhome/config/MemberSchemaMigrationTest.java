package com.happyhome.config;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

class MemberSchemaMigrationTest {

    private final DataSource dataSource = org.mockito.Mockito.mock(DataSource.class);
    private final Connection connection = org.mockito.Mockito.mock(Connection.class);
    private final DatabaseMetaData metaData = org.mockito.Mockito.mock(DatabaseMetaData.class);
    private final ResultSet columns = org.mockito.Mockito.mock(ResultSet.class);
    private final Statement statement = org.mockito.Mockito.mock(Statement.class);

    @Test
    void addsRentalNoticeEmailColumnWhenMissing() throws Exception {
        MemberSchemaMigration migration = new MemberSchemaMigration(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.getCatalog()).thenReturn("ssafyhome");
        when(metaData.getColumns("ssafyhome", null, "members", "rental_notice_email_enabled"))
                .thenReturn(columns);
        when(columns.next()).thenReturn(false);
        when(connection.createStatement()).thenReturn(statement);

        migration.addRentalNoticeEmailColumnIfMissing();

        verify(statement).execute("""
                ALTER TABLE members
                ADD COLUMN rental_notice_email_enabled BOOLEAN NOT NULL DEFAULT FALSE
                """);
    }

    @Test
    void skipsAlterWhenRentalNoticeEmailColumnExists() throws Exception {
        MemberSchemaMigration migration = new MemberSchemaMigration(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.getCatalog()).thenReturn("ssafyhome");
        when(metaData.getColumns("ssafyhome", null, "members", "rental_notice_email_enabled"))
                .thenReturn(columns);
        when(columns.next()).thenReturn(true);

        migration.addRentalNoticeEmailColumnIfMissing();

        verify(connection, never()).createStatement();
    }
}
