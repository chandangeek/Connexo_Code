package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapperDAO;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.util.conditions.Where;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;

@Component(name = "com.elster.jupiter.pki.impl.wrappers.symmetric.SymmetricKeyWrapperDAOImpl", service = {SymmetricKeyWrapperDAO.class}, immediate = true)
public class SymmetricKeyWrapperDAOImpl implements SymmetricKeyWrapperDAO {

    private volatile DataModel dataModel;

    @Override
    public List<SymmetricKeyWrapper> findExpired(Instant plus) {
        return dataModel.query(SymmetricKeyWrapper.class).select(Where.where(KeyImpl.Fields.EXPIRATION.fieldName()).isLessThan(plus));
    }


    @Reference
    public void setDataModel(SoftwareSecurityDataModel ssm) {
        this.dataModel = ssm.getDataModel();
    }
}
