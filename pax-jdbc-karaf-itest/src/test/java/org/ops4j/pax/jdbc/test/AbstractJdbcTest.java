package org.ops4j.pax.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.karaf.features.FeaturesService;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.service.jdbc.DataSourceFactory;

public class AbstractJdbcTest {
    @Inject
    FeaturesService featuresService;

    protected ConfigurationManager cm = new ConfigurationManager();

    String karafVersion = cm.getProperty("karaf.version");

    MavenUrlReference karafUrl = maven().groupId("org.apache.karaf").artifactId("apache-karaf-minimal")
        .version(karafVersion).type("tar.gz");

    MavenUrlReference paxJdbcRepo() {
        return maven().groupId("org.ops4j.pax.jdbc").artifactId("pax-jdbc-features")
            .classifier("features").type("xml").versionAsInProject();
    }

    protected DataSource createDataSource(DataSourceFactory dsf) throws SQLException {
        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "test");
        props.setProperty(DataSourceFactory.JDBC_USER, "SA");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "");
        return dsf.createDataSource(props);
    }
    
    protected void checkDataSource(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement
            .execute("CREATE TABLE PUBLIC.T1 (col1 INTEGER NOT NULL, col2 CHAR(25), PRIMARY KEY (COL1)) ");
        statement.executeUpdate("insert into t1 (col1, col2) values(101, 'pax-jdbc-h2')");
        ResultSet result = statement.executeQuery("select col1 from t1 where col2 = 'pax-jdbc-h2'");

        while (result.next()) {
            assertEquals(101, result.getInt("col1"));
        }
        result.close();

        statement.execute("DROP TABLE PUBLIC.T1");

        statement.close();
        connection.close();
    }
    
    protected void assertFeatureInstalled(String featureName) throws Exception {
        assertTrue("Required feature " + featureName + " not installed", 
                   featuresService.isInstalled(featuresService.getFeature(featureName)));
    }

    protected Option applyConfig(String configName) {
        return replaceConfigurationFile("etc/" + configName, new File("src/test/resources/" + configName));
    }
    
    protected Option karafDefaults() {
        return composite(
        // KarafDistributionOption.debugConfiguration("5005", true),
            karafDistributionConfiguration().frameworkUrl(karafUrl)
                .unpackDirectory(new File("target/exam")).useDeployFolder(false), //
            configureConsole().ignoreLocalConsole().ignoreRemoteShell(), //
            keepRuntimeFolder());
    }
}
