package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.ws.rs.HttpMethod;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Supplier;

public class ConcurrentModificationException extends LocalizedException {
    private MessageSeed messageTitle;
    private Object[] messageTitleArgs;
    private MessageSeed messageBody;
    private Object[] messageBodyArgs;
    private Supplier<Long> version;
    private Object[] parentId;
    private Supplier<Long> parentVersion;

    ConcurrentModificationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed != null ? messageSeed : ExceptionContext.EDIT.getMessageTitle(), args);
        this.messageTitle = messageSeed;
        this.messageTitleArgs = args;
    }

    public String getMessageTitle() {
        if (messageTitle != null) {
            String string = getThesaurus().getStringBeyondComponent(messageTitle.getKey(), messageTitle.getDefaultFormat());
            return string!= null ? new MessageFormat(string).format(messageTitleArgs) : null;
        }
        return getLocalizedMessage();
    }

    public String getMessageBody() {
        if (messageBody != null) {
            String string = getThesaurus().getStringBeyondComponent(messageBody.getKey(), messageBody.getDefaultFormat());
            return string!= null ? new MessageFormat(string).format(messageBodyArgs) : null;
        }
        return null;
    }

    public Long getVersion() {
        return this.version != null ? this.version.get() : null;
    }

    public Long getParentVersion() {
        return this.parentVersion != null ? this.parentVersion.get() : null;
    }

    public Object[] getParentId() {
        return parentId;
    }

    void setMessageBody(MessageSeed messageBody) {
        this.messageBody = messageBody;
    }

    void setMessageBodyArgs(Object[] messageBodyArgs) {
        this.messageBodyArgs = messageBodyArgs;
    }

    void setVersion(Supplier<Long> version) {
        this.version = version;
    }

    void setParentId(Object[] parentId) {
        this.parentId = parentId;
    }

    void setParentVersion(Supplier<Long> parentVersion) {
        this.parentVersion = parentVersion;
    }

    ConcurrentModificationException withContext(ExceptionContext context) {
        if (this.messageTitle != null || context == null) {
            return this;
        }
        ConcurrentModificationException clone = new ConcurrentModificationException(getThesaurus(), context.getMessageTitle(), this.messageTitleArgs);
        clone.messageBody = context.getMessageBody();
        clone.messageBodyArgs = this.messageBodyArgs;
        clone.version = this.version;
        clone.parentId = this.parentId;
        clone.parentVersion = this.parentVersion;
        return clone;
    }

    enum ExceptionContext {
        DELETE(HttpMethod.DELETE) {
            @Override
            public MessageSeed getMessageTitle() {
                return MessageSeeds.CONCURRENT_DELETE_TITLE;
            }

            @Override
            public MessageSeed getMessageBody() {
                return MessageSeeds.CONCURRENT_DELETE_BODY;
            }
        },
        EDIT(HttpMethod.PUT) {
            @Override
            public MessageSeed getMessageTitle() {
                return MessageSeeds.CONCURRENT_EDIT_TITLE;
            }

            @Override
            public MessageSeed getMessageBody() {
                return MessageSeeds.CONCURRENT_EDIT_BODY;
            }
        },;
        String httpOperation;

        ExceptionContext(String httpOperation) {
            this.httpOperation = httpOperation;
        }

        public static ExceptionContext from(String method) {
            return Arrays.stream(ExceptionContext.values())
                    .filter(candidate -> candidate.httpOperation.equals(method))
                    .findAny()
                    .orElse(ExceptionContext.EDIT);
        }

        public abstract MessageSeed getMessageTitle();

        public abstract MessageSeed getMessageBody();
    }

}
