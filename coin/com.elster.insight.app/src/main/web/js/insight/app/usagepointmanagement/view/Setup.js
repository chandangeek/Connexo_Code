Ext.define('InsightApp.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    itemId: 'usage-point-management-setup',
    requires: [
        'InsightApp.usagepointmanagement.view.MetrologyConfiguration',
        'InsightApp.usagepointmanagement.view.UsagePointSideMenu',
        'InsightApp.usagepointmanagement.view.UsagePointAttributesFormMain'
    ],
    router: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'usagePointSetupPanel',
            layout: {
                type: 'fit',
                align: 'stretch'
            }
        }
    ],

    initComponent: function () {
        var me = this,
            panel = me.content[0];
//        panel.title = me.router.getRoute().getTitle();
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
 //                       mRID: me.mRID
                    }
                ]
            }
        ];
        this.callParent(arguments);

        me.down('#usagePointSetupPanel').add(
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                        xtype: 'metrology-configuration',
                        router: me.router
                    },
                    {
                        xtype: 'panel',
                        title: Uni.I18n.translate('usagePointManagement.metrologyConfiguration', 'MDC', 'Usage Point Attributes'),
                        ui: 'tile',
                        itemId: 'usage-point-attributes-panel',
                        router: me.router
                    }
                ]
            }
        );
    }
});