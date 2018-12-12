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
    }


}
