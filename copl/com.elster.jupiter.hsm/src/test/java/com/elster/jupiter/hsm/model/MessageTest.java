package com.elster.jupiter.hsm.model;


import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {


    @Test
    public void validMessage(){
        Charset charSet = Charset.defaultCharset();
        byte[] bytes = {0,1,2,3,4,5,6};
        String testMsg = new String(bytes, charSet);
        Message msg = new Message(testMsg, charSet);

        Assert.assertArrayEquals(testMsg.getBytes(charSet), msg.getBytes());
        Assert.assertEquals(charSet, msg.getCharSet());
        Assert.assertEquals(testMsg, msg.toString());

        byte[] data =  {61, 16, 2, 116, -108, -42, 73, -92, 112, -103, -39, -126, 44, -125, -85, -80, 55, -43, 100, -113, -60, 9, -37, 105, -113, 40, -99, -18, -1, -86, 70, -120};
        String s = new Message(data).toString();
        Assert.assertEquals(s, "");

    }


}
