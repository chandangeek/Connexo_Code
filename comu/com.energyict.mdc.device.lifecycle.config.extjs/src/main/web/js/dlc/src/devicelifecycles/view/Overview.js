Ext.define('Dlc.devicelifecycles.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycles-overview',
    requires: [
        'Dlc.main.view.SideMenu',
        'Dlc.devicelifecycles.view.PreviewForm'
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
                        title: Uni.I18n.translate('general.overview', 'DLC', 'Overview'),
                        ui: 'large',
                        flex: 1,
                        items: [
                            {
                                xtype: 'device-life-cycles-preview-form',
                                itemId: 'device-life-cycles-preview-form',
                                isOverview: true
                            }
                        ]
                    },
                    {
                        xtype: 'button',
                        //hidden: Uni.Auth.hasNoPrivilege('privilege.configure.deviceLifeCycle'),
                        privileges: Dlc.privileges.DeviceLifeCycle.configure,
                        itemId: 'device-life-cycles-action-menu-btn',
                        text: Uni.I18n.translate('general.actions', 'DLC', 'Actions'),
                        iconCls: 'x-uni-action-iconD',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'device-life-cycles-action-menu',
                            itemId: 'device-life-cycles-action-menu'
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
                        xtype: 'device-life-cycles-side-menu',
                        itemId: 'device-life-cycle-overview-side-menu',
                        router: me.router
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});


