package com.microsoft.cse.reference.spring.dal.config;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.io.IOException;

@Configuration
@Profile({"development", "default"})
public class DevelopmentConfig extends WebSecurityConfigurerAdapter implements IApplicationConfig {
    @Autowired
    Environment env;

    Logger logger = LoggerFactory.getLogger(DevelopmentConfig.class);

    Net embeddedBindInformation;
    MongodExecutable embeddedMongoInstance;

    @Override
    public DatabaseInformation getDatabaseInformation() throws Exception {
        String connStr = env.getProperty(Constants.ENV_DB_CONNSTR);
        String name;
        if (connStr == null) {
            if (this.embeddedMongoInstance == null) {
                // we need to try to startup embedded mongo
                this.embeddedBindInformation = new Net();
                this.embeddedMongoInstance = this.setupMongoEmbed(this.embeddedBindInformation);
            }

            connStr = "mongodb://localhost:" + this.embeddedBindInformation.getPort();
            name = "test";
        } else {
            name = env.getRequiredProperty(Constants.ENV_DB_NAME);
        }

        return new DatabaseInformation(connStr, name);
    }

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        // In development mode, we ignore all webSecurity features
        // effectively disabling oauth2 token requirements
        webSecurity.ignoring().antMatchers("/**");
    }

    /**
     * Attempts to start a mongo instance, using embedMongo
     * @param bind the net info to bind to
     * @return the instance
     * @throws IOException indicates a failure
     */
    private MongodExecutable setupMongoEmbed(Net bind) throws IOException {
        MongodStarter starter;
        starter = MongodStarter.getDefaultInstance();

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.DEVELOPMENT)
                .net(bind)
                .build();

        MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
        MongodProcess mongod = mongodExecutable.start();
        return mongodExecutable;
    }
}
