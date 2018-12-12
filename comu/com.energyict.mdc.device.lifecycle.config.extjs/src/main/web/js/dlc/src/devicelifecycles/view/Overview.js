/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                        title: Uni.I18n.translate('general.details', 'DLC', 'Details'),
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
                        xtype: 'uni-button-action',
                        privileges: Dlc.privileges.DeviceLifeCycle.configure,
                        dynamicPrivilege: Dlc.dynamicprivileges.DeviceLifeCycle.viable,
                        itemId: 'device-life-cycles-action-menu-btn',
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


