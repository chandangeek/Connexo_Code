package com.elster.jupiter.hsm.integration.helpers;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class XMLSignerTest {

    private static final String XML = "my.xml";

    /**
     * Truststore and all attached elements to get the key used during test
     */
    private static final String JKS = "my.jks";
    private static final String JKS_PASSWORD = "password";

    private static final String KEY_ALIAS = "shipment-importer";
    private static final String KEY_PASSWORD = "password";

    private InputStream xmlStream;
    private Key keyEntry;
    private X509Certificate cert;


    @Before
    public void setUp() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableEntryException {
        this.xmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML);
        if (this.xmlStream == null) {
            throw new RuntimeException("Ooops we cannot find resource:" + XML);
        }

        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(JKS, JKS_PASSWORD.toCharArray());
        this.keyEntry = keyStoreHelper.getKey(KEY_ALIAS, KEY_PASSWORD.toCharArray());
        this.cert = keyStoreHelper.getCertificate(KEY_ALIAS, KEY_PASSWORD.toCharArray());

    }

    @Test
    public void signXml() throws
            IOException,
            ParserConfigurationException,
            SAXException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            MarshalException,
            XMLSignatureException,
            TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(xmlStream);

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        SignedInfo si = getSignedInfo(fac);
        KeyInfo ki = getKeyInfo(fac);

        DOMSignContext dsc = new DOMSignContext(keyEntry, doc.getDocumentElement());
        XMLSignature sig = fac.newXMLSignature(si, ki);

        sig.sign(dsc);

        OutputStream os = new FileOutputStream("signed-" + XML);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(doc), new StreamResult(os));

    }

    private KeyInfo getKeyInfo(XMLSignatureFactory fac) {
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        List x509Content = new ArrayList();
        x509Content.add(cert.getSubjectX500Principal().getName());
        x509Content.add(cert);
        X509Data xd = kif.newX509Data(x509Content);
        return kif.newKeyInfo(Collections.singletonList(xd));
    }

    private SignedInfo getSignedInfo(XMLSignatureFactory fac) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Reference ref = fac.newReference
                ("", fac.newDigestMethod(DigestMethod.SHA1, null),
                        Collections.singletonList
                                (fac.newTransform
                                        (Transform.ENVELOPED, (TransformParameterSpec) null)),
                        null, null);

        return fac.newSignedInfo
                (fac.newCanonicalizationMethod
                                (CanonicalizationMethod.INCLUSIVE,
                                        (C14NMethodParameterSpec) null),
                        fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                        Collections.singletonList(ref));
    }

}
