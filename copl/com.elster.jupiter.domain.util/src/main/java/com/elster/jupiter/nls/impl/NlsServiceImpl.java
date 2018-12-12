/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.PrivilegeThesaurus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.nls", service = {NlsService.class}, property = {"name=" + NlsService.COMPONENTNAME, "osgi.command.scope=nls", "osgi.command.function=addTranslation"})
public class NlsServiceImpl implements NlsService {

    private static final Pattern MESSAGE_PARAMETER_PATTERN = Pattern.compile("(\\{[^\\}]+?\\})");

    private BundleContext bundleContext;
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
    private volatile FileSystem fileSystem;

    private final Object translationLock = new Object();
    private Languages languages;
    private final Map<Pair<String, Layer>, ThesaurusImpl> thesauri = new ConcurrentHashMap<>();

    private Map<NlsKey, NlsKeyImpl> uninstalledKeysMap = new HashMap<>();

    // For OSGi purposes
    public NlsServiceImpl() {
    }

    // For testing purposes
    @Inject
    public NlsServiceImpl(BundleContext context, FileSystem fileSystem, OrmService ormService, ThreadPrincipalService threadPrincipalService, TransactionService transactionService, ValidationProviderResolver validationProviderResolver, UpgradeService upgradeService) {
        this();
        setFileSystem(fileSystem);
        setOrmService(ormService);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setValidationProviderResolver(validationProviderResolver);
        setUpgradeService(upgradeService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.languages = Languages.withSettingsOf(this);
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(MessageInterpolator.class).toInstance(messageInterpolator);
                bind(NlsService.class).toInstance(NlsServiceImpl.this);
            }
        });
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                dataModel,
                NlsInstaller.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class
                ));
        this.completeTranslationWhiteboard();
        installed = true; // upgradeService either installed, was up to date, or threw an Exception because upgrade was needed; in any case if we get here installed is true
    }

    private void completeTranslationWhiteboard() {
        synchronized (translationLock) {
            if (this.transactionService.isInTransaction()) {
                this.doCompleteTranslationWhiteboard();
            } else {
                try (TransactionContext context = this.transactionService.getContext()) {
                    this.doCompleteTranslationWhiteboard();
                    context.commit();
                }
            }
        }
    }

    private void doCompleteTranslationWhiteboard() {
        translationKeyProviders.forEach(this::doInstallProvider);
        messageSeedProviders.forEach(this::doInstallProvider);
    }

    @Deactivate
    public final void deactivate() {
        this.languages.deactivate();
    }

    @Override
    public ThesaurusImpl getThesaurus(String componentName, Layer layer) {
        return thesauri
                .computeIfAbsent(
                        Pair.of(componentName, layer),
                        componentAndLayer -> dataModel.getInstance(ThesaurusImpl.class).init(componentAndLayer.getFirst(), componentAndLayer.getLast()));
    }

    @Override
    public PrivilegeThesaurus getPrivilegeThesaurus() {
        return new GlobalThesaurus();
    }

    @Override
    public TranslationBuilder translate(NlsKey key) {
        return new TranslationBuilderImpl(key);
    }

    BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public void addTranslations(InputStream in, Locale locale) {
        new TranslationBatchCreator(locale, this).addTranslations(in);
    }

    @Override
    public void updateTranslations(InputStream in, Locale locale) {
        new TranslationBatchUpdater(locale, this).addTranslations(in);
    }

    FileSystem getFileSystem() {
        return fileSystem;
    }

    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
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

    @Reference(target = "(com.elster.jupiter.checklist=Pulse)")
    public void setCheckList(UpgradeCheckList upgradeCheckList) {
        // just explicitly depend
    }

    @Reference(name = "ZTranslationProvider", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    public void addTranslationKeyProvider(TranslationKeyProvider provider) {
        synchronized (translationLock) {
            this.translationKeyProviders.add(provider);
            if (installed) {
                try {
                    setPrincipal();
                    threadPrincipalService.set("INSTALL-translation", provider.getComponentName());
                    if (transactionService.isInTransaction()) {
                        doInstallProvider(provider);
                    } else {
                        try (TransactionContext context = this.transactionService.getContext()) {
                            doInstallProvider(provider);
                            context.commit();
                        }
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
    public void addMessageSeedProvider(MessageSeedProvider provider) {
        synchronized (translationLock) {
            this.messageSeedProviders.add(provider);
            if (installed) {
                try {
                    this.setPrincipal();
                    if (this.transactionService.isInTransaction()) {
                        doInstallProvider(provider);
                    } else {
                        try (TransactionContext context = this.transactionService.getContext()) {
                            this.doInstallProvider(provider);
                            context.commit();
                        }
                    }
                } finally {
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

    private void doInstall(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        installed = true;
    }

    private void doInstallProvider(TranslationKeyProvider provider) {
        String componentName = provider.getComponentName();
        Layer layer = provider.getLayer();
        ThesaurusImpl thesaurus = getThesaurus(componentName, layer);
        thesaurus.createNewTranslationKeys(provider, this.languages);
        new HashMap<>(uninstalledKeysMap).entrySet()
                .stream()
                .filter(mapEntry -> mapEntry.getKey().getComponent().equals(componentName))
                .filter(mapEntry -> thesaurus.hasKey(mapEntry.getValue().getKey()))
                .forEach(mapEntry -> {
                    NlsKey key = mapEntry.getKey();
                    Condition condition = where("nlsKey.componentName").isEqualTo(key.getComponent())
                            .and(where("nlsKey.layer").isEqualTo(key.getLayer()))
                            .and(where("nlsKey.key").isEqualTo(key.getKey()));
                    this.dataModel
                            .stream(NlsEntry.class).join(NlsKey.class)
                            .filter(condition)
                            .forEach(entry -> mapEntry.getValue().add(entry.getLocale(), entry.getTranslation()));
                    uninstalledKeysMap.remove(mapEntry);
                });
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
            dataModel.mapper(NlsKeyImpl.class).persist(newNlsKey(componentName, layer, key, defaultMessage, false));
            invalidate(componentName, layer);
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

    private NlsKeyImpl newNlsKey(String component, Layer layer, String key, String defaultFormat, boolean addDefaultMessageToDefaultLanguage) {
        NlsKeyImpl nlsKey = dataModel.getInstance(NlsKeyImpl.class).init(component, layer, key);
        nlsKey.setDefaultMessage(defaultFormat);
        if (addDefaultMessageToDefaultLanguage) {
            nlsKey.add(Locale.ENGLISH, defaultFormat);
        }
        return nlsKey;
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

    @Override
    public void copy(NlsKey key, String targetComponent, Layer targetLayer, Function<String, String> keyMapper) {
        NlsKeyImpl newKey = this.newNlsKey(targetComponent, targetLayer, keyMapper.apply(key.getKey()), key.getDefaultMessage(), false);
        Condition condition = where("nlsKey.componentName").isEqualTo(key.getComponent())
                .and(where("nlsKey.layer").isEqualTo(key.getLayer()))
                .and(where("nlsKey.key").isEqualTo(key.getKey()));

        QueryStream<NlsEntry> nlsEntryQueryStream = this.dataModel
                .stream(NlsEntry.class).join(NlsKey.class);
        List<NlsEntry> select = nlsEntryQueryStream
                .filter(condition)
                .select();
        if (select.isEmpty()) {
            this.uninstalledKeysMap.put(key, newKey);
        } else {
            select.forEach(entry -> newKey.add(entry.getLocale(), entry.getTranslation()));
        }
        this.dataModel.mapper(NlsKey.class).persist(newKey);
        this.invalidate(targetComponent, targetLayer);
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

    void invalidate(String component, Layer layer) {
        Optional.ofNullable(thesauri.get(Pair.of(component, layer)))
                .ifPresent(IThesaurus::invalidate);
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

    private final class TranslationBuilderImpl implements TranslationBuilder {
        private final NlsKey key;

        private final Map<Locale, String> translations = new HashMap<>();

        private TranslationBuilderImpl(NlsKey key) {
            this.key = key;
        }

        @Override
        public TranslationBuilder to(Locale locale, String translation) {
            translations.put(locale, translation);
            return this;
        }

        @Override
        public void add() {
            NlsKeyImpl nlsKey = dataModel.mapper(NlsKeyImpl.class).getOptional(key.getComponent(), key.getLayer(), key.getKey())
                    .orElseGet(() -> newNlsKey(key.getComponent(), key.getLayer(), key.getKey(), key.getDefaultMessage(), true));
            translations.forEach(nlsKey::add);
            nlsKey.save();
            invalidate(key.getComponent(), key.getLayer());
        }

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
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            nlsService.doInstall(dataModelUpgrader);
        }

    }

    private class GlobalThesaurus implements PrivilegeThesaurus {
        @Override
        public String translateComponentName(String privilegeKey) {
            return this.getString(privilegeKey, privilegeKey);
        }

        @Override
        public String translateResourceName(String privilegeKey) {
            return this.getString(privilegeKey, privilegeKey);
        }

        @Override
        public String translatePrivilegeKey(String privilegeKey) {
            return this.getString(privilegeKey, privilegeKey);
        }

        private String getString(String key, String defaultMessage) {
            for (IThesaurus thesaurus : thesauri.values()) {
                String attempt = thesaurus.getString(key, null);
                if (attempt != null) {
                    return attempt;
                }
            }
            return defaultMessage;
        }
    }

}
