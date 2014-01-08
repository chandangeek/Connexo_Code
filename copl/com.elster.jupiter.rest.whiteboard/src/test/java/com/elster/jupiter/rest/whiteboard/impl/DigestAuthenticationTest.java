package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DigestAuthenticationTest {

    private DigestAuthentication digestAuthentication;

    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpServletRequest request;
    @Mock
    private WhiteBoardConfigurationProvider serviceLocator;
    @Mock
    private UserService userService;
    @Mock
    private User user;

    @Before
    public void setUp() throws Exception {
        digestAuthentication = new DigestAuthentication(userService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testHandleSecurity() throws IOException {
        when(request.getHeader("Authorization")).thenReturn("Digest username=\"Penny\",uri=\"/dir/index.html\",nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",qop=auth,nc=00000001,cnonce=\"0a4f113b\",response=\"f71da74905550a2d16751f77e123e384\"");
        when(request.getMethod()).thenReturn("GET");
        when(userService.findUser("Penny")).thenReturn(Optional.of(user));
        when(user.getDigestHa1()).thenReturn("ha1");

        boolean answer = digestAuthentication.handleSecurity(request, response);

        assertThat(answer).isTrue();
    }

}
