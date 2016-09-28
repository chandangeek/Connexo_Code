Ext.define('Mdc.usagepointmanagement.view.history.UsagePointHistory', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-history',

    requires: [
        'Mdc.usagepointmanagement.view.UsagePointSideMenu',
        'Mdc.usagepointmanagement.view.history.UsagePointHistoryDevices',
        'Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistory'
    ],

    router: null,
    usagePointId: null,
    usagePoint: null,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            items: [
                {
                    xtype: 'usage-point-management-side-menu',
                    itemId: 'usage-point-management-side-menu-history',
                    router: me.router,
                    usagePointId: me.usagePointId
                }
            ]
        };

        me.content = {
            xtype: 'tabpanel',
            title: Uni.I18n.translate('general.history', 'MDC', 'History'),
            ui: 'large',
            itemId: 'usage-point-history-tab-panel',
            activeTab: 'usage-point-' + me.activeTab,
            items: [
                {
                    title: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                    itemId: 'usage-point-devices',
                    padding: '25 0 25 0'
                },
                {
                    title: Uni.I18n.translate('general.metrologyComfiguration', 'MDC', 'Metrology configuration'),
                    padding: '8 16 16 0',
                    itemId: 'usage-point-metrologyconfigurationversion',
                    xtype: 'metrology-configuration-history-tab',
                    router: me.router,
                    usagePoint: me.usagePoint
                }
            ]
        };

        me.callParent(arguments);
    }
});

