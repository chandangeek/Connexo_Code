package com.energyict.mdc.device.data.impl.crlrequest;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.tasks.RecurrentTask;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class CrlRequestTaskPropertyImpl implements CrlRequestTaskProperty {

    public enum Fields {
        TASK("recurrentTask"),
        SECURITY_ACCESSOR("securityAccessor"),
        CA_NAME("caName");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    private long version;
    private String userName;
    private Instant createTime;
    private Instant modTime;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<SecurityAccessor> securityAccessor = ValueReference.absent();

    @NotNull(message = MessageSeeds.Keys.FIELD_REQUIRED, groups = {Save.Create.class, Save.Update.class})
    private String caName;

    @Inject
    public CrlRequestTaskPropertyImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        return recurrentTask.get();
    }

    @Override
    public void setRecurrentTask(RecurrentTask recurrentTask) {
        this.recurrentTask.set(recurrentTask);
    }

    @Override
    public SecurityAccessor getSecurityAccessor() {
        return securityAccessor.get();
    }

    @Override
    public void setSecurityAccessor(SecurityAccessor securityAccessor) {
        this.securityAccessor.set(securityAccessor);
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
    public void delete() {
        this.dataModel.remove(this);
    }

    @Override
    public void save() {
        Save.CREATE.save(this.dataModel, this);
    }

    @Override
    public void update() {
        Save.UPDATE.save(this.dataModel, this);
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }
}
