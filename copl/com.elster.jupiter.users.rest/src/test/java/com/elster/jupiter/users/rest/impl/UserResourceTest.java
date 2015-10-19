package com.elster.jupiter.users.rest.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import javax.ws.rs.client.Entity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.rest.UserInfo;

public class UserResourceTest extends UsersRestApplicationJerseyTest {
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockTransaction();
    }
    
    @Test
    public void testNothingToUpdate() {
        User user = mockUser(1L);
        UserInfo info = new UserInfo(user);
        
        target("/users/1").request().put(Entity.json(info));
        
        verify(user, VerificationModeFactory.times(0)).setDescription("description");
        verify(user, VerificationModeFactory.times(0)).setLocale(Locale.ENGLISH);
        verify(user, VerificationModeFactory.times(0)).update();
    }

    @Test
    public void testUpdateUserLocale() {
        User user = mockUser(1L);
        UserInfo info = new UserInfo(user);
        info.language = new LocaleInfo();
        info.language.languageTag = Locale.US.toLanguageTag();
        
        target("/users/1").request().put(Entity.json(info));
        
        verify(user).setLocale(Locale.US);
        verify(user).update();
    }
    
    @Test
    public void testReleaseUserLocale() {
        User user = mockUser(1L);
        UserInfo info = new UserInfo(user);
        info.language = null;
        
        target("/users/1").request().put(Entity.json(info));
        
        verify(user).setLocale(null);
        verify(user).update();
    }
    
    @Test
    public void testUpdateDescription() {
        User user = mockUser(1L);
        UserInfo info = new UserInfo(user);
        info.description = "new description";
        
        target("/users/1").request().put(Entity.json(info));
        
        verify(user).setDescription("new description");
        verify(user).update();
    }

    private User mockUser(long id) {
        User user = mock(User.class);
        when(userService.getUser(id)).thenReturn(Optional.of(user));
        when(user.getLocale()).thenReturn(Optional.of(Locale.ENGLISH));
        when(user.getLanguage()).thenReturn("en");
        when(user.getCreationDate()).thenReturn(Instant.now());
        when(user.getModifiedDate()).thenReturn(Instant.now());
        when(user.getDescription()).thenReturn("description");
        return user;
    }

    @SuppressWarnings("unchecked")
    private void mockTransaction() {
        when(transactionService.<Object>execute(Matchers.any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                @SuppressWarnings("rawtypes")
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                return transaction.perform();
            }
        });
    }
}
