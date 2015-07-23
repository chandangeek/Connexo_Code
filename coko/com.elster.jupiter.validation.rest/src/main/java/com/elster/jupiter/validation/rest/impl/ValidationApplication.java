package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.validation.rest" , service=Application.class , immediate = true , property = {"alias=/val", "app=SYS", "name=" + ValidationApplication.COMPONENT_NAME} )
public class ValidationApplication extends Application implements BinderProvider {
    public static final String COMPONENT_NAME = "VAL";

	private volatile ValidationService validationService;
	private volatile TransactionService transactionService;
	private volatile RestQueryService restQueryService;
    private volatile MeteringGroupsService meteringGroupsService;

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile TimeService timeService;

	public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                ValidationResource.class,
                DataValidationTaskResource.class);
	}

    @Reference
	public void setValidationService(ValidationService validationService) {
		this.validationService = validationService;
	}
	
	@Reference
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Reference
	public void setRestQueryService(RestQueryService restQueryService) {
		this.restQueryService = restQueryService;
	}

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(ValidationService.COMPONENTNAME, Layer.REST);
   }
	
    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
                bind(PropertyUtils.class).to(PropertyUtils.class);
                bind(nlsService).to(NlsService.class);
                bind(validationService).to(ValidationService.class);
                bind(transactionService).to(TransactionService.class);
                bind(meteringGroupsService).to(MeteringGroupsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(timeService).to(TimeService.class);
            }
        };
    }
}
