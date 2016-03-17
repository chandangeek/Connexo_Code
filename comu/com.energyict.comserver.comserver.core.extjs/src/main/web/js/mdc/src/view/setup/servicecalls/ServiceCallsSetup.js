Ext.define('Mdc.view.setup.servicecalls.ServiceCallsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.service-calls-setup',
    itemId: 'serviceCallsSetup',
    device: null,
    store: null,
    activeTab: 0,

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.servicecalls.RunningServiceCallsPreviewContainer',
        'Mdc.view.setup.servicecalls.HistoryServiceCallsPreviewContainer',
        'Scs.view.ServiceCallFilter'
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
                                store: me.store
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.history', 'MDC', 'History'),
                        itemId: 'history-service-calls-tab',
                        items: [
                            {
                                xtype: 'service-call-filter',
                                modDateHidden: true
                            },
                            {
                                xtype: 'history-service-call-preview-container',
                                store: me.store
                            }
                        ]
                    }
                ]
            }
        ];

        me.doLayout();

        me.callParent(arguments);
    }
});



