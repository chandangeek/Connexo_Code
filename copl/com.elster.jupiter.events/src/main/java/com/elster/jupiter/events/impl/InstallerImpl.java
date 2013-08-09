package com.elster.jupiter.events.impl;

import com.elster.jupiter.orm.callback.InstallService;

import static com.elster.jupiter.events.EventService.JUPITER_EVENTS;

public class InstallerImpl implements InstallService {

    private static final int RETRY_DELAY = 60;

    @Override
    public void install() {
        try {
            Bus.getOrmClient().install();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bus.getMessageService().getQueueTableSpec("MSG_RAWTOPICTABLE").get().createDestinationSpec(JUPITER_EVENTS, RETRY_DELAY);
    }
}
