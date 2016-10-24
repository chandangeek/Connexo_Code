Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsView', {
    extend: 'Uni.view.container.ContentContainer',
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

        registerReadingsStore.getProxy().setUrl(me.device.get('mRID'));

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                dockedItems: [
                    {
                        dock: 'top',
                        xtype: 'mdc-registerReadings-overview-topfilter',
                        store: registerReadingsStore,
                        deviceMRID: me.device.get('mRID')
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
                            store: registerReadingsStore,
                            mRID: me.device.get('mRID'),
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