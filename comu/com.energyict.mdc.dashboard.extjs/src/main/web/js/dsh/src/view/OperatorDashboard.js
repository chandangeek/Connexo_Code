Ext.define('Dsh.view.OperatorDashboard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mdc.privileges.Device',
        'Mdc.privileges.DeviceGroup',
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.OpenDataCollectionIssues',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown',
        'Dsh.view.widget.DeviceGroupFilter',
        'Dsh.view.widget.FavoriteDeviceGroups',
        'Dsh.view.widget.FlaggedDevices',
        'Dsh.view.MyFavoriteDeviceGroups'
    ],
    alias: 'widget.operator-dashboard',
    itemId: 'operator-dashboard',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        'padding-left': '20px'
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                ui: 'large',
                title:  me.router.getRoute().title,
                tools: [
                    {
                        xtype: 'toolbar',
                        style: {
                            marginRight: '20px'
                        },
                        items: [
                            '->',
                            {
                                xtype: 'component',
                                itemId: 'last-updated-field',
                                margins: '0 15 0 0'
                            },
                            {
                                xtype: 'button',
                                itemId: 'refresh-btn',
                                text: Uni.I18n.translate('general.refresh', 'DSH', 'Refresh'),
                                iconCls: 'icon-spinner11'
                            }
                        ]
                    }
                ],
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    flex: 1
                },
                items: []
            },
            {
                xtype: 'toolbar',
                margin: '50 0 0 0',
                items: {
                    xtype: 'device-group-filter',
                    privileges: Mdc.privileges.Device.administrateOrOperateDeviceCommunication,
                    router: me.router
                }
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'panel',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        height: 500,
                        defaults: {
                            flex: 1
                        },
                        items: [],
                        flex: 1
                    },
                    {
                        xtype: 'panel',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            flex: 1
                        },
                        items: [],
                        width: 350
                    }
                ]
            }
        ];

        if(Isu.privileges.Issue.canViewAdminDevice()) {
            me.items[0].items.push(
                {
                    xtype: 'open-data-collection-issues',
                    itemId: 'open-data-collection-issues',
                    router: me.router
                });
        }
        if(Mdc.privileges.Device.canAdministrateDeviceData() ||
            Mdc.privileges.Device.canView() ||
            Mdc.privileges.Device.canAdministrateOrOperateDeviceCommunication()) {
            me.items[0].items.push(
                {
                    xtype: 'flagged-devices',
                    itemId: 'flagged-devices',
                    router: me.router
                });
        }
        if(Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceGroup','privilege.administrate.deviceOfEnumeratedGroup','privilege.view.deviceGroupDetail','privilege.view.device'])) {
            me.items[0].items.push(
                {
                    xtype: 'favorite-device-groups',
                    itemId: 'favorite-device-groups'
                });
        }
        if (Mdc.privileges.Device.canAdministrateOrOperateDeviceCommunication()) {
            me.items[2].items[0].items.push(
                {
                    xtype: 'summary',
                    itemId: 'connection-summary',
                    router: me.router,
                    parent: 'connections',
                    buttonAlign: 'left',
                    buttons: [{
                        text: Uni.I18n.translate('dashboard.widget.connections.link', 'DSH', 'View connections overview'),
                        itemId: 'lnk-connections-overview',
                        ui: 'link',
                        href: typeof me.router.getRoute('workspace/connections') !== 'undefined'
                            ? me.router.getRoute('workspace/connections').buildUrl(null, me.router.queryParams) : ''
                    }]
                },
                {
                    xtype: 'summary',
                    itemId: 'communication-summary',
                    parent: 'communications',
                    router: me.router,
                    buttonAlign: 'left',
                    buttons: [{
                        text: Uni.I18n.translate('dashboard.widget.communications.link', 'DSH', 'View communications overview'),
                        itemId: 'lnk-communications-overview',
                        ui: 'link',
                        href: typeof me.router.getRoute('workspace/communications') !== 'undefined'
                            ? me.router.getRoute('workspace/communications').buildUrl(null, me.router.queryParams) : ''
                    }]
                });
        }

        me.items[2].items[1].items.push(
            {
                xtype: 'communication-servers',
                privileges: Mdc.privileges.Device.administrateOrOperateDeviceCommunication,
                itemId: 'communication-servers',
                router: me.router
            }
        );
        this.callParent(arguments);
    }
});