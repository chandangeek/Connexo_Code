package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.pki.CertificateRequestData;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CertificateRequestDataTest {

    private final String name = "name";
    private final String entity = "entity";
    private final String profile = "profile";
    private final String subjectDnFields = "dnFields";

    private final Map<String, Object> PROPS = new HashMap<>();

    @Before
    public void before() {
        PROPS.put(CSRImporterTranslatedProperty.CA_NAME.getPropertyKey(), name);
        PROPS.put(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME.getPropertyKey(), entity);
        PROPS.put(CSRImporterTranslatedProperty.CA_PROFILE_NAME.getPropertyKey(), profile);
    }

    @Test(expected = RuntimeException.class)
    public void testNullCaName(){
        new CertificateRequestData(null, entity, profile, subjectDnFields);

    }

    @Test(expected = RuntimeException.class)
    public void testEmptyCaName(){
        new CertificateRequestData("", entity, profile, subjectDnFields);

    }

    @Test(expected = RuntimeException.class)
    public void testNullEntity(){
        new CertificateRequestData(name, null, profile, subjectDnFields);

    }

    @Test(expected = RuntimeException.class)
    public void testEmptyEntity(){
        new CertificateRequestData(name, "", profile, subjectDnFields);

    }

    @Test(expected = RuntimeException.class)
    public void testNullProfile(){
        new CertificateRequestData(name, entity, null, subjectDnFields);

    }

    @Test(expected = RuntimeException.class)
    public void testEmptyProfile(){
        new CertificateRequestData(name, entity, "", subjectDnFields);

    }

    @Test
    public void testAllOk(){
        CertificateRequestData certificateRequestData = new CertificateRequestData(name, entity, profile, subjectDnFields);
        Assert.assertEquals(name, certificateRequestData.getCaName());
        Assert.assertEquals(entity, certificateRequestData.getEndEntityName());
        Assert.assertEquals(profile, certificateRequestData.getCertificateProfileName());
    }

    @Test(expected = RuntimeException.class)
    public void testPropsMissingName() {
        remove(CSRImporterTranslatedProperty.CA_NAME);
        CertificateRequestData.from(PROPS);
    }
    @Test(expected = RuntimeException.class)
    public void testPropsMissingEntity() {
        remove(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME);
        CertificateRequestData.from(PROPS);
    }

    @Test(expected = RuntimeException.class)
    public void testPropsMissingProfile() {
        remove(CSRImporterTranslatedProperty.CA_PROFILE_NAME);
        CertificateRequestData.from(PROPS);
    }

    @Test
    public void testPropsOk() {
        CertificateRequestData certificateRequestData = CertificateRequestData.from(PROPS);
        Assert.assertEquals(name, certificateRequestData.getCaName());
        Assert.assertEquals(entity, certificateRequestData.getEndEntityName());
        Assert.assertEquals(profile, certificateRequestData.getCertificateProfileName());
    }

    private void remove(CSRImporterTranslatedProperty caName) {
        PROPS.remove(caName.getPropertyKey());
    }
}
