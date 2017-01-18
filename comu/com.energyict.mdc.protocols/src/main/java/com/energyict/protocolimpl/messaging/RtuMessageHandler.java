package com.energyict.protocolimpl.messaging;

import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation must be used on a public method of a class extending the {@link AnnotatedMessaging} class.
 * The method should return an {@link MessageResult} and should take one and only one argument,
 * a class that extends the {@link AnnotatedMessage} interface.
 * <p/>
 * When an Annotated message is received in the {@link AnnotatedMessaging#queryMessage(MessageEntry)} method,
 * the matching handler method is found by looking for these {@link RtuMessageHandler} annotations. If there are multiple
 * methods found that can handle a given instance of the {@link AnnotatedMessage}, the {@link RtuMessageHandler#tag()} value
 * is used to find an unambiguous matching method that can handle the given message.
 * <p/>
 * There should never be two message handler methods that have the same signature (even if the method name is different),
 * and are both using the same tag value (default value of "" or a user defined value.
 * <p/>
 * Copyrights EnergyICT
 * Date: 6/6/12
 * Time: 11:16 PM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RtuMessageHandler {

    /**
     * @return The tag value, used to match an annotated RtuMessage to a given method
     */
    String tag() default "";

}
