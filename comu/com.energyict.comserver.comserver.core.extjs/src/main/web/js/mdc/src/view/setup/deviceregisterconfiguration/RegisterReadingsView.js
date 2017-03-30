/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsView', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceRegisterReadingsView',
    device: null,
    router: null,
    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsGrid',
        'Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsTopFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this,
            registerReadingsStore = Ext.getStore('Mdc.store.RegisterReadings') || Ext.create('Mdc.store.RegisterReadings');

        registerReadingsStore.getProxy().setUrl(me.device.get('name'));

        me.items = [
            {
                xtype: 'panel',
                ui: 'large',
                dockedItems: [
                    {
                        dock: 'top',
                        xtype: 'mdc-registerReadings-overview-topfilter',
                        store: registerReadingsStore,
                        deviceId: me.device.get('name')
                    }
                ]
            },
            {
                xtype: 'panel',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceRegisterReadingsGrid',
                            itemId: 'mdc-register-readings-grid',
                            router: me.router,
                            store: registerReadingsStore,
                            mRID: encodeURIComponent(me.device.get('name')),
                            showDataLoggerSlaveColumn: !Ext.isEmpty(me.device.get('isDataLogger')) && me.device.get('isDataLogger')
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'mdc-no-register-readings-message',
                            title: Uni.I18n.translate('register.readings.empty.title', 'MDC', 'No register readings found.'),
                            reasons: [
                                Uni.I18n.translate('register.readings.empty.reason1', 'MDC', 'No register readings comply with the filter.'),
                                Uni.I18n.translate('register.readings.empty.reason2', 'MDC', 'No register readings of the selected registers have been collected.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'container',
                            itemId: 'mdc-registers-overview-previewContainer'
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }

});