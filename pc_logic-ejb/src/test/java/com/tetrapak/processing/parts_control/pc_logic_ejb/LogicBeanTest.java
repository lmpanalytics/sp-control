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
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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

// ***************************** ADD EVENT OBJECTS *****************************
//        isStocked: true (item to keep an eye on before planned change)
        testEvents.add(new TaskListEvent("Tetra Alex 400", "check", "piston", "00000000001", "spDenomination", 1, "NA"));
        testEvents.add(new TaskListEvent("Tetra Alex 400", "change", "piston", "00000000001", "spDenomination", 1, "NA"));

//        isStocked: true (item to keep an eye on before planned turn)        
        testEvents.add(new TaskListEvent("Tetra Alex 400", "check", "disc", "00000000002", "spDenomination", 25, "NA"));
        testEvents.add(new TaskListEvent("Tetra Alex 400", "turn", "disc", "00000000002", "spDenomination", 25, "NA"));

//        isStocked: true (not planned to change)        
        testEvents.add(new TaskListEvent("Tetra Alex 400", "check", "gear box", "00000000003", "spDenomination", 30, "NA"));

//        isStocked: true (not planned to change)        
        testEvents.add(new TaskListEvent("Tetra Alex 400", "check", "plate", "00000000004", "spDenomination", 1, "NA"));
        testEvents.add(new TaskListEvent("Tetra Alex 400", "check", "plate", "00000000004", "spDenomination", 1, "NA"));

//        isStocked: true (not planned to change)        
        testEvents.add(new TaskListEvent("Tetra Alex 400", "check", "piston seal", "00000000005", "spDenomination", 5, "NA"));
        testEvents.add(new TaskListEvent("Tetra Alex 400", "check", "piston seal", "00000000005", "spDenomination", 5, "NA"));
        testEvents.add(new TaskListEvent("Tetra Alex 400", "change", "piston seal", "00000000005", "spDenomination", 5, "NA"));

//        isStocked: false (planned maintenance event)         
        testEvents.add(new TaskListEvent("Tetra Alex 400", "turn", "disc", "00000000006", "spDenomination", 100, "NA"));

//        isStocked: false (planned maintenance event)        
        testEvents.add(new TaskListEvent("Tetra Alex 400", "change", "disc", "00000000006", "spDenomination", 100, "NA"));

//        Shuffle the order of events to make the test harder
        Collections.shuffle(testEvents);

// ******************** TEST EVENT OBJECT IN LIST POSITION *********************
//        Assert.assertEquals(1, 1);
        List<Inventory> inventory = logicBean.processEvents(testEvents);
        inventory.forEach(sp -> {
            if (sp.getMaterial().equals("00000000001")) {
                Assert.assertThat(sp.getQuantity(), equalTo(1));
//                Assert.assertEquals(1, sp.getQuantity());
            } else if (sp.getMaterial().equals("00000000002")) {
                Assert.assertEquals(2, sp.getQuantity());
            } else if (sp.getMaterial().equals("00000000003")) {
                Assert.assertEquals(2, sp.getQuantity());
            }

        });
        Assert.assertThat(inventory, hasSize(equalTo(5)));
    }
}
