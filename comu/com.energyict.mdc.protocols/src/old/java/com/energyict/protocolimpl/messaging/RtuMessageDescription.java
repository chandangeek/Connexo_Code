package com.energyict.protocolimpl.messaging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This Annotation should be used to describe an annotated message (Message category, description, ...).
 * <p/>
 * Copyrights EnergyICT
 * Date: 6/6/12
 * Time: 11:16 PM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RtuMessageDescription {

    /**
     * This is the message category, used to group related messages together and used to show in EIServer
     *
     * @return The message category, or "Other" if missing
     */
    String category() default "Other";

    /**
     * Mark this message as advanced. Advanced messages will only show up in EIServer when the 'Show advanced messages' option is
     * selected. If all messages in a category are 'advanced' messages, the complete category is also marked as advanced.
     *
     * @return True if this message is an advanced message, false if not
     */
    boolean advanced() default false;

    /**
     * This is a human readable description of the message, used to show the message in EIServer
     *
     * @return The description
     */
    String description();

    /**
     * This is the tag name of the message. This tagname can be used to link a given message to an {@link RtuMessageHandler}.
     * The tag name is an internal value, and is not directly shown to the user.
     *
     * @return The tag name
     */
    String tag();

    /**
     * With this field, you can prevent a message to be shown in EIServer together with the other messages. However, setting this field to
     * false does not disable the message handling. It only hides the message from the user in EIServer.
     *
     * @return True if visible, false if hidden
     */
    boolean visible() default true;

}
