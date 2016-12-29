Ext.define('Imt.usagepointlifecycle.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycles-overview',
    xtype: 'usagepoint-life-cycles-overview',
    requires: [
        'Imt.usagepointlifecycle.view.SideMenu',
        'Imt.usagepointlifecycle.view.PreviewForm'
    ],
    router: null,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        title: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                        ui: 'large',
                        flex: 1,
                        items: [
                            {
                                xtype: 'usagepoint-life-cycles-preview-form',
                                itemId: 'usagepoint-life-cycles-preview-form',
                                isOverview: true
                            }
                        ]
                    },
                    {
                        xtype: 'uni-button-action',
                        privileges: Imt.privileges.UsagePointLifeCycle.configure,                        
                        itemId: 'usagepoint-life-cycles-action-menu-btn',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'usagepoint-life-cycles-action-menu',
                            itemId: 'usagepoint-life-cycles-action-menu'
                        }
                    }
                ]
            }
        ];
        me.side = [
            {
                ui: 'medium',
                items: [
                    {
                        xtype: 'usagepoint-life-cycles-side-menu',
                        itemId: 'usagepoint-life-cycle-overview-side-menu',
                        router: me.router
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});


