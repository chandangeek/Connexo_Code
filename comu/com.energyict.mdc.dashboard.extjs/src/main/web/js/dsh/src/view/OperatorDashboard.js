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
                        items: [
                            '->',
                            {
                                xtype: 'component',
                                itemId: 'last-updated-field',
                                width: 150,
                                style: {
                                    'font': 'normal 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'button',
                                itemId: 'refresh-btn',
                                style: {
                                    'background-color': '#71adc7'
                                },
                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                                icon: '/apps/sky/build/resources/images/form/restore.png'
                            }
                        ]
                    }
                ],
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
                    type: 'vbox',
                    align: 'stretch'
                },
                height: 500,
                defaults: {
                    flex: 1,
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    }
                },
                style: {'margin-right': '20px'},
                items: [],
                dockedItems: [{
                    xtype: 'communication-servers',
                    width: 300,
                    dock: 'right',
                    privileges: Mdc.privileges.Device.administrateOrOperateDeviceCommunication,
                    itemId: 'communication-servers',
                    router: me.router,
                    style: 'border-width: 1px !important'   // Andrea: Should be fixed with CSS
                }]
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
        //if(Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceGroup','privilege.administrate.deviceOfEnumeratedGroup','privilege.view.deviceGroupDetail'])) {
            me.items[0].items.push(
                {
                    xtype: 'favorite-device-groups',
                    itemId: 'favorite-device-groups'
                });
        //}
        if (Mdc.privileges.Device.canAdministrateOrOperateDeviceCommunication()) {
            me.items[2].items.push(
                {
                    xtype: 'summary',
                    itemId: 'connection-summary',
                    wTitle: Uni.I18n.translate('dashboard.widget.connections.title', 'DSH', 'Active connections'),
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
                    wTitle: Uni.I18n.translate('dashboard.widget.communications.title', 'DSH', 'Active communications'),
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
        this.callParent(arguments);
    }
});