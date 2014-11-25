Ext.define('Dsh.view.OperatorDashboard', {
    extend: 'Ext.container.Container',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.OpenDataCollectionIssues',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown',
        'Dsh.view.widget.DeviceGroupFilter'
    ],
    alias: 'widget.operator-dashboard',
    itemId: 'operator-dashboard',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '0px 20px'
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        flex: 1,
                        items: [
                            {
                                xtype: 'panel',
                                ui: 'large',
                                title: me.router.getRoute().title
                            }
                        ]
                    },
                    {
                        xtype: 'toolbar',
                        itemId: 'header-section',
                        items: [
                            {
                                xtype: 'displayfield',
                                itemId: 'last-updated-field',
                                style: 'margin-right: 10px'
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
                    }
                ]
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
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue']),
                        router: me.router
                    }
                ]
            },
            {
                xtype: 'toolbar',
                margin: '50 0 0 0',
                items: {
                    xtype: 'device-group-filter',
                    hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure']),
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
                        flex: 3,
                        xtype: 'panel',
                        layout: {
                            type: 'vbox',
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
                                    href: me.router.getRoute('workspace/connections').buildUrl(null, me.router.queryParams)
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
                                    href: me.router.getRoute('workspace/communications').buildUrl(null, me.router.queryParams)
                                }]
                            }
                        ]
                    },
                    {
                        flex: 1,
                        xtype: 'communication-servers',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure']),
                        itemId: 'communication-servers',
                        router: me.router
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});