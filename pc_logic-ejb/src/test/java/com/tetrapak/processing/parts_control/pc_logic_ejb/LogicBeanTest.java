/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_logic_ejb;

import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_models.LogicParameters;
import com.tetrapak.processing.parts_control.pc_models.TaskListEvent;
import com.tetrapak.processing.parts_control.pc_models.TaskListMetaData;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jServiceBean;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Ignore;
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
    @Ignore("Not ready to test yet")
    @Test
    @InSequence(2)
    public void testCalculateInventory() {

        System.out.println("Test Calculate Inventory");
        TaskListMetaData taskListMetaData = new TaskListMetaData("PC_100", "project2", "2", "2018-03-24", "SEPALMM");
        LogicParameters logicParameters = new LogicParameters(0, 500, 0);
        logicBean.calculateInventory(taskListMetaData, logicParameters);

//        Map<String, Inventory> map = logicBean.getRecommendedMaterialMap();
        Assert.assertTrue(logicBean.getRecommendedMaterialMap().containsKey("  90606-5824"));
    }

    @Test
    @InSequence(1)
    public void testProcessingOfEvents() {
        System.out.println("Test Processing of Task list events");

        List<TaskListEvent> testEvents = new ArrayList<>();

//        isStocked: true (item to keep an eye on before planned change)
        testEvents.add(new TaskListEvent("check", "piston", "00000000001", "spDenomination", 1, "NA"));
        testEvents.add(new TaskListEvent("change", "piston", "00000000001", "spDenomination", 1, "NA"));

//        isStocked: true (item to keep an eye on before planned turn)        
        testEvents.add(new TaskListEvent("check", "disc", "00000000002", "spDenomination", 25, "NA"));
        testEvents.add(new TaskListEvent("turn", "disc", "00000000002", "spDenomination", 25, "NA"));

//        isStocked: true (not planned to change)        
        testEvents.add(new TaskListEvent("check", "gear box", "00000000003", "spDenomination", 30, "NA"));

//        isStocked: true (not planned to change)        
        testEvents.add(new TaskListEvent("check", "plate", "00000000004", "spDenomination", 0, "NA"));
        testEvents.add(new TaskListEvent("check", "plate", "00000000004", "spDenomination", 0, "NA"));

//        isStocked: true (not planned to change)        
        testEvents.add(new TaskListEvent("check", "piston seal", "00000000005", "spDenomination", 0, "NA"));
        testEvents.add(new TaskListEvent("check", "piston seal", "00000000005", "spDenomination", 0, "NA"));
        testEvents.add(new TaskListEvent("change", "piston seal", "00000000005", "spDenomination", 0, "NA"));

//        isStocked: false (planned maintenance event)         
        testEvents.add(new TaskListEvent("turn", "disc", "00000000006", "spDenomination", 0, "NA"));

//        isStocked: false (planned maintenance event)        
        testEvents.add(new TaskListEvent("change", "disc", "00000000006", "spDenomination", 0, "NA"));

        Assert.assertEquals(1, logicBean.processEvents(testEvents).get(1).getQuantity());
        Assert.assertEquals(2, logicBean.processEvents(testEvents).get(3).getQuantity());
        Assert.assertEquals(2, logicBean.processEvents(testEvents).get(4).getQuantity());

    }
}
