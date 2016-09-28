package com.elster.us.protocolimplv2.sel.registers;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RegisterParserTest {
  public static String input = "\r\nFEEDER 1                          Date: 07/25/16    Time: 13:40:21.154\r\n STATION A                                           Time Source: ext\r\n\r\n\r\n                              A             B             C             3P\r\nW DEL  (kWh)            951764.83     926422.63    1116640.41    2994827.72 \r\nW REC  (kWh)                 0.04          0.03          4.83          4.75 \r\nW NET  (kWh)            951764.79     926422.60    1116635.57    2994822.97 \r\n\r\n                              A             B             C             3P\r\nU DEL  (kVAh)           992289.86    1011570.60    1167614.60    3171477.20 \r\nU REC  (kVAh)                0.60          0.53          8.40          7.42 \r\n\r\n                              A             B             C             3P\r\nQ DEL LAG  (kVARh)      208969.26     380080.25     308188.30     855824.02 \r\nQ DEL LEAD (kVARh)           0.60          0.53          3.58          2.58 \r\nQ DEL      (kVARh)      208969.86     380080.79     308191.88     855826.61 \r\nQ REC LAG  (kVARh)           0.00          0.00          2.38          2.39 \r\nQ REC LEAD (kVARh)       43524.67       1095.82       6544.89       9749.45 \r\nQ REC      (kVARh)       43524.67       1095.82       6547.27       9751.84 \r\n\r\n                              A             B             C             N\r\nAMP  ( Ah)              137842.62     141308.96     163034.16        261.55 \r\nVOLT (kVh)              231235.46     229947.36     230094.62 \r\n\r\n                              3P\r\nAMP  ( Ah)              442185.75 \r\nVOLT (kVh)              691277.46 \r\n\r\nLAST RESET 10/29/12 10:58:09.214\r\n";
  
  @Test
  public void testParser() {
    RegisterParser parser = new RegisterParser();
    List<RegisterData> data = parser.parse(input);
    assertEquals(data.size(),15);
    assertEquals(data.get(0).getDescription(), "W DEL");
    assertEquals(data.get(0).getUnit(), "kWh");
    assertEquals(data.get(0).getBuckets().size(), 4);
    assertEquals(String.format("%.2f", data.get(0).getBucket("A").getValue()), "951764.83");
    assertEquals(data.get(14).getDescription(), "VOLT");
    assertEquals(data.get(14).getUnit(), "kVh");
    assertEquals(data.get(14).getBuckets().size(), 1);
    assertEquals(String.format("%.2f", data.get(14).getBucket("3P").getValue()), "691277.46");
    // TODO: test the time using timezone and DST
  }

}
