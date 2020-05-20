package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.SecurityAccessorDAO;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.impl.pki.SecurityAccessorDAOImpl", service = {SecurityAccessorDAO.class}, immediate = true)
public class SecurityAccessorDAOImpl implements SecurityAccessorDAO {

    private volatile DataModel dataModel;

    @Override
    public Optional<SecurityAccessor> findBy(SymmetricKeyWrapper key) {
        return toSingleObject(dataModel.mapper(SecurityAccessor.class)
                .find(SymmetricKeyAccessorImpl.Fields.SYMM_KEY_WRAPPER_ACTUAL.fieldName(), dataModel.asRefAny(key)), MessageSeeds.MULTIPLE_DEV_SEC_ACCESSOR_FOR_SYMKEY, key.toString());
    }

    @Override
    public Optional<SecurityAccessor> findBy(CertificateWrapper certificateWrapper) {
        return toSingleObject(dataModel.mapper(SecurityAccessor.class)
                .find(SymmetricKeyAccessorImpl.Fields.CERTIFICATE_WRAPPER_ACTUAL.fieldName(), dataModel.asRefAny(certificateWrapper)), MessageSeeds.MULTIPLE_DEV_SEC_ACCESSOR_FOR_SYMKEY, certificateWrapper
                .getId());
    }

    private <T> Optional<T> toSingleObject(List<T> list, MessageSeeds seed, Object searchCriteria) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        if (list.size() > 1) {
            throw new MappingException(seed, searchCriteria);
        }
        return Optional.of(list.get(0));
    }

    @Reference
    public void setDataModel(DeviceDataModelService deviceDataModelService) {
        this.dataModel = deviceDataModelService.dataModel();
    }

}
