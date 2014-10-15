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
                router: me.router,
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
                                href: me.router.getRoute('workspace/connections/details').buildUrl(null, me.router.queryParams)
                            },
                            {
                                link: me.router.getRoute('workspace/communications').title,
                                href: me.router.getRoute('workspace/communications').buildUrl(null, me.router.queryParams)
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
            },
            {
                xtype: 'heat-map',
                itemId: 'heatmap',
                store: 'Dsh.store.ConnectionResultsStore',
                router: me.router,
                parent: 'connections'
            }
        ];
        this.callParent(arguments);
    }
});