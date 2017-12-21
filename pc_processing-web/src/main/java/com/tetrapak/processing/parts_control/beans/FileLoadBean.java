/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import com.tetrapak.processing.parts_control.models.TaskList;
import com.tetrapak.processing.parts_control.models.Purchases;
import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import static org.neo4j.driver.v1.Values.parameters;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads data from Excel.xlsx files to a Neo4j data base. The bean is
 * request scoped to clear the FileInputStream between file readings.
 *
 * See example here:
 * https://www.mkyong.com/java/apache-poi-reading-and-writing-excel-file-in-java/
 *
 * Import starts from row two in the first sheet in the Excel book. Order of
 * columns are important and all quantities must be integers. Additional columns
 * can be added by the user in the Excel sheet to the right of the last column
 * i.e., after column 3 in the Inventory file, after column 3 in the Purchase
 * file, and after column 18 in the Task list file. Such user added columns will
 * be ignored during file upload.
 *
 * @author Magnus Palm
 */
@Named(value = "fileLoadBean")
@RequestScoped
public class FileLoadBean implements Serializable {

    @Inject
    private FileTypeBean ftb;

    @Inject
    private Neo4jService neo;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLoadBean.class);
    private static FileInputStream excelFile;
    private int relationShipCounter;
    private boolean taskListExceptionFlag1;
    private boolean taskListExceptionFlag2;

    /**
     * Creates a new instance of FileLoadBean
     */
    public FileLoadBean() {
    }

    @PostConstruct
    public void init() {
//        System.out.println("I'm in FileLoadBean init() method");
    }

    /**
     * Handles the file upload
     *
     * @param event
     */
//    @RolesAllowed({"BASIC", "PRO", "DIRECTOR", "MDM"})
    public void handleFileUpload(FileUploadEvent event) {

        try {
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);

//            Access the uploaded file from memory
            this.excelFile = (FileInputStream) event.getFile().getInputstream();

//        Convert the file to Excel format and populate maps
            String fileType = ftb.getFileType();
            // System.out.println("Using file type: " + fileType);
            if (null != fileType) {
                switch (fileType) {
                    case "Inventory":
                        Map<Long, Inventory> inventoryMap = readInventoryFile();
                        break;
                    case "Purchases":
                        Map<Long, Purchases> purchaseMap = readPurchaseFile();
                        break;
                    case "Task List":
                        Map<Long, TaskList> taskListMap = readTaskListFile();
                        addTaskList(taskListMap);
                        break;
                    default:
                        break;
                }
            }

        } catch (IOException ex) {
            LOGGER.error("Exception in handleFileUpload method {}", ex.getMessage());
        }
    }

    /**
     * Reads an Excel file, based on Inventory data
     *
     */
    private Map<Long, Inventory> readInventoryFile() {
        Map<Long, Inventory> m = new HashMap<>();
        Long rowCounter = 0L;

        try {

            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = datatypeSheet.iterator();

            while (rowIterator.hasNext()) {

                rowCounter++;

//                Initiate at each new row
                Row currentRow = rowIterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                int cellCounter = 0;
                String material = "";
                String description = "";
                int quantity = 0;

                while (cellIterator.hasNext()) {

                    cellCounter++;

                    Cell currentCell = cellIterator.next();

                    if (currentCell.getCellTypeEnum() == CellType.STRING) {
                        String s = currentCell.getStringCellValue();
//                        System.out.print(s + " I'm a String ");
                        if (cellCounter == 1) {
                            material = s;
                        } else if (cellCounter == 2) {
                            description = s;
                        }

                    } else if (cellCounter == 3 && currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                        Double d = currentCell.getNumericCellValue();
//                        System.out.print(d + " I'm a number ");
                        quantity = d.intValue();
                    }
                }
                //   Put to map starting from row two in the Excel sheet
                if (rowCounter != Long.MAX_VALUE && rowCounter > 1) {
                    // System.out.printf("Inventory file row: %s\t { %s\t, %s\t, %s }", rowCounter, material, description, quantity);
                    m.put(rowCounter, new Inventory(material, description, quantity));
                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Exception file not found {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Exception in IO {}", e.getMessage());
        }
        return m;
    }

    /**
     * Reads an Excel file, based on Purchase data
     *
     */
    private Map<Long, Purchases> readPurchaseFile() {
        Map<Long, Purchases> m = new HashMap<>();
        Long rowCounter = 0L;

        try {

            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = datatypeSheet.iterator();

            while (rowIterator.hasNext()) {

                rowCounter++;

//                Initiate at each new row
                Row currentRow = rowIterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                int cellCounter = 0;
                String material = "";
                String description = "";
                int quantity = 0;

                while (cellIterator.hasNext()) {

                    cellCounter++;

                    Cell currentCell = cellIterator.next();

                    if (currentCell.getCellTypeEnum() == CellType.STRING) {
                        String s = currentCell.getStringCellValue();
//                        System.out.print(s + " I'm a String ");
                        if (cellCounter == 1) {
                            material = s;
                        } else if (cellCounter == 2) {
                            description = s;
                        }

                    } else if (cellCounter == 3 && currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                        Double d = currentCell.getNumericCellValue();
//                        System.out.print(d + " I'm a number ");
                        quantity = d.intValue();
                    }
                }
                //   Put to map starting from row two in the Excel sheet
                if (rowCounter != Long.MAX_VALUE && rowCounter > 1) {
                    // System.out.printf("Purchase file row: %s\t { %s\t, %s\t, %s }", rowCounter, material, description, quantity);
                    m.put(rowCounter, new Purchases(material, description, quantity));
                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Exception file not found {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Exception in IO {}", e.getMessage());
        }
        return m;
    }

    /**
     * Reads an Excel file, based on Task List data from SSPt
     *
     */
    private Map<Long, TaskList> readTaskListFile() {
        Map<Long, TaskList> m = new HashMap<>();
        Long rowCounter = 0L;

        try {

            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = datatypeSheet.iterator();

            while (rowIterator.hasNext()) {

                rowCounter++;

//                Initiate at each new row
                Row currentRow = rowIterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                int cellCounter = 0;

                int machineNumber = 0;
                String label = "";
                String classItem = "";
                String articleNo = "";
                String eqDenomination = "";
                String type = "";
                String docNo = "";
                int interval = 0;
                String action = "";
                String description = "";
                String sparePartNo = "";
                String spDenomination = "";
                int qty = 0;
                String functionalArea = "";

                while (cellIterator.hasNext()) {

                    cellCounter++;

                    Cell currentCell = cellIterator.next();

                    if (currentCell.getCellTypeEnum() == CellType.STRING) {
                        String s = currentCell.getStringCellValue();
//                        System.out.print(s + " I'm a String ");
                        if (cellCounter == 2) {
                            label = s;
                        } else if (cellCounter == 3) {
                            classItem = s;
                        } else if (cellCounter == 4) {
                            articleNo = s;
                        } else if (cellCounter == 5) {
                            eqDenomination = s;
                        } else if (cellCounter == 6) {
                            type = s;
                        } else if (cellCounter == 7) {
                            docNo = s;
                        } else if (cellCounter == 10) {
                            action = s;
                        } else if (cellCounter == 11) {
                            description = s;
                        } else if (cellCounter == 12) {
                            sparePartNo = s;
                        } else if (cellCounter == 13) {
                            spDenomination = s;
                        } else if (cellCounter == 18) {
                            functionalArea = s;
                        }

                    } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                        Double d = currentCell.getNumericCellValue();
//                        System.out.print(d + " I'm a number ");                       
                        if (cellCounter == 1) {
                            machineNumber = d.intValue();
                        } else if (cellCounter == 9) {
                            interval = d.intValue();
                        } else if (cellCounter == 14) {
                            qty = d.intValue();
                        }

                    }
                }
                //   Put to map starting from row two in the Excel sheet
                if (rowCounter != Long.MAX_VALUE && rowCounter > 1) {
//                    System.out.printf("Task List file row: %s\t { %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s }", rowCounter, machineNumber, label, classItem, articleNo, eqDenomination, type, docNo, interval, action, description, sparePartNo, spDenomination, qty, functionalArea);
                    m.put(rowCounter, new TaskList(machineNumber, label, classItem, articleNo, eqDenomination, type, docNo, interval, action, description, sparePartNo, spDenomination, qty, functionalArea));
                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Exception file not found {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Exception in IO {}", e.getMessage());
        }
        return m;
    }

    /**
     * Adds task list data to the data base
     */
    private void addTaskList(Map<Long, TaskList> taskListMap) {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = neo.getDRIVER().session()) {

            // Exception flags
            taskListExceptionFlag1 = true;
            taskListExceptionFlag2 = true;

            String prefix = "PC_";
            // user input
            String customerNumber = prefix + "100";
            String description = "project2";
            String version = "2";

            // System input
            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();

            String userId = "SEPALMM";

            // Wrapping Cypher in an explicit transaction provides atomicity
            // and makes handling errors much easier.
            // 1. Create TaskList node:
            try (Transaction tx = session.beginTransaction()) {

                tx.run("MERGE (t:TaskList{ id: $id, version: $version, userId: $userId, description: $description }) "
                        + "SET t.year = $year "
                        + "SET t.month = $month "
                        + "SET t.day = $day "
                        + "WITH t "
                        + "MATCH (c:PcCustomer) WHERE c.id = $id "
                        + "MERGE (c)-[:PLANNING]->(t);",
                        parameters("id", customerNumber,
                                "description", description,
                                "version", version,
                                "year", year,
                                "month", month,
                                "day", day,
                                "userId", userId));
                tx.success();  // Mark this write as successful.
                taskListExceptionFlag1 = false;

            } catch (ClientException ce) {
                taskListExceptionFlag1 = true;
                LOGGER.error("Exception in creating TaskList node: {}", ce.getMessage());
            }

            // 2. Create Material to TaskList relationships:
            try (Transaction tx = session.beginTransaction()) {
                relationShipCounter = 0;

                taskListMap.values().stream().forEach((v) -> {
                    String materialNumber = v.getSparePartNo();
                    int quantity = v.getQty();
                    String functionalArea = v.getFunctionalArea();
                    int machineNumberSSPt = v.getMachineNumber();
                    int actionInterval = v.getInterval();
                    String action = v.getAction();
                    tx.run("MATCH (m:PcMaterial) WHERE m.materialNumber = $materialNumber_id "
                            + "MATCH (t:TaskList) WHERE t.id = $id "
                            + "MERGE (m)-[r:LISTED_IN]->(t) "
                            + "SET r.quantity = $qty "
                            + "SET r.functionalArea = $functionalArea "
                            + "SET r.machineNumberSSPt = $machineNumberSSPt "
                            + "SET r.actionInterval = $actionInterval "
                            + "SET r.action = $action;",
                            parameters("id", customerNumber,
                                    "materialNumber_id", materialNumber,
                                    "qty", quantity,
                                    "functionalArea", functionalArea,
                                    "machineNumberSSPt", machineNumberSSPt,
                                    "actionInterval", actionInterval,
                                    "action", action));
                    tx.success();  // Mark this write as successful.
                    relationShipCounter++;
                    taskListExceptionFlag2 = false;
                });

            } catch (ClientException ce) {
                taskListExceptionFlag2 = true;
                LOGGER.error("Exception in Material to TaskList relationships: {}", ce.getMessage());
            }

            if (!taskListExceptionFlag1) {
                LOGGER.info("Succesfully created TaskList node {}." /*, compositeKey */);
            }
            if (!taskListExceptionFlag2) {
                LOGGER.info("Succesfully created {} Material to TaskList relationship(s).", relationShipCounter);
            }

        } catch (Exception e) {
            LOGGER.error("Exception in adding TaskList: {}", e.getMessage());
        }
    }

    @PreDestroy
    void destroyMe() {
        try {
            excelFile.close();
        } catch (IOException ex) {
            LOGGER.error("Exception in closing excelFile reader {}", ex.getMessage());
        }
    }
}
