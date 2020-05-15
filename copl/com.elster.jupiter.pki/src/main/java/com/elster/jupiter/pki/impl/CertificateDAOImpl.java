package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.CertificateDAO;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.wrappers.certificate.AbstractCertificateWrapperImpl;
import com.elster.jupiter.util.conditions.Where;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;


@Component(name = "com.elster.jupiter.pki.impl.CertificateDAOImpl", service = {CertificateDAO.class}, immediate = true)
public class CertificateDAOImpl implements CertificateDAO {

    private volatile DataModel dataModel;

    @Override
    public List<CertificateWrapper> findExpired(Instant when) {
        return dataModel.query(CertificateWrapper.class).select(Where.where(AbstractCertificateWrapperImpl.Fields.EXPIRATION.fieldName()).isLessThan(when));
    }


    @Reference
    public void setDataModel(SecurityManagementService securityManagementService) {
        this.dataModel = securityManagementService.getDataModel();
    }
}
