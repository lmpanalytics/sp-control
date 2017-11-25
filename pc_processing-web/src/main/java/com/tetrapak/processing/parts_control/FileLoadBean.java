/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.inject.Named;
import java.io.Serializable;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLoadBean.class);
    private static FileInputStream excelFile;

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
                        break;
                    default:
                        break;
                }
            }
//        Write data patterns to the data base
            /* addPattern(patternMap);
            LOGGER.info("Data written to DB.");*/

        } catch (IOException ex) {
            LOGGER.error("Exception in handleFileUpload method {}", ex);
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
        } catch (IOException e) {
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
        } catch (IOException e) {
        }
        return m;
    }

    /**
     * Reads an Excel file, based on Task List data
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

                        } else if (cellCounter == 14) {
                            qty = d.intValue();

                        }

                    }
                }
                //   Put to map starting from row two in the Excel sheet
                if (rowCounter != Long.MAX_VALUE && rowCounter > 1) {
                    // System.out.printf("Task List file row: %s\t { %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s\t, %s }", rowCounter, machineNumber, label, classItem, articleNo, eqDenomination, type, docNo, description, sparePartNo, spDenomination, qty, functionalArea);
                    m.put(rowCounter, new TaskList(machineNumber, label, classItem, articleNo, eqDenomination, type, docNo, description, sparePartNo, spDenomination, qty, functionalArea));
                }
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return m;
    }

    /**
     * Adds data patterns to the data base
     *
     * @param pattern
     */
    /* private void addPattern(Map<Long, Pattern> pattern) {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = neo.getDRIVER().session()) {
            // Wrapping Cypher in an explicit transaction provides atomicity
            // and makes handling errors much easier.
            try (Transaction tx = session.beginTransaction()) {
                pattern.values().stream().forEach((v) -> {
                    Double respVar0 = v.getResponeVar0();
                    Long ms = v.getMsTime();
                    tx.run("MATCH (c:Customer { customerNumber: {custNo} })"
                            + "MERGE (p:Pattern {msEpoch: {t}, respVar0: {rv0}}) "
                            + "MERGE (p)-[:OWNED_BY]->(c)",
                            parameters("t", ms,
                                    "rv0", respVar0,
                                    "custNo", user.getCustomerNumber()));
                    tx.success();  // Mark this write as successful.
                });
            }
        }
    }*/
    @PreDestroy
    void destroyMe() {
        try {
            excelFile.close();
        } catch (IOException ex) {
            LOGGER.error("Exception in closing excelFile reader {}", ex);
        }
    }
}
