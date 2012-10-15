package com.energyict.protocolimpl.base;

import com.energyict.protocolimpl.base.protocolcollections.GenericProtocolCollectionImpl;
import com.energyict.protocolimpl.base.protocolcollections.ProtocolCollectionImpl;
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

    @Test
    public void classNameTest() throws IOException {
        ProtocolCollectionImpl pci = new ProtocolCollectionImpl();

        for(int i = 0; i < pci.getProtocolClasses().size(); i++){
            String[] str = pci.getProtocolClassName(i).split("\\.");
            if(str.length >= 1){
                String name = str[str.length - 1];
                if(name.length() > 24){
                    fail("Oeps, you defined a protocol with a name longer then 24 characters, this is not allowed in 9.1 : " + name);
                }
            }
        }
    }

    @Ignore
    @Test
    public void createCollection(){
        try{
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("Classic Protocols");
            HSSFSheet sheetSm = wb.createSheet("SmartMeter Protocols");

            ProtocolCollectionImpl pci = new ProtocolCollectionImpl();

            int counter = 0;
            int smCounter = 0;

            for(int i = 0; i < pci.getProtocolClasses().size(); i++){
                HSSFRow row;
                if(pci.getProtocolClassName(i).indexOf("genericprotocolimpl") > 0 || pci.getProtocolClassName(i).indexOf("smartmeterprotocolimpl") > 0  ){
                    row = sheetSm.createRow(smCounter++);
                } else {
                    row = sheet.createRow(counter++);
                }

                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue((String)pci.getProtocolClassName(i));
                row.createCell(2).setCellValue((String)pci.getProtocolName(i));
                try{
                    row.createCell(3).setCellValue((String)pci.getProtocolRevision(i));
                } catch (IOException e){
                    row.createCell(3).setCellValue("Temporary Unavailable");
                }

            }

            GenericProtocolCollectionImpl gpci = new GenericProtocolCollectionImpl();
            for(int i = 0; i < gpci.getProtocolClasses().size(); i++){

                HSSFRow row;
                if(gpci.getProtocolClassName(i).indexOf("genericprotocolimpl") > 0 || gpci.getProtocolClassName(i).indexOf("smartmeterprotocolimpl") > 0  ){
                    row = sheetSm.createRow(smCounter++);
                } else {
                    row = sheet.createRow(counter++);
                }

                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue((String)gpci.getProtocolClassName(i));
                row.createCell(2).setCellValue((String)gpci.getProtocolName(i));
                try{
                    row.createCell(3).setCellValue((String)gpci.getProtocolRevision(i));
                } catch (IOException e){
                    row.createCell(3).setCellValue("Temporary Unavailable");
                }

            }
            
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheetSm.autoSizeColumn(0);
            sheetSm.autoSizeColumn(1);
            sheetSm.autoSizeColumn(2);
            sheetSm.autoSizeColumn(3);

            FileOutputStream fileOut = new FileOutputStream("c:\\MeterProtocols.xls");
            wb.write(fileOut);
            fileOut.close();

        } catch (IOException e){
            fail();
        }

    }
}
