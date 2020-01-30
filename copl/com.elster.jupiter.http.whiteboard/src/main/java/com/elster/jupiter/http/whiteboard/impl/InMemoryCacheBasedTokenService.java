package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.Map;

@Component(
        name = "com.elster.jupiter.http.whiteboard.InMemoryCacheBasedTokenService",
        property = {
                "name=" + InMemoryCacheBasedTokenService.SERVICE_NAME
        },
        immediate = true,
        service = {
                TokenService.class
        }
)
public class InMemoryCacheBasedTokenService implements TokenService {

    public static final String SERVICE_NAME = "TNS";

    private volatile DataVaultService dataVaultService;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile DataModel dataModel;

    private volatile SecurityTokenImpl securityToken;

    private int TIMEOUT;
    private int TOKEN_REFRESH_MAX_COUNT;
    private int TOKEN_EXPIRATION_TIME;

//    @Activate
//    public void activate(final BundleContext bundleContext) {
//        initializeDataModel();
//        initializeSecurityTokenImplementation();
//    }

    @Override
    public SignedJWT createSignedJWT(final Map<String, Object> customClaims) {
        return null;
    }

    @Override
    public SignedJWT createSignedJWT(final Map<String, Object> customClaims, final Instant expirationDateTime) {
        return null;
    }

    @Override
    public void validateSignedJWT(final SignedJWT signedJWT) throws JOSEException {

    }

    @Override
    public void invalidateSignedJWT(final SignedJWT signedJWT) {

    }

//    private void initializeDataModel() {
//        dataModel.register(new AbstractModule() {
//            @Override
//            protected void configure() {
//                bind(DataModel.class).toInstance(dataModel);
//                bind(DataVaultService.class).toInstance(dataVaultService);
//                bind(EventService.class).toInstance(eventService);
//                bind(MessageService.class).toInstance(messageService);
//            }
//        });
//    }

//    private void initializeSecurityTokenImplementation() {
//        Optional<KeyStoreImpl> keyStore = getKeyPair();
//
//        keyStore.ifPresent(keyStr -> {
//            try {
//                this.securityToken = new SecurityTokenImpl(
//                        dataVaultService.decrypt(keyStr.getPublicKey()),
//                        dataVaultService.decrypt(keyStr.getPrivateKey()),
//                        TOKEN_EXPIRATION_TIME,
//                        TOKEN_REFRESH_MAX_COUNT,
//                        TIMEOUT
//                );
//            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//                e.printStackTrace();
//            }
//        });
//    }

//    private Optional<KeyStoreImpl> getKeyPair() {
//        List<KeyStoreImpl> keys = new ArrayList<>(dataModel.mapper(KeyStoreImpl.class).find());
//        if (!keys.isEmpty()) {
//            return Optional.of(keys.get(0));
//        }
//        return Optional.empty();
//    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(WhiteBoardImpl.COMPONENTNAME, "HTTP Whiteboard");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void setSecurityToken(SecurityTokenImpl securityToken) {
        this.securityToken = securityToken;
    }
}
