/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

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

    private static final String HOSTNAME = "localhost";
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

    /**
     * @return the driver
     */
    public Driver getDriver() {
        System.out.println("Aquire Neo4jDriver.");
        return DRIVER;
    }

    /**
     * Close the DB driver
     */
    public void closeNeo4jDriver() {
        DRIVER.close();
        System.out.println("Closed Neo4jDriver.");
    }
}
