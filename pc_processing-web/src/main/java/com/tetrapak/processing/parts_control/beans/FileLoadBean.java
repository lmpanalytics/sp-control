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
 * The file reader handles blank rows and cells.
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
                        Map<Integer, Inventory> inventoryMap = readInventoryFile();
                        break;
                    case "Purchases":
                        Map<Integer, Purchases> purchaseMap = readPurchaseFile();
                        break;
                    case "Task List":
                        Map<Integer, TaskList> taskListMap = readTaskListFile();
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
    private Map<Integer, Inventory> readInventoryFile() {
        Map<Integer, Inventory> m = new HashMap<>();
        Integer rowCounter = 0;

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
                if (rowCounter != Integer.MAX_VALUE && rowCounter > 1) {
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
    private Map<Integer, Purchases> readPurchaseFile() {
        Map<Integer, Purchases> m = new HashMap<>();
        Integer rowCounter = 0;

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
    private Map<Integer, TaskList> readTaskListFile() {
        Map<Integer, TaskList> m = new HashMap<>();

        try {
            int MY_MINIMUM_COLUMN_COUNT = 17;

            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            // Decide which rows to process
            int rowStart = sheet.getFirstRowNum();
            int rowEnd = sheet.getLastRowNum();

            for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) {
                Row r = sheet.getRow(rowNum);

                // Initiate at each new row 
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

                if (r == null) {
                    // This whole row is empty
                    // Handle it as needed
                    continue;
                }

                int lastColumn = Math.max(r.getLastCellNum(), MY_MINIMUM_COLUMN_COUNT);

                for (int cn = 0; cn <= lastColumn; cn++) {
                    Cell c = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (c == null) {
                        // The spreadsheet is empty in this cell
                    } else {
                        // Do something useful with the cell's contents

                        if (c.getCellTypeEnum() == CellType.STRING) {
                            String s = c.getStringCellValue();
//                        System.out.print(s + " I'm a String ");
                            switch (cn) {
                                case 1:
                                    label = s;
                                    break;
                                case 2:
                                    classItem = s;
                                    break;
                                case 3:
                                    articleNo = s;
                                    break;
                                case 4:
                                    eqDenomination = s;
                                    break;
                                case 5:
                                    type = s;
                                    break;
                                case 6:
                                    docNo = s;
                                    break;
                                case 9:
                                    action = s;
                                    break;
                                case 10:
                                    description = s;
                                    break;
                                case 11:
                                    sparePartNo = s;
                                    break;
                                case 12:
                                    spDenomination = s;
                                    break;
                                case 17:
                                    functionalArea = s;
                                    break;
                                default:
                                    break;
                            }

                        } else if (c.getCellTypeEnum() == CellType.NUMERIC) {
                            Double d = c.getNumericCellValue();
//                        System.out.print(d + " I'm a number ");
                            switch (cn) {
                                case 0:
                                    machineNumber = d.intValue();
                                    break;
                                case 1:
                                    label = String.valueOf(d.intValue());
                                    break;
                                case 2:
                                    classItem = String.valueOf(d.intValue());
                                    break;
                                case 3:
                                    articleNo = String.valueOf(d.intValue());
                                    break;
                                case 6:
                                    docNo = String.valueOf(d.intValue());
                                    break;
                                case 8:
                                    interval = d.intValue();
                                    break;
                                case 11:
                                    sparePartNo = String.valueOf(d.intValue());
                                    break;
                                case 13:
                                    qty = d.intValue();
                                    break;
                                default:
                                    break;
                            }

                        }

                    }
                }

                //   Put to map starting from row 2 in the Excel sheet (idx 1)
                if (rowNum != Integer.MAX_VALUE && rowNum >= 1) {
//                    System.out.printf("Task List file row: %s\t { %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s }", rowNum, machineNumber, label, classItem, articleNo, eqDenomination, type, docNo, interval, action, description, sparePartNo, spDenomination, qty, functionalArea);
                    m.put(rowNum, new TaskList(machineNumber, label, classItem, articleNo, eqDenomination, type, docNo, interval, action, description, sparePartNo, spDenomination, qty, functionalArea));
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
    private void addTaskList(Map<Integer, TaskList> taskListMap) {
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
