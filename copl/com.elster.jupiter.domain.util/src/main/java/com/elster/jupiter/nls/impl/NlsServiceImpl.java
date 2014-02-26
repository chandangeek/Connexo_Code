package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.AbstractModule;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.metadata.ConstraintDescriptor;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(name = "com.elster.jupiter.nls", service = {NlsService.class, InstallService.class}, property = {"name=" + NlsService.COMPONENTNAME, "osgi.command.scope=nls", "osgi.command.function=addTranslation"})
public class NlsServiceImpl implements NlsService, InstallService {

	private static final Pattern MESSAGE_PARAMETER_PATTERN = Pattern.compile( "(\\{[^\\}]+?\\})" );
	
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MessageInterpolator messageInterpolator;

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
    }

    @Deactivate
    public void deactivate() {

    }

    public NlsServiceImpl() {
    }

    @Inject
    public NlsServiceImpl(OrmService ormService, ThreadPrincipalService threadPrincipalService,ValidationProviderResolver validationProviderResolver) {
        setOrmService(ormService);
        setThreadPrincipalService(threadPrincipalService);
        setValidationProviderResolver(validationProviderResolver);
        activate();
        if (!dataModel.isInstalled()) {
            dataModel.install(true, true);
        }
    }

    @Override
    public Thesaurus getThesaurus(String componentName, Layer layer) {
        return dataModel.getInstance(ThesaurusImpl.class).init(componentName, layer);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
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
    public final void setValidationProviderResolver(ValidationProviderResolver validationProviderResolver) {
    	this.messageInterpolator = Validation.byDefaultProvider()
            	.providerResolver(validationProviderResolver)
            	.configure()
            	.getDefaultMessageInterpolator();
    }
    
    @Override
    public void install() {
        dataModel.install(true, true);
    }

    public void addTranslation(String componentName, String layerName, String key, String defaultMessage) {
        try {
            Layer layer = Layer.valueOf(layerName);
            Thesaurus thesaurus = getThesaurus(componentName, layer);
            SimpleNlsKey nlsKey = SimpleNlsKey.key(componentName, layer, key).defaultMessage(defaultMessage);
            Translation translation = SimpleTranslation.translation(nlsKey, Locale.ENGLISH, defaultMessage);
            thesaurus.addTranslations(Arrays.asList(translation));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void addTranslation(Object... args) {
        System.out.println("Usage : \n\n addTranslation componentName layerName key defaultMessage");
    }
    
	@Override
	public String interpolate(ConstraintViolation<?> violation) {
		return messageInterpolator.interpolate(interpolate(violation.getMessageTemplate()),interpolationContext(violation),threadPrincipalService.getLocale());
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
				matcher.appendReplacement( sb, Matcher.quoteReplacement( resolvedParameterValue ) );
			}
		}
		matcher.appendTail( sb );
		return sb.toString();
	}
	
	private String resolveParameter(String parameter) {
		String base = removeCurlyBrace(parameter);
		String[] parts = base.split("\\.");
		if (parts.length < 2) {
			return parameter;
		}
		Thesaurus thesaurus = getThesaurus(parts[0], Layer.DOMAIN);
		if (thesaurus != null) {
			String result = thesaurus.getString(parts[1], parameter);
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
		return parameter.substring(1,parameter.length() - 1);
	}
	
	private MessageInterpolator.Context interpolationContext (final ConstraintViolation<?> violation) {
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
}
