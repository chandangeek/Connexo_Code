package com.elster.jupiter.devtools.tests;

import org.mockito.Mockito;
import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues;
import org.mockito.invocation.InvocationOnMock;

import java.util.Arrays;

public class FakeBuilder {

    /**
     *
     * @param build The object built by the builder (so returned by add() or build() for example)
     * @param builderInterface The interface the builder implements
     * @param subInterfaces Ask Tom
     * @param <T>
     * @return A fully mocked builder instance
     */
    public static <T> T initBuilderStub(final Object build, Class<T> builderInterface, Class<?>... subInterfaces) {
        final Class<?>[] allInterfaces = new Class<?>[subInterfaces.length + 1];
        allInterfaces[0] = builderInterface;
        System.arraycopy(subInterfaces, 0, allInterfaces, 1, subInterfaces.length);
        return Mockito.mock(builderInterface, Mockito.withSettings().defaultAnswer(
                new ReturnsEmptyValues() {
                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        if (Arrays.stream(allInterfaces).anyMatch(aClass -> aClass.isAssignableFrom(invocation.getMethod().getReturnType()))) {
                            return invocation.getMock();
                        } else {
                            return build;
                        }
                    }
                }));
    }

}
