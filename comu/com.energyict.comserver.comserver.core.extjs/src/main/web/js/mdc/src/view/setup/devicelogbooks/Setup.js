Ext.define('Mdc.view.setup.devicelogbooks.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbooksSetup',
    itemId: 'deviceLogbooksSetup',

    router: null,
    toggleId: null,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicelogbooks.Grid',
        'Mdc.view.setup.devicelogbooks.Preview'
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
                        device: me.device,
                        toggleId: me.toggleId
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.logbooks', 'MDC', 'Logbooks'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceLogbooksGrid',
                        router: me.router,
                        itemId: 'deviceLogbooksGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('devicelogbooks.empty.title', 'MDC', 'No logbooks found'),
                        reasons: [
                            Uni.I18n.translate('devicelogbooks.empty.list.item1', 'MDC', 'No logbooks have been defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'deviceLogbooksPreview',
                        device: me.device
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});