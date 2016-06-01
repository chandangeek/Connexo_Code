Ext.define('Mdc.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    itemId: 'usage-point-management-setup',
    requires: [
        'Mdc.usagepointmanagement.view.MetrologyConfiguration',
        'Mdc.usagepointmanagement.view.UsagePointSideMenu',
        'Mdc.usagepointmanagement.view.UsagePointAttributesFormMain',
        'Mdc.usagepointmanagement.view.UsagePointActionMenu'
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
        panel.title = me.router.getRoute().getTitle();

        panel.tools = [
            {
                xtype: 'toolbar',
                margin: '0 20 0 0',
                items: [
                    '->',
                    {
                        xtype: 'button',
                        itemId: 'usage-point-landing-actions-btn',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        privileges: Mdc.privileges.UsagePoint.admin,
                        style: {
                            'background-color': '#71adc7'
                        },
                        iconCls: 'x-uni-action-iconD',
                        menu: {
                            xtype: 'usage-point-action-menu',
                            itemId: 'usagePointActionMenu',
                            router: me.router,
                            mRID: me.mRID
                        }
                    }
                ]
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        mRID: me.mRID
                    }
                ]
            }
        ];

        panel.items = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'usagePointSetupPanel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
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
                        router: me.router,
                        flex: 7
                    },
                    {
                        xtype: 'usagePointAttributesFormMain',
                        itemId: 'usage-point-attributes-panel',
                        router: me.router,
                        flex: 4
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});