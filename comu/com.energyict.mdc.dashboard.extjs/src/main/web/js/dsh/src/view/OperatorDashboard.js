Ext.define('Dsh.view.OperatorDashboard', {
    extend: 'Ext.panel.Panel',
    requires: [
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
                xtype: 'toolbar',
                style: 'top: 40px !important', // Andrea: Should be fixed with CSS
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
                        icon: '/apps/sky/resources/images/form/restore.png'
                    }
                ]
            },
            {
                xtype: 'component',
                html: me.router.getRoute().title,
                // Andrea: Should be created a Component "large" ui
                style: {
                    'color': 'rgb(0, 125, 195)',
                    'font-size': '60px',
                    'font-family': 'Open Sans Condensed',
                    'width': '250px !important',
                    'top': '20px !important'
                }
            },
            {
                xtype: 'panel',
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
                        xtype: 'open-data-collection-issues',
                        itemId: 'open-data-collection-issues',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue']),
                        router: me.router
                    },
                    {
                        xtype: 'flagged-devices',
                        itemId: 'flagged-devices',
                        router: me.router
                    },
                    {
                        xtype: 'favorite-device-groups',
                        itemId: 'favorite-device-groups'
                    }
                ]
            },
            {
                xtype: 'toolbar',
                margin: '50 0 0 0',
                items: {
                    xtype: 'device-group-filter',
                    hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure', 'privilege.view.communicationInfrastructure']),
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
                items: [
                    {
                        xtype: 'summary',
                        itemId: 'connection-summary',
                        wTitle: Uni.I18n.translate('dashboard.widget.connections.title', 'DSH', 'Active connections'),
                        router: me.router,
                        parent: 'connections',
                        buttonAlign: 'left',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure']),
                        buttons: [{
                            text: Uni.I18n.translate('dashboard.widget.connections.link', 'DSH', 'View connections overview'),
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
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure']),
                        buttons: [{
                            text: Uni.I18n.translate('dashboard.widget.communications.link', 'DSH', 'View communications overview'),
                            ui: 'link',
                            href: typeof me.router.getRoute('workspace/communications') !== 'undefined'
                                ? me.router.getRoute('workspace/communications').buildUrl(null, me.router.queryParams) : ''
                        }]
                    }
                ],
                dockedItems: [{
                    xtype: 'communication-servers',
                    width: 300,
                    dock: 'right',
                    hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure']),
                    itemId: 'communication-servers',
                    router: me.router,
                    style: 'border-width: 1px !important'   // Andrea: Should be fixed with CSS
                }]
            }
        ];

        this.callParent(arguments);
    }
});