package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.common.Environment;
import org.junit.BeforeClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 10:37
 */
public abstract class AbstractEIWebTests {
    @BeforeClass
    public static void  setupEnvironment(){
        Environment environment = mock(Environment.class);
        Environment.DEFAULT.set(environment);
        when(environment.getTranslation(anyString())).thenAnswer(getTestTranslationAnswer());
        when(environment.getErrorMsg(anyString())).thenAnswer(getTestTranslationAnswer());
    }

    private static Answer<String> getTestTranslationAnswer() {
        return new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        };
    }
}
