Ext.define('Dsh.view.ConnectionOverview', {
    extend: 'Ext.container.Container',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown'
    ],
    alias: 'widget.connection-overview',
    itemId: 'connection-overview',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '20px'
    },
    defaults: {
        style: {
            marginBottom: '20px',
            padding: 0
        }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                wTitle: Uni.I18n.translate('connection.widget.headerSection.title', 'DSH', 'Connection overview'),
                style: 'none'
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
                        xtype: 'summary',
                        flex: 2,
                        wTitle: Uni.I18n.translate('connection.widget.summary.title', 'DSH', 'Connection summary'),
                        router: me.router,
                        parent: 'connections'
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router
                    },
                    {
                        xtype: 'quick-links',
                        itemId: 'quick-links',
                        style: {
                            marginRight: '0',
                            padding: '20px'
                        },
                        data: [
                            {
                                link: Uni.I18n.translate('connection.widget.quicklinks.viewAll', 'DSH', 'View all connections'),
                                href: me.router.getRoute('workspace/datacommunication/connections').buildUrl()
                            },
                            {
                                link: Uni.I18n.translate('communication.widget.headerSection.title', 'DSH', 'Communication overview'),
                                href: me.router.getRoute('workspace/datacommunication/communication').buildUrl()
                            }
                        ]
                    }
                ]
            },
//            {
//                xtype: 'read-outs-over-time'
//            },
            {
                xtype: 'overview',
                category: 'Connection',
                parent: 'connections',
                router: me.router
            },
            {
                xtype: 'breakdown',
                parent: 'connections',
                router: me.router
            }
        ];
        this.callParent(arguments);
    }
});