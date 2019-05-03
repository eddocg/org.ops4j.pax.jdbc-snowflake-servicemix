/*
 * Copyright 2012 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.test.mysql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

@RunWith(PaxExam.class)
public class MysqlNativeDataSourceTest extends AbstractJdbcTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Inject
    @Filter("(osgi.jdbc.driver.name=mysql)")
    private DataSourceFactory dsf;

    @Rule
    public ServerConfiguration dbConfig = new ServerConfiguration("mysql");

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-mysql"), //
            mvnBundle("mysql", "mysql-connector-java") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        dsf.createDataSource(props).getConnection().close();
    }

    @Test
    public void connectWithDefaultPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(3306));

        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        dsf.createDataSource(props).getConnection().close();
    }

    @Test
    public void connectWithDefaultHostAndPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(3306));
        assumeThat(dbConfig.getServerName(), is("localhost"));

        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        dsf.createDataSource(props).getConnection().close();
    }

    @Test
    public void failOnMissingPassword() throws SQLException {
        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        
        thrown.expect(SQLException.class);
        thrown.expectMessage("Access denied");
        thrown.expectMessage("using password: NO");
        dsf.createDataSource(props).getConnection().close();
    }

    @Test
    public void failOnWrongPassword() throws SQLException {
        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "wrong");

        thrown.expect(SQLException.class);
        thrown.expectMessage("Access denied");
        thrown.expectMessage("using password: YES");
        dsf.createDataSource(props).getConnection().close();
    }
}
