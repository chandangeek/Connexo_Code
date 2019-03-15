/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.LdapServerException;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSecurableLdapDirectoryImpl extends AbstractLdapDirectoryImpl {
    protected static final String MEMBER = "member";
    protected static final String[] MEMBER_ARRAY = { MEMBER };
    @Inject
    protected AbstractSecurableLdapDirectoryImpl(DataModel dataModel, UserService userService, BundleContext context) {
        super(dataModel, userService, context);
    }

    protected abstract DirContext createDirContextSimple(String url) throws NamingException;

    protected abstract DirContext createDirContextSsl(String url, SslSecurityProperties sslSecurityProperties)
            throws NamingException;

    protected abstract Pair<LdapContext, StartTlsResponse> createDirContextTls(String url,
            SslSecurityProperties sslSecurityProperties) throws NamingException, IOException;

    protected interface FunctionWithNamingException<T, R> {
        R apply(T t, Object... args) throws NamingException;
    }

    protected <T> T getSomethingFromLdap(FunctionWithNamingException<DirContext, T> doGetSomething, Object... args) {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return getSomethingSimple(urls, doGetSomething, args);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return getSomethingSSL(urls, getSslSecurityProperties(), doGetSomething, args);
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return getSomethingTLS(urls, getSslSecurityProperties(), doGetSomething, args);
        } else {
            return null;
        }
    }

    private <T> T getSomethingSimple(List<String> urls, FunctionWithNamingException<DirContext, T> doGetSomething,
            Object... args) {
        try {
            DirContext ctx = createDirContextSimple(urls.get(0));
            return doGetSomething.apply(ctx, args);
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getSomethingSimple(urls, doGetSomething, args);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private <T> T getSomethingSSL(List<String> urls, SslSecurityProperties sslSecurityProperties,
            FunctionWithNamingException<DirContext, T> doGetSomething, Object... args) {
        try {
            DirContext ctx = createDirContextSsl(urls.get(0), sslSecurityProperties);
            return doGetSomething.apply(ctx, args);
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getSomethingSSL(urls, sslSecurityProperties, doGetSomething, args);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private <T> T getSomethingTLS(List<String> urls, SslSecurityProperties sslSecurityProperties,
            FunctionWithNamingException<DirContext, T> doGetSomething, Object... args) {
        StartTlsResponse tls = null;
        try {
            Pair<LdapContext, StartTlsResponse> pair = createDirContextTls(urls.get(0), sslSecurityProperties);
            LdapContext ctx = pair.getFirst();
            tls = pair.getLast();
            return doGetSomething.apply(ctx, args);
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getSomethingTLS(urls, sslSecurityProperties, doGetSomething, args);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        } finally {
            if (tls != null) {
                try {
                    tls.close();
                } catch (IOException e) {
                    throw new UnderlyingIOException(e);
                }
            }
        }
    }

    protected final List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        urls.add(getUrl().trim());
        if (getBackupUrl() != null) {
            String[] backupUrls = getBackupUrl().split(";");
            Arrays.stream(backupUrls).forEach(s -> urls.add(getRealGroupName(s).trim()));
        }
        return urls;
    }

    @Override
    public List<String> getGroupNames() {
        if (getBaseGroup() == null) {
            return Collections.emptyList();
        }
        return getSomethingFromLdap(this::doGetGroupNames);
    }

    private List<String> doGetGroupNames(DirContext context, Object... args) throws NamingException {
        List<String> names = new ArrayList<>();
        SearchControls controls = new SearchControls(SearchControls.OBJECT_SCOPE, 0, 0, MEMBER_ARRAY, true, true);
        NamingEnumeration<SearchResult> groupEnumeration = context.search(getBaseGroup(), "(objectClass=groupOfNames)",
                controls);
        if (groupEnumeration.hasMore()) {
            Attribute memberAttributes = groupEnumeration.next().getAttributes().get(MEMBER);
            NamingEnumeration<?> membersEnumeration = memberAttributes.getAll();
            while (membersEnumeration.hasMore()) {
                String member = (String) membersEnumeration.next();
                names.add(getRealGroupName(member));
            }
        }
        return names;
    }

    protected String getRealGroupName(String member) {
        return member;
    }


}
