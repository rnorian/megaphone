package com.norian.megaphone.hibernate;

import org.hibernate.dialect.H2Dialect;

/**
 * Created by rnorian on 5/24/14.
 * @@see https://hibernate.onjira.com/browse/HHH-7002
 * @@see http://stackoverflow.com/questions/12054422/unsuccessful-alter-table-xxx-drop-constraint-yyy-in-hibernate-jpa-hsqldb-standa
 */
public class H2DialectFix extends H2Dialect {
    @Override
    public String getDropSequenceString(String sequenceName) {
        // Adding the "if exists" clause to avoid warnings
        return "drop sequence if exists " + sequenceName;
    }

    @Override
    public boolean dropConstraints() {
        // We don't need to drop constraints before dropping tables, that just
        // leads to error messages about missing tables when we don't have a
        // schema in the database
        return false;
    }
}
