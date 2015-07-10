/*
 * === Copyright ===
 *
 *  Copyright (C) 2013 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License along
 *    with this program; if not, write to the Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package au.com.redboxresearchdata.cm.dialect

import org.hibernate.dialect.Dialect
import org.hibernate.dialect.PostgreSQL9Dialect
import org.hibernate.id.PersistentIdentifierGenerator
import org.hibernate.id.SequenceGenerator
import org.hibernate.type.Type

/**
 * from http://grails.1312388.n4.nabble.com/One-hibernate-sequence-is-used-for-all-Postgres-tables-td1351722.html
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
class PostGresDialect extends PostgreSQL9Dialect {

    /**
     * Get the native identifier generator class.
     * @return TableNameSequenceGenerator.
     */
    @Override
    public Class<?> getNativeIdentifierGeneratorClass() {
        return TableNameSequenceGenerator.class;
    }

    /**
     * Creates a sequence per table instead of the default behavior of one
     sequence.
     */
    public static class TableNameSequenceGenerator extends SequenceGenerator {

        /**
         * {@inheritDoc}
         * If the parameters do not contain a {@link SequenceGenerator#SEQUENCE}name, we
         * assign one based on the table name.
         */
        @Override
        public void configure(final Type type, final Properties params, final Dialect dialect) {
            if (params.getProperty(SEQUENCE) == null || params.getProperty(SEQUENCE).length() == 0) {
                String tableName = params.getProperty(PersistentIdentifierGenerator.TABLE);
                if (tableName != null) {
                    params.setProperty(SEQUENCE, "seq_" + tableName);
                }
            }
            super.configure(type, params, dialect);
        }
    }
}

