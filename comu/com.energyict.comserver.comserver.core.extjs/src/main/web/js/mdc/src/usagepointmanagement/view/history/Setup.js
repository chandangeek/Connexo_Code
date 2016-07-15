Ext.define('Mdc.usagepointmanagement.view.history.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-history-setup',
    router: null,
    usagePoint: null,
    requires: [
        'Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistory'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'MDC', 'History'),
            itemId: 'history-panel',
            items: [
                {
                    xtype: 'tabpanel',
                    margin: '20 0 0 0',
                    itemId: 'usage-point-history-tab-panel',
                    activeTab: 0,
                    width: '100%',
                    items: [
                        {
                            title: Uni.I18n.translate('general.metrologyComfiguration', 'MDC', 'Metrology configuration'),
                            padding: '8 16 16 0',
                            xtype: 'metrology-configuration-history-tab',
                            router: me.router,
                            usagePoint: me.usagePoint
                        }
                    ]
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        mRID: me.usagePoint.get('mRID')
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }    
});