package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.NestedTransactionException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.metadata.ConstraintDescriptor;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.nls", service = {NlsService.class}, property = {"name=" + NlsService.COMPONENTNAME, "osgi.command.scope=nls", "osgi.command.function=addTranslation"})
public class NlsServiceImpl implements NlsService {

    private static final Pattern MESSAGE_PARAMETER_PATTERN = Pattern.compile("(\\{[^\\}]+?\\})");

    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile MessageInterpolator messageInterpolator;
    @GuardedBy("translationLock")
    private final List<TranslationKeyProvider> translationKeyProviders = new ArrayList<>();
    @GuardedBy("translationLock")
    private volatile List<MessageSeedProvider> messageSeedProviders = new CopyOnWriteArrayList<>();
    private volatile boolean installed = false;
    private volatile UpgradeService upgradeService;

    private final Object translationLock = new Object();

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(MessageInterpolator.class).toInstance(messageInterpolator);
                bind(NlsService.class).toInstance(NlsServiceImpl.this);
            }
        });
        upgradeService.register(InstallIdentifier.identifier(COMPONENTNAME), dataModel, NlsInstaller.class, Collections.emptyMap());
        installed = true; // upgradeService either installed, was up to date, or threw an Exception because upgrade was needed; in any case if we get here installed is true
    }

    public NlsServiceImpl() {
    }

    @Inject
    public NlsServiceImpl(OrmService ormService, ThreadPrincipalService threadPrincipalService, TransactionService transactionService, ValidationProviderResolver validationProviderResolver, UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setValidationProviderResolver(validationProviderResolver);
        setUpgradeService(upgradeService);
        activate();
    }

    @Override
    public Thesaurus getThesaurus(String componentName, Layer layer) {
        return dataModel.getInstance(ThesaurusImpl.class).init(componentName, layer);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "National Language Support");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public final void setValidationProviderResolver(ValidationProviderResolver validationProviderResolver) {
        this.messageInterpolator = Validation.byDefaultProvider()
                .providerResolver(validationProviderResolver)
                .configure()
                .getDefaultMessageInterpolator();
    }

    @Reference(name = "ZTranslationProvider", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    public synchronized void addTranslationKeyProvider(TranslationKeyProvider provider) {
        synchronized (translationLock) {
            this.translationKeyProviders.add(provider);
            if (installed) {
                try {
                    setPrincipal();
                    threadPrincipalService.set("INSTALL-translation", provider.getComponentName());
                    // Attempt to setup a new transaction, required when a new bundle is activated
                    try (TransactionContext context = this.transactionService.getContext()) {
                        doInstallProvider(provider);
                        context.commit();
                    }
                    catch (NestedTransactionException e) {
                        // Fails if we were already in transaction mode when installing a License, simply try again
                        doInstallProvider(provider);
                    }
                } finally {
                    clearPrincipal();
                }
            }
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @SuppressWarnings("unused")
    public void removeTranslationKeyProvider(TranslationKeyProvider provider) {
        synchronized (translationLock) {
            this.translationKeyProviders.remove(provider);
        }
    }

    @SuppressWarnings("unused")
    @Reference(name = "ZMessageSeedProvider", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    public synchronized void addMessageSeedProvider(MessageSeedProvider provider) {
        synchronized (translationLock) {
            this.messageSeedProviders.add(provider);
            if (installed) {
                try {
                    this.setPrincipal();
                    // Attempt to setup a new transaction, required when a new bundle is activated
                    try (TransactionContext context = this.transactionService.getContext()) {
                        this.doInstallProvider(provider);
                        context.commit();
                    }
                    catch (NestedTransactionException e) {
                        // Fails if we were already in transaction mode when installing a License, simply try again
                        doInstallProvider(provider);
                    }
                }
                finally {
                    this.clearPrincipal();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void removeMessageSeedProvider(MessageSeedProvider provider) {
        synchronized (translationLock) {
            this.messageSeedProviders.remove(provider);
        }
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private Principal getPrincipal() {
        return () -> "Jupiter Installer";
    }

    public synchronized void doInstall(DataModelUpgrader dataModelUpgrader) {
        synchronized (translationLock) {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
            translationKeyProviders.forEach(this::doInstallProvider);
            messageSeedProviders.forEach(this::doInstallProvider);
            installed = true;
        }
    }

    private void doInstallProvider(TranslationKeyProvider provider) {
        String componentName = provider.getComponentName();
        Layer layer = provider.getLayer();
        ThesaurusImpl thesaurus = (ThesaurusImpl) getThesaurus(componentName, layer);
        thesaurus.createNewTranslationKeys(provider);
    }

    private void doInstallProvider(MessageSeedProvider provider) {
        if (!provider.getSeeds().isEmpty()) {
            this.doInstallProvider(new MessageSeedProviderAdapterForTranslationKeyProvider(provider));
        }
    }

    // Published as a gogo command so be wary when refactoring
    @SuppressWarnings("unused")
    public void addTranslation(String componentName, String layerName, String key, String defaultMessage) {
        try {
            Layer layer = Layer.valueOf(layerName);
            Thesaurus thesaurus = getThesaurus(componentName, layer);
            SimpleNlsKey nlsKey = SimpleNlsKey.key(componentName, layer, key).defaultMessage(defaultMessage);
            Translation translation = SimpleTranslation.translation(nlsKey, Locale.ENGLISH, defaultMessage);
            thesaurus.addTranslations(Collections.singletonList(translation));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Published as a gogo command
    @SuppressWarnings("unused")
    public void addTranslation(Object... args) {
        System.out.println("Usage : \n\n addTranslation componentName layerName key defaultMessage");
    }

    @Override
    public String interpolate(ConstraintViolation<?> violation) {
        try (ContextClassLoaderResource ctx = ContextClassLoaderResource.of(com.sun.el.ExpressionFactoryImpl.class)) {
            return messageInterpolator.interpolate(
                    interpolate(violation.getMessageTemplate()),
                    interpolationContext(violation),
                    threadPrincipalService.getLocale());
        }
    }

    private String interpolate(String messageTemplate) {
        //
        // copied from org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
        //
        Matcher matcher = MESSAGE_PARAMETER_PATTERN.matcher(messageTemplate);
        StringBuffer sb = new StringBuffer();
        String resolvedParameterValue;
        while (matcher.find()) {
            String parameter = matcher.group(1);
            resolvedParameterValue = resolveParameter(parameter);
            if (!"".equals(resolvedParameterValue)) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedParameterValue));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveParameter(String parameter) {
        String base = removeCurlyBrace(parameter);

        int index = base.indexOf('.');

        if (index == -1) {
            return parameter;
        }

        String module = base.substring(0, index);
        String key = base.substring(index + 1);

        Thesaurus thesaurus = getThesaurus(module, Layer.DOMAIN);
        if (thesaurus != null) {
            String result = thesaurus.getString(key, parameter);
            if (result.equals(parameter)) {
                return parameter;
            } else {
                return interpolate(result);
            }
        } else {
            return parameter;
        }
    }

    private String removeCurlyBrace(String parameter) {
        return parameter.substring(1, parameter.length() - 1);
    }

    private MessageInterpolator.Context interpolationContext(final ConstraintViolation<?> violation) {
        return new MessageInterpolator.Context() {

            @Override
            public <T> T unwrap(Class<T> clazz) {
                return clazz.cast(this);
            }

            @Override
            public Object getValidatedValue() {
                return violation.getInvalidValue();
            }

            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor() {
                return violation.getConstraintDescriptor();
            }
        };
    }

    /**
     * Adapts a {@link MessageSeedProvider} so that it implements
     * the {@link TranslationKeyProvider} interface to be able to
     * pass it to {@link ThesaurusImpl#createNewTranslationKeys}
     * as that is clever enough to check if the key already exists.
     */
    private static class MessageSeedProviderAdapterForTranslationKeyProvider implements TranslationKeyProvider {
        private final MessageSeedProvider messageSeedProvider;

        private MessageSeedProviderAdapterForTranslationKeyProvider(MessageSeedProvider messageSeedProvider) {
            this.messageSeedProvider = messageSeedProvider;
        }

        @Override
        public String getComponentName() {
            return this.messageSeedProvider.getSeeds().get(0).getModule();
        }

        @Override
        public Layer getLayer() {
            return this.messageSeedProvider.getLayer();
        }

        @Override
        public List<TranslationKey> getKeys() {
            return this.messageSeedProvider
                    .getSeeds()
                    .stream()
                    .map(MessageSeedAdapterForTranslationKey::new)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Adapts a {@link MessageSeed} so that it implements
     * the {@link TranslationKey} interface to be able
     * to pass it {@link ThesaurusImpl#createNewTranslationKeys}
     * as that is clever enough to check if the key already exists.
     */
    private static class MessageSeedAdapterForTranslationKey implements TranslationKey {
        private final MessageSeed messageSeed;

        private MessageSeedAdapterForTranslationKey(MessageSeed messageSeed) {
            this.messageSeed = messageSeed;
        }

        @Override
        public String getKey() {
            return this.messageSeed.getKey();
        }

        @Override
        public String getDefaultFormat() {
            return this.messageSeed.getDefaultFormat();
        }
    }

    static class NlsInstaller implements FullInstaller {
        private final NlsServiceImpl nlsService;

        @Inject
        NlsInstaller(NlsService nlsService) {
            this.nlsService = (NlsServiceImpl) nlsService;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader) {
            nlsService.doInstall(dataModelUpgrader);
        }

    }
}
