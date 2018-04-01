/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_logic_ejb;

import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_models.LogicParameters;
import com.tetrapak.processing.parts_control.pc_models.TaskListMetaData;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jServiceBean;
import java.io.File;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the LogicBean
 *
 * @author SEPALMM
 */
@RunWith(Arquillian.class)
public class LogicBeanTest {

    @Deployment
    public static WebArchive createDeployment() {

        File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies()
                .resolve().withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Logic.class, LogicBean.class,
                        Neo4jService.class, Neo4jServiceBean.class,
                        TaskListMetaData.class, LogicParameters.class, Inventory.class
                ).addAsLibraries(files)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(archive.toString(true));
        return archive;
    }

    @EJB
    private Logic logicBean;

    /**
     * Test of calculateInventory method, of class LogicBean.
     */
    @Test
    @InSequence(1)
    public void testCalculateInventory() {

        System.out.println("calculateInventory");
        TaskListMetaData taskListMetaData = new TaskListMetaData("PC_100", "project2", "2", "2018-03-24", "SEPALMM");
        LogicParameters logicParameters = new LogicParameters(0, 500, 0);
        logicBean.calculateInventory(taskListMetaData, logicParameters);

//        Map<String, Inventory> map = logicBean.getRecommendedMaterialMap();
        Assert.assertTrue(logicBean.getRecommendedMaterialMap().containsKey("  90606-5824"));
    }
}
