/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import static com.tetrapak.processing.parts_control.beans.Utilities.removeNonDigits;
import com.tetrapak.processing.parts_control.models.PartNumbers;
import com.tetrapak.processing.parts_control.models.TaskList;
import com.tetrapak.processing.parts_control.models.Purchases;
import com.tetrapak.processing.parts_control.models.ImportMaterial;
import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
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
 * Import starts from row two in the first sheet in the Excel book, except for
 * Task-lists where import is filtered on action type. Order of columns are
 * important and all quantities must be integers. Additional columns can be
 * added by the user in the Excel sheet to the right of the last column i.e.,
 * after column 3 in the Inventory file, after column 3 in the Purchase file,
 * and after column 15 in the Task list file. Such user added columns will be
 * ignored during file upload.
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

    @Inject
    private UnknownMaterialsViewBean unknownMaterialsViewBean;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLoadBean.class);
    private Map<String, PartNumbers> globalPartNumberMap;
    private static FileInputStream excelFile;
    private int counter;
    private boolean taskListExceptionFlag1;
    private boolean taskListExceptionFlag2;
    private boolean partNumberExceptionFlag;
    private String sparePartNo;
    private boolean foundIt;
    private boolean gapExceptionFlag;
    private List<ImportMaterial> unknownMaterialsList;

    /**
     * Creates a new instance of FileLoadBean
     */
    public FileLoadBean() {
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT
        // Initialize globalPartNumberMap
        globalPartNumberMap = new HashMap<>();
        foundIt = false;

        // Initiate unknown materials list
        unknownMaterialsList = new ArrayList<>();
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
            excelFile = (FileInputStream) event.getFile().getInputstream();

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
                        collectPartNumbersFromDB();
                        Map<Integer, TaskList> taskListMap = readTaskListFile();
                        addTaskList(taskListMap);
                        unknownMaterialsViewBean.processUnknownMaterials();
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
     * Reads an Excel file, based on Task List(s) formatted acc. to TPPS20057.
     * Returns all valid PMR actions, attempting BW formatted number
     * conversions.
     *
     * @return map of row numbers and Task list instances
     */
    private Map<Integer, TaskList> readTaskListFile() {
        Map<Integer, TaskList> m = new HashMap<>();

        try {
            int MY_MINIMUM_COLUMN_COUNT = 14;

            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            // Decide which rows to process
            int rowStart = sheet.getFirstRowNum();
            int rowEnd = sheet.getLastRowNum();

            for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) {
                Row r = sheet.getRow(rowNum);

                // Initiate at each new row 
                String label = "";
                String classItem = "";
                String articleNo = "";
                String eqDenomination = "";
                String type = "";
                String docNo = "";
                int interval = 0;
                String action = "";
                String description = "";
                sparePartNo = "";
                String spDenomination = "";
                int qty = 0;
//                String functionalArea = "";

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
                            // Extract the String value
                            // Remove any hidden carriage returns in cell contents
                            String s = c.getStringCellValue().replaceAll("[\n\r]", "");
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
//                                case 17:
//                                    functionalArea = s;
//                                    break;
                                default:
                                    break;
                            }

                        } else if (c.getCellTypeEnum() == CellType.NUMERIC) {
                            Double d = c.getNumericCellValue();
//                        System.out.print(d + " I'm a number ");
                            switch (cn) {
                                case 1:
                                    label = String.valueOf(d.longValue()).replaceAll("[\n\r]", "");
                                    break;
                                case 2:
                                    classItem = String.valueOf(d.longValue()).replaceAll("[\n\r]", "");
                                    break;
                                case 3:
                                    articleNo = String.valueOf(d.longValue()).replaceAll("[\n\r]", "");
                                    break;
                                case 6:
                                    docNo = String.valueOf(d.longValue()).replaceAll("[\n\r]", "");
                                    break;
                                case 8:
                                    interval = d.intValue();
                                    break;
                                case 11:
                                    sparePartNo = String.valueOf(d.longValue()).replaceAll("[\n\r]", "");
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

//   Start from row 2 in the Excel sheet (idx 1), filter on action, put to map.
                if (rowNum != Integer.MAX_VALUE && rowNum >= 1
                        && (action.equalsIgnoreCase("check")
                        || action.equalsIgnoreCase("change")
                        || action.equalsIgnoreCase("turn"))) {
                    fixBWmatching();
//                    System.out.printf("Task List file row: %s\t { %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s}", rowNum, label, classItem, articleNo, eqDenomination, type, docNo, interval, action, description, sparePartNo, spDenomination, qty);
                    m.put(rowNum, new TaskList(label, classItem, articleNo, eqDenomination, type, docNo, interval, action, description, sparePartNo, spDenomination, qty));
                }

            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Exception file not found {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Exception in IO {}", e.getMessage());
        } catch (NoSuchElementException e) {
            LOGGER.error("Exception in globalPartNumberMap stream: {}", e.getMessage());
        }
        return m;
    }

    private void fixBWmatching() {
        // Convert task list sparePartNo to BW format
        // Handle NPE
        if (!globalPartNumberMap.isEmpty()) {
            // If  sparePartNo is not in BW format convert, else skip

            if (!globalPartNumberMap.containsKey(sparePartNo)) {
                // if sparePartNo match TP format or Numeric, convert
                // to BW format, else remove all non-digits and
                // compare with map value numeric, else use
                // sparePartNo as is (is excluded from rec. list)
// System.out.printf("SparePartNo %s not in TS60, now processing%n", sparePartNo);

                foundIt = false;
                PartNumbers pn1 = globalPartNumberMap.values().stream()
                        .filter(v -> v.getMaterialNumberTP().equals(sparePartNo)
                        || v.getMaterialNumberNUM().equals(sparePartNo)
                        ).findFirst().
                        orElse(new PartNumbers("NA", "NA", "NA"));

                if (!pn1.getMaterialNumberBW().equals("NA")) {
// System.out.printf("Converting '%s' to '%s'%n", sparePartNo, pn1.getMaterialNumberBW());
                    sparePartNo = pn1.getMaterialNumberBW();
                    foundIt = true;
                } else if (!sparePartNo.equals("")) {
                    String nonDigitPN = removeNonDigits(sparePartNo);
                    PartNumbers pn2 = globalPartNumberMap.values().stream()
                            .filter(v -> v.getMaterialNumberNUM().equals(nonDigitPN)
                            || removeNonDigits(v.getMaterialNumberBW()).equals(nonDigitPN)
                            || removeNonDigits(v.getMaterialNumberTP()).equals(nonDigitPN)
                            ).findFirst().
                            orElse(new PartNumbers("NA", "NA", "NA"));

                    if (!pn2.getMaterialNumberBW().equals("NA")) {
//  System.out.printf("After removeNonDigits converting '%s' to '%s'%n", sparePartNo, pn2.getMaterialNumberBW());
                        sparePartNo = pn2.getMaterialNumberBW();
                        foundIt = true;
                    }
                }

                if (foundIt == false && !sparePartNo.equals("")) {
                    LOGGER.warn("Could not convert material number '{}' to BW format", sparePartNo);
                }
            }
        }
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
            String customerNumber = prefix + Long.toString(System.currentTimeMillis());
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
                counter = 0;

                taskListMap.values().stream().forEach((v) -> {
                    String materialNumber = v.getSparePartNo();
                    int quantity = v.getQty();
//                    String functionalArea = v.getFunctionalArea();
                    int actionInterval = v.getInterval();
                    String action = v.getAction();
                    tx.run("MATCH (m:PcMaterial) "
                            + "WHERE m.materialNumber = $materialNumber_id "
                            + "MATCH (t:TaskList) WHERE t.id = $id "
                            + "CREATE (m)-[r:LISTED_IN]->(t) "
                            + "SET r.quantity = $qty "
                            + "SET r.actionInterval = $actionInterval "
                            + "SET r.action = $action;",
                            parameters("id", customerNumber,
                                    "materialNumber_id", materialNumber,
                                    "qty", quantity,
                                    "actionInterval", actionInterval,
                                    "action", action));
                    tx.success();  // Mark this write as successful.
                    counter++;
                    taskListExceptionFlag2 = false;
                });

            } catch (ClientException ce) {
                taskListExceptionFlag2 = true;
                LOGGER.error("Exception in Material to TaskList relationships: {}", ce.getMessage());
            }

            if (!taskListExceptionFlag1) {
                LOGGER.info("Succesfully created TaskList node {}.", customerNumber);
            }
            if (!taskListExceptionFlag2) {
                LOGGER.info("Attempted {} Material to TaskList relationship(s).", counter);
                listUnknownMaterials(customerNumber, taskListMap);
            }

        } catch (Exception e) {
            LOGGER.error("Exception in adding TaskList: {}", e.getMessage());
        }
    }

    /**
     * Collect Part Numbers from DB
     */
    private void collectPartNumbersFromDB() {
        int materialCounter = 0;
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = neo.getDRIVER().session()) {

            String tx = "MATCH (m:PcMaterial) RETURN "
                    + "m.materialNumberNUM AS materialNumberNUM, "
                    + "m.materialNumber AS materialNumberBW, "
                    + "m.materialNumberTP AS materialNumberTP;";

            StatementResult result = session.run(tx);
            partNumberExceptionFlag = false;
            while (result.hasNext()) {
                Record next = result.next();

                String materialNumberNUM = next.get("materialNumberNUM").asString();
                String materialNumberBW = next.get("materialNumberBW").asString();
                String materialNumberTP = next.get("materialNumberTP").asString();

                // Add to globalPartNumberMap
                globalPartNumberMap.put(materialNumberBW, new PartNumbers(materialNumberNUM, materialNumberBW, materialNumberTP));

                materialCounter++;
            }

        } catch (Exception e) {
            LOGGER.error("Exception in collectPartNumbers: {}", e.getMessage());
            partNumberExceptionFlag = true;
        }
        if (!partNumberExceptionFlag) {
            LOGGER.info("Succesfully collected part numbers from {} materials to map.", materialCounter);
        }

    }

    /**
     * Compare the task list material numbers with the created relationships as
     * to establish the gap of materials that are neither in GPL nor invoiced in
     * last 36 months, and nor having convertible material numbers. These
     * materials could be third party materials to be added manually to the list
     * of recommended parts.
     */
    private void listUnknownMaterials(String taskListID, Map<Integer, TaskList> taskListMap) {
        Map<String, ImportMaterial> qualifiedMtrlNoMap = new HashMap<>();
        Map<String, ImportMaterial> taskListNumbersMap = new HashMap<>();

        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = neo.getDRIVER().session()) {
            String tx = "MATCH (:TaskList {id:$id})-[:LISTED_IN]-(m:PcMaterial) RETURN m.materialNumber AS qualifiedMtrlNumbers, m.description AS description;";

            StatementResult result = session.run(tx, parameters("id", taskListID));
            gapExceptionFlag = false;
            while (result.hasNext()) {
                Record next = result.next();

                String qualifiedMtrlNumbersBW = next.get("qualifiedMtrlNumbers").asString();
                String description = next.get("description").asString();

                // Add to qualified material number map
                qualifiedMtrlNoMap.put(qualifiedMtrlNumbersBW, new ImportMaterial(qualifiedMtrlNumbersBW, description));

            }

            if (!taskListMap.isEmpty()) {
                taskListMap.values().stream().forEach(t -> {
                    String spNumber = t.getSparePartNo();
                    String spDenomination = t.getSpDenomination();

                    taskListNumbersMap.put(spNumber, new ImportMaterial(spNumber, spDenomination));
                });
            }

        } catch (Exception e) {
            LOGGER.error("Exception in listMissingMaterials: {}", e.getMessage());
            gapExceptionFlag = true;
        }
        if (!gapExceptionFlag) {
            LOGGER.info("Succesfully establised gap between {} stored materials and {} parts in task list.", qualifiedMtrlNoMap.size(), taskListNumbersMap.size());
            unknownMaterialsList.clear();
            qualifiedMtrlNoMap.keySet().stream().forEach(k -> {
                taskListNumbersMap.remove(k);
            });
            taskListNumbersMap.values().stream().forEach(t -> {
                unknownMaterialsList.add(t);
//                System.out.printf("Task list material number '%s' doesn't exist in material DB.%n", t);
            });
        }
    }

    public List<ImportMaterial> getUnknownMaterialsList() {
        return unknownMaterialsList;
    }

    public void setUnknownMaterialsList(List<ImportMaterial> unknownMaterialsList) {
        this.unknownMaterialsList = unknownMaterialsList;
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
