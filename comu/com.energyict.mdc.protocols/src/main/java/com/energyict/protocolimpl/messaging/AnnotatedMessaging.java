package com.energyict.protocolimpl.messaging;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.messaging.proxy.ProxyMessageInvocationHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 6/6/12
 * Time: 11:57 PM
 */
public abstract class AnnotatedMessaging implements MessageProtocol {

    private final Class<? extends AnnotatedMessage>[] messages;
    private final Logger logger;

    /**
     * For this method, it's required that the message class is annotated with {@link RtuMessageDescription}
     * Each {@link RtuMessageDescription} should also have an unique tag
     *
     * @param messages The messages, used to create the MessageCategorySpec's
     * @throws IllegalArgumentException if one or more messages are not annotated with {@link RtuMessageDescription}
     * @throws IllegalArgumentException if two or more messages have the same tag
     */
    protected AnnotatedMessaging(final Logger logger, final Class<? extends AnnotatedMessage>... messages) {
        this.logger = logger != null ? logger : Logger.getLogger(getClass().getName());
        for (Class<? extends AnnotatedMessage> message : messages) {
            if (!message.isAnnotationPresent(RtuMessageDescription.class) && !message.isAnnotationPresent(RtuMessageDescriptions.class)) {
                throw new IllegalArgumentException("AnnotatedMessaging can only handle AnnotatedMessages when they are annotated with an RtuMessageDescription" +
                        " but [" + message.getName() + "] has no RtuMessageDescription!");
            }
        }

        for (Class<? extends AnnotatedMessage> message : messages) {
            final List<RtuMessageDescription> descriptions = getDescriptionsForMessageClass(message);

            for (final RtuMessageDescription description : descriptions) {
                String tag = description.tag().trim();

                for (Class<? extends AnnotatedMessage> otherMessage : messages) {
                    if (!otherMessage.equals(message) && getTagsForMessageSpecification(otherMessage).contains(tag)) {
                        throw new IllegalArgumentException("Found two different messages [" + message + "] and [" + otherMessage + "] with the same tag [" + tag + "]! " +
                                "The message tag name should be unique per message.");
                    }
                }
            }
        }

        this.messages = new Class[messages.length];
        System.arraycopy(messages, 0, this.messages, 0, this.messages.length);
    }

    /**
     *
     * @return The list of supported messages
     */
    protected Class<? extends AnnotatedMessage>[] getMessages() {
        return this.messages;
    }

    /**
     * Gets all the tags that are used by the given message specification.
     *
     * @param clazz The message specification class.
     * @return The tags that are used by the given message spec.
     */
    public static Set<String> getTagsForMessageSpecification(final Class<? extends AnnotatedMessage> clazz) {
        final Set<String> tags = new HashSet<String>();

        final List<RtuMessageDescription> descriptions = getDescriptionsForMessageClass(clazz);

        for (final RtuMessageDescription description : descriptions) {
            tags.add(description.tag());
        }

        return tags;
    }

    /**
     * Returns the {@link RtuMessageDescription} that matches the given tag name.
     *
     * @param tagName The name of the tag.
     * @param clazz   The class.
     * @return The matching {@link RtuMessageDescription}, <code>null</code> if not found.
     */
    public static final RtuMessageDescription getDescriptionThatMatchesTagName(final String tagName, final Class<? extends AnnotatedMessage> clazz) {
        final List<RtuMessageDescription> descriptions = getDescriptionsForMessageClass(clazz);

        for (final RtuMessageDescription description : descriptions) {
            if (description.tag() != null && description.tag().equals(tagName)) {
                return description;
            }
        }

        return null;
    }

    /**
     * Returns all the message descriptions that are associated with the given message specification.
     *
     * @param clazz The message specification class.
     * @return All the {@link RtuMessageDescription}s attached to the given spec.
     */
    public static final List<RtuMessageDescription> getDescriptionsForMessageClass(final Class<? extends AnnotatedMessage> clazz) {
        final List<RtuMessageDescription> descriptions = new ArrayList<RtuMessageDescription>();

        if (clazz.isAnnotationPresent(RtuMessageDescription.class)) {
            descriptions.add(clazz.getAnnotation(RtuMessageDescription.class));
        } else if (clazz.isAnnotationPresent(RtuMessageDescriptions.class)) {
            final RtuMessageDescriptions descriptionsAnnotation = clazz.getAnnotation(RtuMessageDescriptions.class);

            for (final RtuMessageDescription description : descriptionsAnnotation.value()) {
                descriptions.add(description);
            }
        }

        return descriptions;
    }

    /**
     * Get the logger used by this messaging class. This logger is always available and will never be 'null'
     *
     * @return The logger of this class
     */
    protected final Logger getLogger() {
        return this.logger;
    }

    /**
     * Create a list of all the MessageCategorySpec's available in the given list of messages
     *
     * @return The list of MessageAttributeSpec
     */
    public List<MessageCategorySpec> getMessageCategories() {
        final List<MessageCategorySpec> specs = new ArrayList<MessageCategorySpec>();
        final List<String> messageCategories = getCategories();
        for (String category : messageCategories) {
            specs.add(createMessageCategorySpec(category));
        }
        return specs;
    }

    /**
     * Create a new {@link com.energyict.mdc.protocol.api.messaging.MessageCategorySpec} containing all messages for a given category.
     * If the category contains no messages, return an empty MessageCategorySpec
     *
     * @param category The category to use.
     * @return The new MessageCategorySpec
     */
    private final MessageCategorySpec createMessageCategorySpec(String category) {
        final MessageCategorySpec categorySpec = new MessageCategorySpec(category);
        final List<Class<? extends AnnotatedMessage>> messagesByCategory = getMessagesWithCategory(category);

        for (Class<? extends AnnotatedMessage> message : messagesByCategory) {
            final List<MessageSpec> specs = createMessageSpecsFromMessage(message);

            for (final MessageSpec spec : specs) {
                categorySpec.addMessageSpec(spec);
            }
        }

        return categorySpec;
    }

    /**
     * Create a new {@link com.energyict.mdc.protocol.api.messaging.MessageSpec} from a given class, using the annotations on the class
     * For this method, it's REQUIRED that the message class is annotated with {@link RtuMessageDescription}
     *
     * @param message The message class containing the messaging annotations
     * @return The new MessageSpec
     */
    private final List<MessageSpec> createMessageSpecsFromMessage(Class<? extends AnnotatedMessage> message) {
        final List<RtuMessageDescription> descriptions = AnnotatedMessaging.getDescriptionsForMessageClass(message);
        final List<MessageSpec> specs = new ArrayList<MessageSpec>();

        for (final RtuMessageDescription msgAnnotation : descriptions) {
            if (msgAnnotation.visible()) {
                final MessageTagSpec messageTagSpec = new MessageTagSpec(msgAnnotation.tag());
                final Method[] methods = message.getMethods();
                for (final Method method : methods) {
                    final MessageAttributeSpec attributeSpec = createMessageAttributeSpec(method);
                    if (attributeSpec != null) {
                        messageTagSpec.add(attributeSpec);
                    }
                }

                final MessageSpec messageSpec = new MessageSpec(msgAnnotation.description(), msgAnnotation.advanced());
                messageSpec.add(messageTagSpec);
                specs.add(messageSpec);
            }
        }

        return specs;
    }

    /**
     * Creates a new message attribute spec from a given method using the {@link RtuMessageAttribute} annotation
     * If the annotation is not present, this method will return 'null';
     *
     * @param method The method to create the new {@link com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec} for
     * @return The new MessageAttributeSpec
     */
    private MessageAttributeSpec createMessageAttributeSpec(Method method) {
        if (method.isAnnotationPresent(RtuMessageAttribute.class)) {
            final RtuMessageAttribute rtuMessageAttribute = method.getAnnotation(RtuMessageAttribute.class);
            return new MessageAttributeSpec(rtuMessageAttribute.tag(), rtuMessageAttribute.required());
        }
        return null;
    }

    /**
     * Look through all the message classes and return all messages with the given category
     *
     * @param category The message category to look
     * @return The list of message classes with the given category or an empty list if category was not found
     */
    private List<Class<? extends AnnotatedMessage>> getMessagesWithCategory(String category) {
        final List<Class<? extends AnnotatedMessage>> messagesByCategory = new ArrayList<>();
        for (Class<? extends AnnotatedMessage> messageDescription : this.messages) {
            final List<RtuMessageDescription> descriptions = AnnotatedMessaging.getDescriptionsForMessageClass(messageDescription);

            for (final RtuMessageDescription description : descriptions) {
                final String thisCategory = description.category();
                if (thisCategory != null && thisCategory.equals(category) && !messagesByCategory.contains(messageDescription)) {
                    messagesByCategory.add(messageDescription);
                }
            }
        }
        return messagesByCategory;
    }

    /**
     * Find an AnnotatedMessage supported in this AnnotatedMessaging class by its tag.
     * If there is no message found with this particular tag null is returned.
     *
     * @param tagName The tagName of the message to look for
     * @return The matching unique message, or null if no message was found with this tag
     * @throws IllegalArgumentException If the tagName parameter is null
     */
    private Class<? extends AnnotatedMessage> getMessageByTag(final String tagName) {
        if (tagName == null) {
            throw new IllegalArgumentException("Unable to find message by tag name if tagName if ['null']");
        }

        for (Class<? extends AnnotatedMessage> message : this.messages) {
            final Set<String> tags = getTagsForMessageSpecification(message);

            for (final String tag : tags) {
                if (tag.trim().equalsIgnoreCase(tagName.trim())) {
                    return message;
                }
            }
        }

        return null;
    }

    /**
     * Look through all the message classes, extract the categories from the {@link com.energyict.protocolimpl.messaging.RtuMessageDescription}
     * annotations and return a list of all the different unique categories. The categories are case and white space sensitive.
     *
     * @return A {@link java.util.List} containing all the unique categories as {@link String}.
     */
    private List<String> getCategories() {
        final List<String> categories = new ArrayList<>();
        for (Class<? extends AnnotatedMessage> rtuMessageDescription : this.messages) {
            final List<RtuMessageDescription> descriptions = AnnotatedMessaging.getDescriptionsForMessageClass(rtuMessageDescription);

            for (final RtuMessageDescription description : descriptions) {
                String category = description.category();
                if (category != null && description.visible()) {
                    if (!categories.contains(category)) {
                        categories.add(category);
                    }
                }
            }
        }

        return categories;
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag tag) {
        StringBuilder buf = new StringBuilder();

        // a. Opening tag
        buf.append("<");
        buf.append(tag.getName());

        // b. Attributes
        for (Iterator it = tag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.length() == 0)) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("\n\n</");
        buf.append(tag.getName());
        buf.append(">");

        return buf.toString();
    }

    public void applyMessages(List messageEntries) throws IOException {
        List<AnnotatedMessage> annotatedMessages = new ArrayList<AnnotatedMessage>(messageEntries.size());
        for (Object msgObject : messageEntries) {
            if (msgObject instanceof MessageEntry) {
                MessageEntry messageEntry = (MessageEntry) msgObject;
                try {
                    AnnotatedMessage annotatedMessage = createAnnotatedMessage(messageEntry);
                    annotatedMessages.add(annotatedMessage);
                } catch (IOException e) {
                    getLogger().severe("Unable to create annotated message from message entry with content [" + messageEntry.getContent() + "]! " + e.getMessage());
                }
            }
        }
        applyAnnotatedMessages(annotatedMessages);
    }

    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        AnnotatedMessage message;

        try {
            message = createAnnotatedMessage(messageEntry);
        } catch (IOException e) {
            final String msg = "Unable to create annotated message from message entry [" + messageEntry + "]: " + e.getMessage();
            getLogger().log(Level.SEVERE, msg, e);
            return MessageResult.createFailed(messageEntry, msg);
        }

        final String messageTag = message.getRtuMessageDescription().tag();

        final Method handlerMethod = findMatchingHandlerMethod(message);
        if (handlerMethod != null) {
            try {
                return (MessageResult) handlerMethod.invoke(this, message);
            } catch (IllegalAccessException e) {
                getLogger().log(Level.SEVERE, "Unable to use handler method [" + handlerMethod.getName() + "] for message with tag [" + messageTag + "]! " + e.getMessage(), e);
                return MessageResult.createFailed(message.getMessageEntry(), e.getMessage());
            } catch (InvocationTargetException e) {
                String exceptionDescription = e.getMessage();
                if (exceptionDescription == null) {
                    exceptionDescription = e.getTargetException().getMessage();
                }
                getLogger().log(Level.SEVERE, "Unable to use handler method [" + handlerMethod.getName() + "] for message with tag [" + messageTag + "]! " + exceptionDescription, e);
                return MessageResult.createFailed(message.getMessageEntry(), exceptionDescription);
            }
        }

        final String msg = "No matching handler found for message with tag [" + messageTag + "]!";
        getLogger().log(Level.SEVERE, msg);
        return MessageResult.createFailed(message.getMessageEntry(), msg);

    }

    private final Method findMatchingHandlerMethod(final AnnotatedMessage message) throws IOException {
        final Class<? extends AnnotatedMessage> messageClass = message.getClass();
        final List<Method> handlerMethods = getHandlerMethods(messageClass);
        if (handlerMethods.isEmpty()) {
            return null;
        }

        final String tag = message.getRtuMessageDescription().tag();
        List<Method> taggedMethods = findHandlerMethodByTagName(handlerMethods, tag);
        if (taggedMethods.isEmpty()) {
            taggedMethods = findHandlerMethodByTagName(handlerMethods, "");
        }

        if (taggedMethods.size() == 1) {
            return taggedMethods.get(0);
        }

        if (taggedMethods.size() > 1) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Found multiple matches for handlerMethods with tag ['").append(tag).append("']!: ");
            for (final Method taggedMethod : taggedMethods) {
                sb.append(" [").append(taggedMethod.getName()).append(']');
            }
            sb.append("Handler method should be defined unambiguous!");
            throw new IOException(sb.toString());
        }

        return null;
    }

    private static final List<Method> findHandlerMethodByTagName(final List<Method> methods, final String tag) {
        final List<Method> taggedMethods = new ArrayList<Method>();
        for (final Method method : methods) {
            if (method.getAnnotation(RtuMessageHandler.class).tag().equals(tag)) {
                taggedMethods.add(method);
            }
        }
        return taggedMethods;
    }

    private final List<Method> getHandlerMethods(Class<? extends AnnotatedMessage> messageClass) {
        final List<Method> handlerMethods = new ArrayList<Method>();
        final Method[] methods = getClass().getMethods();
        for (final Method method : methods) {
            if (!method.isAnnotationPresent(RtuMessageHandler.class)) {
                continue;
            }

            final Class<?> returnType = method.getReturnType();
            if (!MessageResult.class.isAssignableFrom(returnType)) {
                continue;
            }

            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                continue;
            }

            if (parameterTypes[0].isAssignableFrom(messageClass)) {
                handlerMethods.add(method);
            }

        }
        return handlerMethods;
    }

    /**
     * @param messageEntry
     * @param <M>
     * @return
     * @throws java.io.IOException
     */
    protected <M extends AnnotatedMessage> M createAnnotatedMessage(final MessageEntry messageEntry) throws IOException {
        final String content = messageEntry.getContent();

        try {

            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final Document document = docBuilder.parse(new ByteArrayInputStream(content.getBytes()));
            document.getDocumentElement().normalize();

            final Element element = document.getDocumentElement();
            final String tagName = element.getTagName();
            final Class<? extends AnnotatedMessage> messageByTag = getMessageByTag(tagName);

            final HashMap<String, String> attributes = new HashMap<String, String>();
            final NamedNodeMap attributeNodes = element.getAttributes();
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                final Node node = attributeNodes.item(i);
                attributes.put(node.getNodeName(), node.getNodeValue());
            }

            final M messageProxy = (M) Proxy.newProxyInstance(
                    messageByTag.getClassLoader(),
                    new Class[]{messageByTag},
                    new ProxyMessageInvocationHandler(attributes, messageByTag, messageEntry, tagName)
            );

            return messageProxy;

        } catch (Exception e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Unable to create annotated message from message entry with content [" + content + "]! " + e.getMessage());
        }
    }

    protected abstract void applyAnnotatedMessages(final List<? extends AnnotatedMessage> messages);

}
