/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_neo4j_service_ejb;

import javax.ejb.Local;
import org.neo4j.driver.v1.Driver;

/**
 * Local business interface for the Neo4jService Bean.
 *
 * @author SEPALMM
 */
@Local
public interface Neo4jService {

    public Driver getDRIVER();

    public void closeNeo4jDriver();

    public void destroyMe();

}
