/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.AddLogbookConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookConfigurations'
    ],

    views: [
        'setup.deviceconfiguration.AddLogbookConfigurations'
    ],

    init: function () {
        this.control({
            'add-logbook-configurations grid': {
                selectionchange: this.countSelectedLogbooks
            },
            'add-logbook-configurations button[action=add]': {
                click: this.addLogbookType
            }
        });
    },

    countSelectedLogbooks: function (grid) {
        var textLabel = Ext.ComponentQuery.query('add-logbook-configurations #logbook-count')[0];
        textLabel.setText(
            Uni.I18n.translatePlural('general.nrOfLogbookTypes.selected', grid.view.getSelectionModel().getSelection().length, 'MDC',
                'No logbook types selected', '{0} logbook type selected', '{0} logbook types selected')
        );
    },

    addLogbookType: function (btn) {
        var me = this,
            addView = Ext.ComponentQuery.query('add-logbook-configurations')[0],
            grid = addView.down('grid'),
            records = grid.getSelectionModel().getSelection(),
            url = '/api/dtc/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/logbookconfigurations',
            ids = [],
            router = me.getController('Uni.controller.history.Router');

        Ext.Array.each(records, function (item) {
            ids.push(item.internalId);
        });
        var jsonIds = Ext.encode(ids);

        addView.setLoading(Uni.I18n.translate('general.loading', 'MDC', 'Loading...'));
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: jsonIds,
            success: function () {
                router.getRoute('administration/devicetypes/view/deviceconfigurations/view/logbookconfigurations').forward();
                me.getApplication().fireEvent('acknowledge', 'Logbook configurations added');
            },
            callback: function () {
                addView.setLoading(false);
            }
        });
    }
});




