package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.http.whiteboard.*;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(
        name = "com.elster.jupiter.http.whiteboard",
        service = {WhiteBoard.class, Application.class, InstallService.class, TranslationKeyProvider.class},
        property = {"alias=/apps", "name=HTW"},
        immediate = true)
public class WhiteBoardImpl extends Application implements BinderProvider, InstallService, TranslationKeyProvider, WhiteBoard {


    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile JsonService jsonService;
    private volatile LicenseService licenseService;
    private volatile TransactionService transactionService;
    private volatile DataModel dataModel;
    private volatile DataVaultService dataVaultService;
    private volatile QueryService queryService;

    private AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();

    private final static String TIMEOUT = "com.elster.jupiter.timeout";
    private final static String TOKEN_REFRESH_MAX_COUNT = "com.elster.jupiter.token.refresh.maxcount";
    private final static String TOKEN_EXPIRATION_TIME = "com.elster.jupiter.token.expirationtime";
    private  static int timeout = 300; // default value 5 min
    private  static long tokenRefreshMaxCount = 100;
    private static int tokenExpTime = 300;
    private static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC9WAYPBNeqvL0XFw+APuy75t68oOlPtHwWmyO53xNND0KYgIvAqE6ZeBeCdEKEtgugwPFYeUzMaVMpGqek4D/UDYJQdGcu7XHprqZEFb1n0ZaC2wOAyyqJgfJYk14l7HTkOxgt9FG95r1WM+XpZokC6nxli78cPLzc/V4AsvbdQwIDAQAB";
    private static String privateKey ="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAL1YBg8E16q8vRcXD4A+7Lvm3ryg6U+0fBabI7nfE00PQpiAi8CoTpl4F4J0QoS2C6DA8Vh5TMxpUykap6TgP9QNglB0Zy7tcemupkQVvWfRloLbA4DLKomB8liTXiXsdOQ7GC30Ub3mvVYz5elmiQLqfGWLvxw8vNz9XgCy9t1DAgMBAAECgYB1YnrvGLtz+GrqQ7uycFBn9aulGcVLSsObaDbv5uKaZZmN3jPGDxIbhx5cHCOZDxuKX9PAXANGvw11cDb6uqQWiSb4nhe2mdnr+t7rfGyQc45NOWhhJvbt2b6+fxnbeg4eW68cS3MjB7mDjzNT8WSiB1rZjqL1/yROOigkRW82IQJBAP52m/vEdbW1arSOUrhR730Lum2sbXgUuXtNAERTZMLL9ya0eKB+6du3tdIlx3JqWyp5ZYbHM+Rho639JjwMy3kCQQC+fL36iu1tt8v0ZFf4fm4ItshUAD9Yh081e2SYofgeWKOgaIwkYePw1SPstu0i1HlGk+/kh186L7b7b+9I70ObAkEAgcbcJrs7jUDI+uzbI0Ymbg/dNLIL4oIvVsMer7oYWYDMu+Cu5KvVeUloYZUC80dq126yaNqPjJp/b+z74wRjgQJBAKE/TT5qHiRTgrXlv0YeGB0ORTQ+ZWEWYWm/g19lNHjTxedCuOcPannyow99pe1m+SZSkq7cHTNPMtFTZrZRu+MCQDu5AqGJIVlXcvpjJspn2gRt8OzzI1+HoC+CqPK/LjG8AqyNYWbMq4ApWIdG7GoDfThkBtqRiok7k+El7gDJ9Mk=";
    private List<HttpResource> resources = new CopyOnWriteArrayList<>();
    private List<App> apps = new CopyOnWriteArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(WhiteBoardImpl.class.getName());

    public WhiteBoardImpl() {
        super();
    }

    @Inject
    public WhiteBoardImpl(OrmService ormService, TransactionService transactionService, QueryService queryService, DataVaultService dataVaultService) {
            this();
            setTransactionService(transactionService);
            setQueryService(queryService);
            setOrmService(ormService);
            setDataVaultService(dataVaultService);
            activate(null,null);
            if (!dataModel.isInstalled()) {
                install();
            }
        }



    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference
    public void setEventAdminService(EventAdmin eventAdminService) {
        this.eventAdminHolder.set(eventAdminService);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(WhiteBoard.COMPONENTNAME, "Whiteboard");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }



    @Reference(name = "ZResource", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(HttpResource resource) {

        String alias = getAlias(resource.getAlias());
        HttpContext httpContext = new HttpContextImpl(this, resource.getResolver(), userService, transactionService, eventAdminHolder);
        try {
            httpService.registerResources(alias, resource.getLocalName(), httpContext);
            resources.add(resource);
        } catch (NamespaceException e) {
            LOGGER.log(Level.SEVERE, "Error while registering " + alias + ": " + e.getMessage(), e);
            throw new UnderlyingNetworkException(e);
        }
    }


    @Reference(name = "ZApplication", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addApplication(App resource) {
        if (resource.isInternalApp()) {
            addResource(resource.getMainResource());
        }
        apps.add(resource);
    }

    @Activate
    public void activate(BundleContext context, Map<String, Object> props) {
        boolean generateEvents = props != null && Boolean.TRUE.equals(props.get("event"));
        if (!generateEvents) {
            eventAdminHolder.set(null);
        }
        if (context != null) {
            List<String> initList = new ArrayList<>();
            initList.add(TIMEOUT);
            initList.add(TOKEN_REFRESH_MAX_COUNT);
            initList.add(TOKEN_EXPIRATION_TIME);
            for(String initElement:initList){
                int configElement=0;
                String configParam = context.getProperty(initElement);
                if(configParam!=null) {
                    try {
                        configElement = Integer.parseInt(configParam);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Cannot parse '" + configParam + "'", e);
                    }
                }
                if(configElement>0) {
                    if(initElement.equals(TIMEOUT)) timeout = configElement;// *1000 since the check will be against millis
                    else if(initElement.equals(TOKEN_REFRESH_MAX_COUNT)) tokenRefreshMaxCount = configElement;
                    else tokenExpTime = configElement;
                }
            }

        }

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(DataModel.class).toInstance(dataModel);
                bind(QueryService.class).toInstance(queryService);
                bind(JsonService.class).toInstance(jsonService);
                bind(WhiteBoard.class).toInstance(WhiteBoardImpl.this);
            }
        });

        if(!getKeyPairDecrypted().isEmpty()) {
            publicKey = getKeyPairDecrypted().get("PUB");
            privateKey = getKeyPairDecrypted().get("PRV");
        }
    }

    public static int getTimeout() {
        return timeout;
    }

    public static long getTokenRefreshMaxCount() {
        return tokenRefreshMaxCount;
    }

    public static int getTokenExpTime() {
        return tokenExpTime;
    }

    @Override
    public KeyStore createKeystore() {
        return KeyStoreImpl.from(dataModel, this);
    }

    @Override
    public DataVaultService getDataVaultService() {
        return dataVaultService;
    }

    public static String getPrivateKey() {
        return privateKey;
    }

    public static String getPublicKey() {
        return publicKey;
    }

    public void removeResource(HttpResource resource) {
        httpService.unregister(getAlias(resource.getAlias()));
        resources.remove(resource);
    }

    public void removeApplication(App app) {
        if (app.getMainResource() != null) {
            removeResource(app.getMainResource());
        }
        apps.remove(app);
    }

    String getAlias(String name) {
        return "/apps" + (name.startsWith("/") ? name : "/" + name);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(PageResource.class, AppResource.class);
    }

    List<HttpResource> getResources() {
        return new ArrayList<>(resources);
    }

    LicenseService getLicenseService() {
        return licenseService;
    }

    List<App> getApps() {
        return apps;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                this.bind(jsonService).to(JsonService.class);
                this.bind(userService).to(UserService.class);
                this.bind(dataModel).to(DataModel.class);
                this.bind(queryService).to(QueryService.class);
                this.bind(WhiteBoardImpl.this).to(WhiteBoard.class);
            }
        };
    }

    @Override
    public void install() {

        InstallerImpl installer = new InstallerImpl(dataModel,this);
        installer.install();
        installer.addDefaults();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT");
    }

    @Override
    public String getComponentName() {
        return "HTW";
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    public Optional<KeyStore> getKeyPair() {
         try {
            List<KeyStore> keys = new ArrayList<>(dataModel.mapper(KeyStore.class).find());
            if (!keys.isEmpty()) return Optional.of(keys.get(0));
        }catch(UnderlyingSQLFailedException e){
            return Optional.empty();
        }
        return Optional.empty();
    }

    public Map<String, String> getKeyPairDecrypted() {
        Map<String, String> keyPair = new HashMap<>();
        if(getKeyPair().isPresent())
        keyPair= getKeyPair().get().getKeyPairDecrypted(this);
        return keyPair;
    }




}

	
