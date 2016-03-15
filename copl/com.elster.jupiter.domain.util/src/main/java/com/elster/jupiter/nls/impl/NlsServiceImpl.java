package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.NestedTransactionException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.nls", service = {NlsService.class, InstallService.class}, property = {"name=" + NlsService.COMPONENTNAME, "osgi.command.scope=nls", "osgi.command.function=addTranslation"})
public class NlsServiceImpl implements NlsService, InstallService {

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

    private final Object translationLock = new Object();
    private final Map<Pair<String, Layer>, IThesaurus> thesauri = new HashMap<>();

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(MessageInterpolator.class).toInstance(messageInterpolator);
            }
        });
        installed = dataModel.isInstalled();
    }

    public NlsServiceImpl() {
    }

    @Inject
    public NlsServiceImpl(OrmService ormService, ThreadPrincipalService threadPrincipalService, TransactionService transactionService, ValidationProviderResolver validationProviderResolver) {
        this();
        setOrmService(ormService);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setValidationProviderResolver(validationProviderResolver);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public Thesaurus getThesaurus(String componentName, Layer layer) {
        ThesaurusImpl thesaurus = dataModel.getInstance(ThesaurusImpl.class).init(componentName, layer);
        thesauri.put(Pair.of(componentName, layer), thesaurus);
        return thesaurus;
    }

    @Override
    public TranslationBuilder translate(NlsKey key) {
        return new TranslationBuilderImpl(key);
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

    @Override
    public synchronized void install() {
        synchronized (translationLock) {
            dataModel.install(true, true);
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
        provider
            .getSeeds()
            .stream()
            .forEach(messageSeed -> this.addTranslation(provider.getLayer(), messageSeed));
    }

    private void addTranslation(Layer layer, MessageSeed messageSeed) {
        this.addTranslation(messageSeed.getModule(), layer.name(), messageSeed.getKey(), messageSeed.getDefaultFormat());
    }

    // Published as a gogo command so be wary when refactoring
    public void addTranslation(String componentName, String layerName, String key, String defaultMessage) {
        try {
            Layer layer = Layer.valueOf(layerName);
            dataModel.mapper(NlsKeyImpl.class).persist(newNlsKey(componentName, layer, key, defaultMessage));
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

    private NlsKeyImpl newNlsKey(String component, Layer layer, String key, String defaultFormat) {
        NlsKeyImpl nlsKey = dataModel.getInstance(NlsKeyImpl.class).init(component, layer, key);
        nlsKey.setDefaultMessage(defaultFormat);
        nlsKey.add(Locale.ENGLISH, defaultFormat);
        return nlsKey;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Collections.singletonList("ORM");
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
        NlsKeyImpl newKey = this.newNlsKey(targetComponent, targetLayer, keyMapper.apply(key.getKey()), key.getDefaultMessage());
        Condition condition = where("nlsKey.componentName").isEqualTo(key.getComponent())
                .and(where("nlsKey.layer").isEqualTo(key.getLayer()))
                .and(where("nlsKey.key").isEqualTo(key.getKey()));
        this.dataModel
                .stream(NlsEntry.class).join(NlsKey.class)
                .filter(condition)
                .forEach(entry -> newKey.add(entry.getLocale(), entry.getTranslation()));
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

    private void invalidate(String component, Layer layer) {
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
                    .orElseGet(() -> newNlsKey(key.getComponent(), key.getLayer(), key.getKey(), key.getDefaultMessage()));
            translations.forEach(nlsKey::add);
            nlsKey.save();
            invalidate(key.getComponent(), key.getLayer());
        }

    }

}
