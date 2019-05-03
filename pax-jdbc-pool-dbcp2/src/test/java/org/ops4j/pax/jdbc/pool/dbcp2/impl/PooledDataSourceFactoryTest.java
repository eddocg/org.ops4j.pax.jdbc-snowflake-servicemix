package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

public class PooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSourceWithXA() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        XADataSource xads = c.createMock(XADataSource.class);
        EasyMock.expect(dsf.createXADataSource(EasyMock.anyObject(Properties.class))).andReturn(xads).atLeastOnce();
        TransactionManager tm = c.createMock(TransactionManager.class);
        DbcpXAPooledDataSourceFactory pdsf = new DbcpXAPooledDataSourceFactory(tm);
        c.replay();
        DataSource ds = pdsf.create(dsf, createValidProps());
        c.verify();
        Assert.assertEquals(ManagedDataSource.class, ds.getClass());
        
        try {
            pdsf.create(dsf, createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.create(dsf, createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory for property dummy", e.getMessage());
        }
    }

    @Test
    public void testcreateDataSource() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        DataSource exds = c.createMock(DataSource.class);
        EasyMock.expect(dsf.createDataSource(EasyMock.anyObject(Properties.class))).andReturn(exds).atLeastOnce();
        DbcpPooledDataSourceFactory pdsf = new DbcpPooledDataSourceFactory();

        c.replay();
        DataSource ds = pdsf.create(dsf, createValidProps());
        c.verify();
        Assert.assertEquals(PoolingDataSource.class, ds.getClass());
        
        try {
            pdsf.create(dsf, createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.create(dsf, createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.PoolableConnectionFactory for property dummy", e.getMessage());
        }
    }
    
    private Properties createValidProps() {
        Properties props = new Properties();
        props.put("pool.maxTotal", "8");
        props.put("factory.validationQuery", "dummyQuery");
        return props;
    }

    private Properties createInvalidPoolConfig() {
        Properties props = new Properties();
        props.put("pool.dummy", "8");
        return props;
    }

    private Properties createInvalidFactoryConfig() {
        Properties props2 = new Properties();
        props2.put("factory.dummy", "8");
        return props2;
    }

}
