package io.hgc.exwhy.data;

import io.hgc.exwhy.tables.records.ContributorRecord;
import io.hgc.exwhy.tables.records.PostRecord;
import org.jooq.*;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static io.hgc.exwhy.Tables.*;
import static org.jooq.impl.DSL.*;

public class Main {
    public static void main(String[] args) {
        Connection connection = null;

        String userName = "xyadmin";
        String password = "123dev";
        String url = "jdbc:postgresql://localhost:5432/exwhy";

        try {
            Class.forName("org.postgresql.Driver").newInstance();
            connection = DriverManager.getConnection(url, userName, password);

            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
            DSLContext create = using(configuration);
            Result<Record> result = create
                    .select()
                    .from(POST)
                    .join(CONTRIBUTOR)
                    .on(POST.CONTRIBUTOR_ID.eq(CONTRIBUTOR.ID))
                    .fetch();

            for (Record r : result) {
                PostRecord post = r.into(POST);
                ContributorRecord contributor = r.into(CONTRIBUTOR);

                System.out.println(
                        "ID: " + post.getItemid() +
                        " title: " + post.getTitle() +
                        " body: " + post.getBody() +
                        " author: " + contributor.getUsername());
            }
        } catch (Exception e) {
            // For the sake of this tutorial, let's keep exception handling simple
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }
}