Ext.define('Mdc.view.setup.devicehistory.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-history-setup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicehistory.LifeCycle'
    ],

    router: null,
    device: null,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceMenu',
                    itemId: 'device-history-side-menu',
                    device: me.device
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'MDC', 'History'),
            itemId: 'history-panel',
            items: [
                {
                    xtype: 'tabpanel',
                    margin: '20 0 0 20',
                    itemId: 'device-history-tab-panel',
                    activeTab: 0,
                    width: 140,
                    items: [
                        {
                            title: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                            itemId: 'device-history-life-cycle-tab'
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});
