package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.estimation.rest", service = {Application.class, InstallService.class}, immediate = true, property = {"alias=/est", "app=SYS", "name=" + EstimationApplication.COMPONENT_NAME})
public class EstimationApplication extends Application implements BinderProvider, InstallService {
    public static final String COMPONENT_NAME = "EST";
    private static final String ESTIMATIONS_PRIVILEGE_CATEGORY_NAME = "estimation.estimations";
    private static final String ESTIMATIONS_PRIVILEGE_CATEGORY_DESCRIPTION = "estimation.estimations.description";

    private volatile EstimationService estimationService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TimeService timeService;

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                EstimationResource.class);
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(EstimationService.COMPONENTNAME, Layer.REST);
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(nlsService).to(NlsService.class);
                bind(estimationService).to(EstimationService.class);
                bind(transactionService).to(TransactionService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(meteringGroupsService).to(MeteringGroupsService.class);
                bind(timeService).to(TimeService.class);
                bind(PropertyUtils.class).to(PropertyUtils.class);
            }
        };
    }

    @Override
    public void install() {
        List<Translation> translations =
                Stream.of(
                        fromPrivileges(),
                        privilegeCategoryTranslations()
                )
                        .flatMap(Function.identity())
                        .collect(Collectors.toList());

        thesaurus.addTranslations(translations);
    }

    private Stream<Translation> privilegeCategoryTranslations() {
        NlsKey categoryNameKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.REST, ESTIMATIONS_PRIVILEGE_CATEGORY_NAME);
        Translation categoryName = SimpleTranslation.translation(categoryNameKey, Locale.ENGLISH, "Estimation");
        NlsKey categoryDescriptionKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.REST, ESTIMATIONS_PRIVILEGE_CATEGORY_DESCRIPTION);
        Translation categoryDescription = SimpleTranslation.translation(categoryDescriptionKey, Locale.ENGLISH, "Estimation");
        return Stream.of(categoryName, categoryDescription);
    }

    private Stream<Translation> fromPrivileges() {
        return Arrays.stream(Privileges.values())
                .map(this::toTranslation);
    }

    private Translation toTranslation(TranslationKey translationKey) {
        NlsKey nlsKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.REST, translationKey.getKey()).defaultMessage(translationKey.getDefaultFormat());
        return SimpleTranslation.translation(nlsKey, Locale.ENGLISH, translationKey.getDefaultFormat());
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("MTR", "TSK", "MTG", "TME");
    }
}
