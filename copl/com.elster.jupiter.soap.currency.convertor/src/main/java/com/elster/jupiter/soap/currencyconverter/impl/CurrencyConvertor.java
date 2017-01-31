/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.currencyconverter.CurrencyConvertorSoap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class was generated by Apache CXF 2.7.18
 * 2016-05-09T09:36:06.629+02:00
 * Generated source version: 2.7.18
 */
@WebServiceClient(name = "CurrencyConvertor",
        wsdlLocation = "http://www.webservicex.net/CurrencyConvertor.asmx?wsdl",
        targetNamespace = "http://www.webserviceX.NET/")
public class CurrencyConvertor extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://www.webserviceX.NET/", "CurrencyConvertor");
    public final static QName CurrencyConvertorHttpPost = new QName("http://www.webserviceX.NET/", "CurrencyConvertorHttpPost");
    public final static QName CurrencyConvertorSoap = new QName("http://www.webserviceX.NET/", "CurrencyConvertorSoap");
    public final static QName CurrencyConvertorSoap12 = new QName("http://www.webserviceX.NET/", "CurrencyConvertorSoap12");
    public final static QName CurrencyConvertorHttpGet = new QName("http://www.webserviceX.NET/", "CurrencyConvertorHttpGet");

    static {
        URL url = null;
        try {
            url = new URL("http://www.webservicex.net/CurrencyConvertor.asmx?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(CurrencyConvertor.class.getName())
                    .log(java.util.logging.Level.INFO,
                            "Can not initialize the default wsdl from {0}", "http://www.webservicex.net/CurrencyConvertor.asmx?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public CurrencyConvertor(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public CurrencyConvertor(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public CurrencyConvertor() {
        super(WSDL_LOCATION, SERVICE);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public CurrencyConvertor(WebServiceFeature... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public CurrencyConvertor(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public CurrencyConvertor(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * @return returns CurrencyConvertorHttpPost
     */
    @WebEndpoint(name = "CurrencyConvertorHttpPost")
    public CurrencyConvertorHttpPost getCurrencyConvertorHttpPost() {
        return super.getPort(CurrencyConvertorHttpPost, CurrencyConvertorHttpPost.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns CurrencyConvertorHttpPost
     */
    @WebEndpoint(name = "CurrencyConvertorHttpPost")
    public CurrencyConvertorHttpPost getCurrencyConvertorHttpPost(WebServiceFeature... features) {
        return super.getPort(CurrencyConvertorHttpPost, CurrencyConvertorHttpPost.class, features);
    }

    /**
     * @return returns CurrencyConvertorSoap
     */
    @WebEndpoint(name = "CurrencyConvertorSoap")
    public com.elster.jupiter.soap.currencyconverter.CurrencyConvertorSoap getCurrencyConvertorSoap() {
        return super.getPort(CurrencyConvertorSoap, CurrencyConvertorSoap.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns CurrencyConvertorSoap
     */
    @WebEndpoint(name = "CurrencyConvertorSoap")
    public CurrencyConvertorSoap getCurrencyConvertorSoap(WebServiceFeature... features) {
        return super.getPort(CurrencyConvertorSoap, CurrencyConvertorSoap.class, features);
    }

    /**
     * @return returns CurrencyConvertorSoap
     */
    @WebEndpoint(name = "CurrencyConvertorSoap12")
    public CurrencyConvertorSoap getCurrencyConvertorSoap12() {
        return super.getPort(CurrencyConvertorSoap12, CurrencyConvertorSoap.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns CurrencyConvertorSoap
     */
    @WebEndpoint(name = "CurrencyConvertorSoap12")
    public CurrencyConvertorSoap getCurrencyConvertorSoap12(WebServiceFeature... features) {
        return super.getPort(CurrencyConvertorSoap12, CurrencyConvertorSoap.class, features);
    }

    /**
     * @return returns CurrencyConvertorHttpGet
     */
    @WebEndpoint(name = "CurrencyConvertorHttpGet")
    public CurrencyConvertorHttpGet getCurrencyConvertorHttpGet() {
        return super.getPort(CurrencyConvertorHttpGet, CurrencyConvertorHttpGet.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns CurrencyConvertorHttpGet
     */
    @WebEndpoint(name = "CurrencyConvertorHttpGet")
    public CurrencyConvertorHttpGet getCurrencyConvertorHttpGet(WebServiceFeature... features) {
        return super.getPort(CurrencyConvertorHttpGet, CurrencyConvertorHttpGet.class, features);
    }

}
