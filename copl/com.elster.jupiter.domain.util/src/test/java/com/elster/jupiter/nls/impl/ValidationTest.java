package com.elster.jupiter.nls.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidationTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    
    private static final boolean printSql = false;

    @BeforeClass
    public static void setUp() throws SQLException {
        injector = Guice.createInjector(
        			inMemoryBootstrapModule,  
        			new OrmModule(),
        			new UtilModule(), 
        			new ThreadSecurityModule(), 
        			new PubSubModule(), 
        			new TransactionModule(printSql),
                    new NlsModule()
                );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(NlsService.class);
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    private NlsService getNlsService() {
        return injector.getInstance(NlsService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @Test
    public void testCrud()  {
    	NlsService nlsService = getNlsService();
        try (TransactionContext context = getTransactionService().getContext()) {
        	Thesaurus thesaurus = nlsService.getThesaurus("DUM", Layer.DOMAIN);
        	thesaurus.addTranslations(getTranslations());
         	context.commit();
        }
        ThreadPrincipalService threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
        threadPrincipalService.set(threadPrincipalService.getPrincipal(),"module","action",Locale.FRANCE);
        Validator validator = ((NlsServiceImpl) nlsService).getDataModel().getValidatorFactory().getValidator();
        for (ConstraintViolation<?> violation : validator.validate(new Bean())) {
    		String message = nlsService.interpolate(violation);
    		if (violation.getConstraintDescriptor().getAnnotation().annotationType().equals(NotNull.class)) {
    			assertThat(message).isEqualTo("vide (ne peut pas \u00eatre nul)");
    		} else {
    			assertThat(message).isEqualTo("svp ne laissez pas vide (ne peut pas \u00eatre nul), valeur minimal: 10");
    		}
    	}
    }
    
    private List<Translation> getTranslations() {
    	List<Translation> result = new ArrayList<>();
    	SimpleNlsKey nlsKey = SimpleNlsKey.key("DUM", Layer.DOMAIN, "empty");
    	result.add(SimpleTranslation.translation(nlsKey,Locale.FRANCE,"vide ({javax.validation.constraints.NotNull.message})"));
    	nlsKey = SimpleNlsKey.key("DUM", Layer.DOMAIN, "min.size");
    	result.add(SimpleTranslation.translation(nlsKey,Locale.FRANCE,"svp ne laissez pas {DUM.empty}, valeur minimal: {min}"));
    	return result;
    }
    
    public class Bean {
    	@NotNull(message="{DUM.empty}")    	
    	private String name;
    	@Size(message="{DUM.min.size}", min=10 )
    	private String description = "N/A";
    }

}