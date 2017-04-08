/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

import java.sql.Timestamp;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * Session Bean implementation class NeoTest
 *
 * @author SEPALMM
 */
@Singleton
@LocalBean
public class Neo4jBean {

    private static final String HOSTNAME = "localhost:7687";
    // 'For most use cases it is recommended to use a single driver instance
    // throughout an application.'
    private final Driver DRIVER = GraphDatabase.driver(
            "bolt://" + HOSTNAME + "", AuthTokens.basic("neo4j", "Tokyo2000"));
//private static final Driver DRIVER = GraphDatabase.driver("bolt://" + HOSTNAME + "", AuthTokens.basic("neo4j", "s7asTaba"));

    /**
     * Default constructor.
     */
    public Neo4jBean() {
    }

    @PreDestroy
    public void destroyMe() {
        DRIVER.session().close();
        DRIVER.close();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(
                timestamp + ": Neo4jDriver in Neo4jBean has been disposed of.");
    }

    /**
     * @return the driver
     */
    public Driver getDriver() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp + ": Aquire Neo4jDriver.");
        return DRIVER;
    }

    /**
     * Close the DB driver
     */
    public void closeNeo4jDriver() {
        DRIVER.close();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp + ": Closed Neo4jDriver.");
    }
}
