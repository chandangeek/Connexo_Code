/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.LoadProfileTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-loadprofiles-topfilter',
    store: 'Mdc.store.LoadProfilesOfDeviceData',
    filterDefault: {},

    initComponent: function() {
        var me = this;

        this.filters = [
            {
                type: 'duration',
                dataIndex: 'interval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                defaultFromDate: me.filterDefault.fromDate,
                defaultDuration: me.filterDefault.duration,
                text: Uni.I18n.translate('communications.widget.topfilter.startedDate', 'MDC', 'Start date'),
                durationStore: me.filterDefault.durationStore,
                loadStore: false,
                itemId: 'loadprofiles-topfilter-duration'
            },
            {
                type: 'checkbox',
                dataIndex: 'suspect',
                layout: 'hbox',
                defaults: {margin: '0 10 0 0'},
                emptyText: Uni.I18n.translate('communications.widget.topfilter.validationResult', 'MDC', 'Validation result'),
                options: [
                    {
                        display: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
                        value: 'suspect',
                        itemId: 'loadprofiles-topfilter-suspect'
                    },
                    {
                        display: Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect'),
                        value: 'nonSuspect',
                        itemId: 'loadprofiles-topfilter-notsuspect'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});