/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapServerException;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;

import org.osgi.framework.BundleContext;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSecurableLdapDirectoryImplTest {
    private final static class MyNamingEnumeration<T> implements NamingEnumeration<T> {

        private final Iterator<T> iter;

        public MyNamingEnumeration(Iterator<T> iter) {
            super();
            this.iter = iter;
        }

        @Override
        public boolean hasMoreElements() {
            return iter.hasNext();
        }

        @Override
        public T nextElement() {
            return iter.next();
        }

        @Override
        public void close() throws NamingException {
        }

        @Override
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        @Override
        public T next() throws NamingException {
            return nextElement();
        }
    }

    private final static class MyLdapDirectoryImpl extends AbstractSecurableLdapDirectoryImpl {
        final LdapContext dirContext;
        final StartTlsResponse tlsResponse;

        public MyLdapDirectoryImpl(DataModel dataModel, UserService userService, BundleContext context,
                LdapContext dirContext, StartTlsResponse tlsResponse) {
            super(dataModel, userService, context);
            this.dirContext = dirContext;
            this.tlsResponse = tlsResponse;
        }

        @Override
        public List<LdapUser> getLdapUsers() {
            return null;
        }

        @Override
        public boolean getLdapUserStatus(String userName) {
            return false;
        }

        @Override
        public Optional<User> authenticate(String name, String password) {
            return null;
        }

        @Override
        protected DirContext createDirContextSimple(String url) throws NamingException {
            return dirContext;
        }

        @Override
        protected DirContext createDirContextSsl(String url, SslSecurityProperties sslSecurityProperties)
                throws NamingException {
            return dirContext;
        }

        @Override
        protected Pair<LdapContext, StartTlsResponse> createDirContextTls(String url,
                SslSecurityProperties sslSecurityProperties) throws NamingException, IOException {
            return Pair.of(dirContext, tlsResponse);
        }

        @Override
        protected List<Group> doGetGroups(DirContext context, Object... args) throws NamingException {
            return null;
        }

    }

    private MyLdapDirectoryImpl sut;

    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserService userService;
    @Mock
    private BundleContext context;
    @Mock
    private LdapContext dirContext;
    @Mock
    private StartTlsResponse tlsResponse;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SearchResult searchResult1;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SearchResult searchResult2;

    @Before
    public void setup() {
        sut = new MyLdapDirectoryImpl(dataModel, userService, context, dirContext, tlsResponse);
        sut.setUrl("my main url");
        sut.setBackupUrl("my first url; my second url");

        when(userService.getDataVaultService().decrypt(anyString())).thenReturn("some string".getBytes());
    }

    @Test
    public void testGetGroupNamesWithoutBaseGroup() {
        assertThat(sut.getGroupNames()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = LdapServerException.class)
    public void testGetGroupNamesShouldThrowLdapServerExceptionWhenExceptionHappens() throws NamingException {
        sut.setBaseGroup("my base group");
        when(dirContext.search(anyString(), anyString(), any(SearchControls.class))).thenThrow(NamingException.class);

        sut.getGroupNames();
    }

    @Test
    public void testGetGroupNames() throws NamingException {
        sut.setBaseGroup("cn=my base group,dc=some, dc=com");
        NamingEnumeration<SearchResult> groupEnumeration = new MyNamingEnumeration<>(
                Arrays.asList(searchResult1, searchResult2).iterator());
        String group1Name = "firstGroup";
        String group2Name = "secondGroup";
        prepareAttribute(searchResult1, group1Name);
        prepareAttribute(searchResult2, group2Name);

        when(dirContext.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(groupEnumeration);

        assertThat(sut.getGroupNames()).hasSize(2).contains(group1Name).contains(group2Name);
    }

    private void prepareAttribute(SearchResult searchResult, String groupName) throws NamingException {
        Attribute cnAttribute = mock(Attribute.class);
        when(searchResult.getAttributes().get(AbstractSecurableLdapDirectoryImpl.CN)).thenReturn(cnAttribute);
        when(cnAttribute.get()).thenReturn(groupName);
    }

}
