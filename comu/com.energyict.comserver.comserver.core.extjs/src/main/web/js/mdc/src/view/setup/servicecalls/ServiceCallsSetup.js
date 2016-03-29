Ext.define('Mdc.view.setup.servicecalls.ServiceCallsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.service-calls-setup',
    itemId: 'serviceCallsSetup',
    device: null,
    activeTab: 0,
    filterDefault: {},

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.servicecalls.RunningServiceCallsPreviewContainer',
        'Mdc.view.setup.servicecalls.HistoryServiceCallsPreviewContainer',
        'Scs.view.ServiceCallFilter'
    ],

    stores: [
        'Mdc.store.servicecalls.ServiceCallHistory',
        'Mdc.store.servicecalls.RunningServiceCalls',
        'Scs.store.ServiceCallTypes',
        'Scs.store.States'
    ],

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'tabpanel',
                itemId: 'device-service-calls-tab-panel',
                title: Uni.I18n.translate('general.serviceCalls', 'MDC', 'Service calls'),
                ui: 'large',
                activeTab: me.activeTab,
                mRID: me.device.get('mRID'),
                items: [
                    {
                        title: Uni.I18n.translate('servicecalls.runningServiceCalls', 'MDC', 'Running service calls'),
                        itemId: 'running-service-calls-tab',
                        items: [
                            {
                                xtype: 'running-service-call-preview-container',
                                store: 'Mdc.store.servicecalls.RunningServiceCalls'
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.history', 'MDC', 'History'),
                        itemId: 'history-service-calls-tab'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    addHistoryGrid: function (filterDefault) {
        var me = this;

        me.suspendLayouts();
        me.down('#history-service-calls-tab').add(
            {
                xtype: 'service-call-filter',
                store: 'Mdc.store.servicecalls.ServiceCallHistory',
                modDateHidden: true,
                filterDefault: filterDefault
            },
            {
                xtype: 'history-service-call-preview-container',
                store: 'Mdc.store.servicecalls.ServiceCallHistory'
            });

        me.resumeLayouts(true);

        me.doLayout();
    }
});



