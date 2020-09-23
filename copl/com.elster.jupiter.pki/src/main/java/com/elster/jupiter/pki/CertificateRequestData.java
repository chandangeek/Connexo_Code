package com.elster.jupiter.pki;

import com.elster.jupiter.pki.impl.importers.csr.CSRImporterTranslatedProperty;
import com.elster.jupiter.util.ObjectChecker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class CertificateRequestData {


    private static final String FIELD_CA = "ca";
    private static final String FIELD_EE = "ee";
    private static final String FIELD_CP = "cp";

    private final String caName;
    private final String endEntityName;
    private final String certificateProfileName;

    private JSONObject mappingJson;
    private String prefix;
    private String subjectDNfields;


    public CertificateRequestData(String caName, String endEntityName,String certificateProfileName) {
        if (isEmpty(caName) || isEmpty(endEntityName) || isEmpty(certificateProfileName)) {
            throw new RuntimeException("Invalid certificate request data ca name:" + caName + " end entity name:" + endEntityName + " profile name:" + certificateProfileName);
        }
        this.caName = caName;
        this.endEntityName = endEntityName;
        this.certificateProfileName = certificateProfileName;
        this.prefix = "";
    }

    public CertificateRequestData(String mappingJson, String caName, String endEntityName,String certificateProfileName, String subjectDNfields) {
        if (isEmpty(mappingJson)) {
            if (isEmpty(caName) || isEmpty(endEntityName) || isEmpty(certificateProfileName)) {
                throw new RuntimeException("Invalid certificate request data ca name:" + caName + " end entity name:" + endEntityName + " profile name:" + certificateProfileName);
            }
        } else {
            try {
                this.mappingJson = new JSONObject(mappingJson);
            } catch (JSONException e) {
                throw new RuntimeException("Cannot parse input JSON: " + mappingJson);
            }
        }

        this.caName = caName;
        this.endEntityName = endEntityName;
        this.certificateProfileName = certificateProfileName;
        this.subjectDNfields = subjectDNfields;
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public String getCaName() {
        if (prefix!=null && !prefix.isEmpty()){
            if (mappingJson!=null){
                String value = getJsonFieldForPrefix(prefix, FIELD_CA);
                if (!value.isEmpty())
                    return value;
            }
        }
        return caName;
    }


    public String getEndEntityName() {
        if (prefix!=null && !prefix.isEmpty()){
            if (mappingJson!=null){
                String value = getJsonFieldForPrefix(prefix, FIELD_EE);
                if (!value.isEmpty())
                    return value;
            }
        }
        return endEntityName;
    }

    public String getCertificateProfileName() {
        if (prefix!=null && !prefix.isEmpty()){
            if (mappingJson!=null){
                String value = getJsonFieldForPrefix(prefix, FIELD_CP);
                if (!value.isEmpty())
                    return value;
            }
        }
        return certificateProfileName;
    }

    public static CertificateRequestData from(Map<String, Object> properties){
        return new CertificateRequestData(
                (String)properties.get(CSRImporterTranslatedProperty.CSR_MAPPING.getPropertyKey()),
                (String)properties.get(CSRImporterTranslatedProperty.CA_NAME.getPropertyKey()),
                (String)properties.get(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME.getPropertyKey()),
                (String) properties.get(CSRImporterTranslatedProperty.CA_PROFILE_NAME.getPropertyKey()),
                (String)properties.get(CSRImporterTranslatedProperty.SUBJECT_DN_FIELDS.getPropertyKey()));
    }

    private boolean isEmpty(String caName) {
        return caName == null || caName.isEmpty();
    }


    private String getJsonFieldForPrefix(String prefix, String field) {
        if (mappingJson==null){
            return "";
        }
        Iterator keys = mappingJson.keys();
        while(keys.hasNext()) {
            String currentPrefix = (String) keys.next();
            try {
                if (currentPrefix.equals(prefix)) {
                    return mappingJson.getJSONObject(prefix).getString(field).toString();
                }
            } catch (JSONException e) {
                return "";
            }
        }
        return "";
    }

    public String getSubjectDNfields() {
        return subjectDNfields;
    }
}
