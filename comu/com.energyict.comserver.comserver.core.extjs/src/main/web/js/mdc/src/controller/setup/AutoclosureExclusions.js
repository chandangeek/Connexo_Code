/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.AutoclosureExclusions', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.autoclosureexclusions.AutoclosureExclusionsSetup'
    ],

    models: [
        'Mdc.model.Device',
		'Mdc.model.AutoclosureExclusion',
		'Mdc.model.AutoclosureExclusionIssueType'
    ],

    stores: [
		'Mdc.store.AutoclosureExclusions'
    ],

    requires: [
        'Uni.controller.Navigation',
        'Uni.util.History'
    ],

    refs: [
    ],

    init: function () {
        this.control({
			
		});
    },

    showAutoclosureExclusions: function (deviceId) {
		
		var me = this;
            viewport = Ext.ComponentQuery.query('viewport')[0],
            exclusionsStore = me.getStore('Mdc.store.AutoclosureExclusions');
        exclusionsStore.getProxy().setUrl(deviceId);
        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.widget('autoclosure-exclusions-setup', {device: device});
				me.getApplication().fireEvent('loadDevice', device);
				me.getApplication().fireEvent('changecontentevent', widget);
				viewport.setLoading(false);
            }
        });
		
    }
});
