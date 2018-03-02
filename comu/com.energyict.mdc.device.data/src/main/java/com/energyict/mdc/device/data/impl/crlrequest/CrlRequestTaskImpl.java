package com.energyict.mdc.device.data.impl.crlrequest;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTask;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class CrlRequestTaskImpl implements CrlRequestTask {

    public enum Fields {
        END_DEVICE_GROUP("deviceGroup"),
        //todo: accessor type?
        SECURITY_ACCESSOR("securityAccessor"),
        CERTIFICATE("certificate"),
        CA_NAME("caName"),
        FREQUENCY("frequency");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final CrlRequestTaskService crlRequestTaskService;
    private final Thesaurus thesaurus;

    private long id;
    private long version;
    private String userName;
    private Instant createTime;
    private Instant modTime;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<EndDeviceGroup> deviceGroup = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<SecurityAccessor> securityAccessor = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<CertificateWrapper> certificate = ValueReference.absent();
    @NotNull(message = MessageSeeds.Keys.FIELD_REQUIRED, groups = {Save.Create.class, Save.Update.class})
    //todo: persist temporal amount
    private String frequency;
    @NotNull(message = MessageSeeds.Keys.FIELD_REQUIRED, groups = {Save.Create.class, Save.Update.class})
    private String caName;

    @Inject
    public CrlRequestTaskImpl(DataModel dataModel, CrlRequestTaskService crlRequestTaskService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.crlRequestTaskService = crlRequestTaskService;
        this.thesaurus = thesaurus;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getFrequency() {
        return frequency;
    }

    @Override
    public EndDeviceGroup getDeviceGroup() {
        return deviceGroup.get();
    }

    @Override
    public void setDeviceGroup(EndDeviceGroup deviceGroup) {
        this.deviceGroup.set(deviceGroup);
    }

    @Override
    public String getCaName() {
        return caName;
    }

    @Override
    public void setCaName(String caName) {
        this.caName = caName;
    }

    @Override
    public SecurityAccessor getSecurityAccessor() {
        return securityAccessor.get();
    }

    @Override
    public void setSecurityAccessor(SecurityAccessor securityAccessor) {
        if(!securityAccessor.getKeyAccessorType().getKeyType().getCryptographicType().isKey()){
            if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper){
                CertificateWrapper defaultCertificate = (CertificateWrapper) securityAccessor.getActualValue().get();
                this.setCertificate(defaultCertificate);
            }
        }
        this.securityAccessor.set(securityAccessor);
    }

    @Override
    public void setCertificate(CertificateWrapper certificateWrapper) {
        this.certificate.set(certificateWrapper);
    }

    @Override
    public CertificateWrapper getCertificate() {
        return certificate.get();
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
    }

    @Override
    public void save() {
        if (this.getId() == 0) {
            Save.CREATE.save(this.dataModel, this);
        }
        Save.UPDATE.save(this.dataModel, this);
    }

    @Override
    public long getVersion() {
        return version;
    }
}
