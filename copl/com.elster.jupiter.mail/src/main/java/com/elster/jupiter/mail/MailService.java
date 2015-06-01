package com.elster.jupiter.mail;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MailService {

    MailMessageBuilder messageBuilder(List<MailAddress> recipients);

    default MailMessageBuilder messageBuilder(MailAddress first, MailAddress... other) {
        List<MailAddress> recipients = Stream.of(Stream.of(first), Arrays.stream(other))
                .flatMap(Function.<Stream<MailAddress>>identity())
                .collect(Collectors.toList());
        return messageBuilder(recipients);
    }

    MailAddress mailAddress(String mailAddress);
}
