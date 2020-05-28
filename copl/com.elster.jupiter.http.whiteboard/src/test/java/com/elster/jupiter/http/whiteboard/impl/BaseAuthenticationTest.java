package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.http.whiteboard.CSRFFilterService;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.SamlRequestService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.blacklist.BlackListTokenService;

import org.mockito.Answers;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class BaseAuthenticationTest {

    protected static final String SSO_ENABLED_PROPERTY = "sso.enabled";
    protected static final String SSO_IDP_ENDPOINT_PROPERTY = "sso.idp.endpoint";
    protected static final String SSO_SP_ISSUER_ID = "sso.sp.issuer.id";
    protected static final String SSO_ACS_ENDPOINT_PROPERTY = "sso.acs.endpoint";
    protected static final String INSTALL_DIR_PROPERTY = "install.dir";
    protected static final String LOGIN_URL = "/apps/login/";

    @Mock
    protected UserService userService;
    @Mock
    protected OrmService ormService;
    @Mock
    protected DataVaultService dataVaultService;
    @Mock
    protected UpgradeService upgradeService;
    @Mock
    protected BpmService bpmService;
    @Mock
    protected DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected Table table;
    @Mock
    protected DataMapper<KeyStoreImpl> keyStoreDataMapper;
    @Mock
    protected BundleContext context;
    @Mock
    protected SamlRequestService samlRequestService;
    @Mock
    protected BlackListTokenService blackListdTokenService;
    @Mock
    protected CSRFFilterService csrfFilterService;
    @Mock
    protected TokenService tokenService;

    protected HttpAuthenticationService getHttpAuthentication() throws InvalidKeySpecException, NoSuchAlgorithmException {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.isInstalled()).thenReturn(true);
        when(context.getProperty(SSO_ENABLED_PROPERTY)).thenReturn("true");
        when(context.getProperty(SSO_ACS_ENDPOINT_PROPERTY)).thenReturn("/security/acs");
        when(context.getProperty(SSO_IDP_ENDPOINT_PROPERTY)).thenReturn("http://idp.connexo.com/saml2/http-post/sso/973243");
        when(context.getProperty(SSO_SP_ISSUER_ID)).thenReturn("http://sp.connexo.com/saml2");
        when(context.getProperty(INSTALL_DIR_PROPERTY)).thenReturn(anyString());

        when(dataModel.mapper(KeyStoreImpl.class)).thenReturn(keyStoreDataMapper);
        BasicAuthentication basicAuthentication = new BasicAuthentication(userService, ormService, dataVaultService,
					upgradeService, bpmService, context,blackListdTokenService, tokenService, csrfFilterService);
        basicAuthentication.setSamlRequestService(samlRequestService);
        return basicAuthentication;
    }
}
