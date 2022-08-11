package com.energyict.protocolimplv2.eict.webcatch.model;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ServletInputStreamCorrectOrder extends ServletInputStream {

    private String json = "{\"ip\":\"10.113.40.47\",\"serial\":\"B229K3X0B1\",\"utcstamp\":1658751300,\"devices\":[{\"values\":[\"1070715.000\"],\"serial\":\"Chn01\"},{\"values\":[\"50054.000\"],\"serial\":\"Chn02\"},{\"values\":[\"4.000\"],\"serial\":\"Chn03\"},{\"values\":[\"2.000\"],\"serial\":\"Chn04\"}],\"version\":\"1.0\"}";
    private InputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

    @Override
    public int read() throws IOException {
        return byteArrayInputStream.read();
    }
}
