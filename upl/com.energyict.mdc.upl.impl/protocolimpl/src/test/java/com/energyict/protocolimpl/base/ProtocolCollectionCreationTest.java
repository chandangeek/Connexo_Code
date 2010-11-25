package com.energyict.protocolimpl.base;

import com.energyict.genericprotocolimpl.common.GenericProtocolCollectionImpl;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.fail;


/**
 * <p>
 * Copyrights EnergyICT
 * Date: 22-jun-2010
 * Time: 13:35:14
 * </p>
 */
public class ProtocolCollectionCreationTest {

    @Ignore
    @Test
    public void createCollection(){
        try{
            HSSFWorkbook wb          = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet();

            ProtocolCollectionImpl pci = new ProtocolCollectionImpl();

            for(int i = 0; i < pci.getProtocolclasses().size(); i++){

                HSSFRow row     = sheet.createRow(i);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue((String)pci.getProtocolClassName(i));
                row.createCell(2).setCellValue((String)pci.getProtocolName(i));
                try{
                    row.createCell(3).setCellValue((String)pci.getProtocolRevision(i));
                } catch (IOException e){
                    row.createCell(3).setCellValue("Temporary Unavailable");
                }

            }

            HSSFRow emptyRow = sheet.createRow(sheet.getLastRowNum() +1);
            emptyRow.createCell(0).setCellValue(" ");
            emptyRow.createCell(1).setCellValue(" ");
            emptyRow.createCell(2).setCellValue(" ");
            emptyRow.createCell(3).setCellValue(" ");

            GenericProtocolCollectionImpl gpci = new GenericProtocolCollectionImpl();
            int offset = sheet.getLastRowNum() + 1;
            for(int i = 0; i < gpci.getProtocolclasses().size(); i++){

                HSSFRow row     = sheet.createRow(i + offset);
                row.createCell(0).setCellValue(i + offset);
                row.createCell(1).setCellValue((String)gpci.getProtocolClassName(i));
                row.createCell(2).setCellValue((String)gpci.getProtocolName(i));
                try{
                    row.createCell(3).setCellValue((String)gpci.getProtocolRevision(i));
                } catch (IOException e){
                    row.createCell(3).setCellValue("Temporary Unavailable");
                }

            }

            // Create an empty row that differs the generics form the classic protocols
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            FileOutputStream fileOut = new FileOutputStream("c:\\MeterProtocols.xls");
            wb.write(fileOut);
            fileOut.close();

        } catch (IOException e){
            fail();
        }

    }
}
