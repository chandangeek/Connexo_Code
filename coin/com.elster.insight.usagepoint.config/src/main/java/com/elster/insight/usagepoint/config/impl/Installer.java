package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;

public class Installer {

	private final DataModel dataModel;

	Installer(DataModel dataModel,EventService eventService) {
        super();
		this.dataModel = dataModel;
	}

	public void install(boolean executeDdl, boolean updateOrm) {
        try {
            dataModel.install(executeDdl, updateOrm);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}