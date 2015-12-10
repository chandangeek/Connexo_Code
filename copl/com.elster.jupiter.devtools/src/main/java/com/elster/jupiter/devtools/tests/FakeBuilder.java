package com.elster.jupiter.devtools.tests;

import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues;
import org.mockito.invocation.InvocationOnMock;

import java.util.Arrays;

public class FakeBuilder {

    public static <T> T initBuilderStub(final Object build, Class<T> builderInterface, Class<?>... subInterfaces) {
        final Class<?>[] allInterfaces = new Class<?>[subInterfaces.length + 1];
        allInterfaces[0] = builderInterface;
        System.arraycopy(subInterfaces, 0, allInterfaces, 1, subInterfaces.length);
        MockSettings mockSettings = Mockito.withSettings();
        if (subInterfaces.length > 0) {
            mockSettings.extraInterfaces(subInterfaces);
        }
        mockSettings.defaultAnswer(
                new ReturnsEmptyValues() {
                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        if (Arrays.stream(allInterfaces).anyMatch(aClass -> aClass.isAssignableFrom(invocation.getMethod().getReturnType()))) {
                            return invocation.getMock();
                        } else {
                            return build;
                        }
                    }
                });
        return Mockito.mock(builderInterface, mockSettings);
    }

}
