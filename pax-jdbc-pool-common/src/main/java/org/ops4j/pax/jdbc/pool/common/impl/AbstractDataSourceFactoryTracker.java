/*
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
package org.ops4j.pax.jdbc.pool.common.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.transaction.TransactionManager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSourceFactory services and creates/destroys a PooledDataSourceFactory for each
 * existing DataSourceFactory
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractDataSourceFactoryTracker extends
    ServiceTracker<DataSourceFactory, ServiceRegistration<DataSourceFactory>> {

    private Logger LOG = LoggerFactory.getLogger(AbstractDataSourceFactoryTracker.class);
    protected final TransactionManager tm;

    public AbstractDataSourceFactoryTracker(BundleContext context) {
        this(context, null);
    }

    public AbstractDataSourceFactoryTracker(BundleContext context, TransactionManager tm) {
        super(context, DataSourceFactory.class, null);
        this.tm = tm;
    }

    @Override
    public ServiceRegistration addingService(ServiceReference<DataSourceFactory> reference) {
        if (reference.getProperty("pooled") != null) {
            // Make sure we do not react on our own service for the pooled factory
            return null;
        }
        return createAndRegisterPooledFactory(reference);
    }

    protected TransactionManager getTransactionManager() {
        return tm;
    }

    private ServiceRegistration<DataSourceFactory> createAndRegisterPooledFactory(
        ServiceReference<DataSourceFactory> reference) {
        LOG.debug("Registering PooledDataSourceFactory");
        DataSourceFactory dsf = context.getService(reference);
        DataSourceFactory pdsf = createPooledDatasourceFactory(dsf);
        Dictionary<String, Object> props = createPropsForPoolingDataSourceFactory(reference);
        LOG.debug("Registering PooledDataSourceFactory: " + props);
        return context.registerService(DataSourceFactory.class, pdsf, props);
    }

    private Dictionary<String, Object> createPropsForPoolingDataSourceFactory(
        ServiceReference<DataSourceFactory> reference) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        for (String key : reference.getPropertyKeys()) {
            if (!"service.id".equals(key)) {
                props.put(key, reference.getProperty(key));
            }
        }
        props.put("pooled", "true");
        if (getTransactionManager() != null) {
            props.put("xa", "true");
        }
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, getPoolDriverClass(reference));
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, getPoolDriverName(reference));
        return props;
    }
    
    private String getPoolDriverClass(ServiceReference<DataSourceFactory> reference) {
        String origName = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        return origName + "-pool" + ((getTransactionManager() != null) ? "-xa" : "");
    }

    private String getPoolDriverName(ServiceReference<DataSourceFactory> reference) {
        String origName = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        if (origName == null) {
            origName = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        }
        return origName + "-pool" + ((getTransactionManager() != null) ? "-xa" : "");
    }

    protected abstract DataSourceFactory createPooledDatasourceFactory(DataSourceFactory dsf);

    @Override
    public void modifiedService(ServiceReference<DataSourceFactory> reference,
        ServiceRegistration<DataSourceFactory> reg) {
    }

    @Override
    public void removedService(ServiceReference<DataSourceFactory> reference, ServiceRegistration<DataSourceFactory> reg) {
        LOG.debug("Unregistering PooledDataSourceFactory");
        reg.unregister();
        super.removedService(reference, reg);
    }

}
