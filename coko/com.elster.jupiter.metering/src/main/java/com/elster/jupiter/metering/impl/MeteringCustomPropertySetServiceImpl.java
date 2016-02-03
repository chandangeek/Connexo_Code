package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringCustomPropertySetService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategoryCustomPropertySet;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(name = "com.elster.jupiter.metering.impl.MeteringCustomPropertySetServiceImpl", service = MeteringCustomPropertySetService.class, immediate = true)
public class MeteringCustomPropertySetServiceImpl implements MeteringCustomPropertySetService {

    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCustomPropertySet(ServiceCategoryCustomPropertySet customPropertySet) {

        try (TransactionContext ctx = transactionService.getContext()) {
            customPropertySetService.addSystemCustomPropertySet(customPropertySet.getCustomPropertySet());
            customPropertySetService.findActiveCustomPropertySet(customPropertySet.getCustomPropertySet().getId())
                    .ifPresent(this::registerSetOnServiceCategory);
            ctx.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            ex.printStackTrace();
        }


    }

    public void removeCustomPropertySet(ServiceCategoryCustomPropertySet customPropertySet) {
        customPropertySetService.removeCustomPropertySet(customPropertySet.getCustomPropertySet());
    }

    public void registerSetOnServiceCategory(RegisteredCustomPropertySet registeredCustomPropertySet) {
        for (ServiceKind kind : ((ServiceCategoryCustomPropertySet) registeredCustomPropertySet.getCustomPropertySet()).getServiceKinds()) {
            meteringService.getServiceCategory(kind).orElseThrow(IllegalArgumentException::new).addCustomPropertySet(registeredCustomPropertySet);
        }
    }

}
