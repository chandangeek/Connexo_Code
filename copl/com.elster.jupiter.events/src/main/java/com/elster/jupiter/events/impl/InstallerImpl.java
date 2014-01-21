package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.MessageSeeds;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.elster.jupiter.events.EventService.JUPITER_EVENTS;

public class InstallerImpl implements InstallService {

    private static final int RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final MessageService messageService;
    private final Thesaurus thesaurus;

    public InstallerImpl(DataModel dataModel, MessageService messageService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void install() {
        try {
            dataModel.install(true, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get().createDestinationSpec(JUPITER_EVENTS, RETRY_DELAY);
        createTranslations();
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(EventService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(final SimpleNlsKey nlsKey, final Locale locale, final String translation) {
        return new Translation() {
            @Override
            public NlsKey getNlsKey() {
                return nlsKey;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getTranslation() {
                return translation;
            }
        };
    }

}
