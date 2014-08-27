Ext.define('Dsh.view.CommunicationOverview', {
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
    alias: 'widget.communication-overview',
    itemId: 'communication-overview',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '15px'
    },
    defaults: {
        style: {
            marginTop: '30px',
            paddingTop: '30px',
            borderTop: '3px dotted grey'
        }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                wTitle: Uni.I18n.translate('communication.widget.headerSection.title', 'DSH', 'Communication overview'),
                style: 'none'
            },
            {
                layout: 'hbox',
                style: {
                    marginTop: '30px',
                    border: 'none'
                },
                defaults: {
                    style: {
                        paddingRight: '50px'
                    }
                },
                items: [
                    {
                        xtype: 'summary',
                        flex: 3,
                        wTitle: Uni.I18n.translate('communication.widget.summary.title', 'DSH', 'Communication summary'),
                        style: {
                            paddingRight: '150px'
                        }
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router,
                        flex: 1,
                        style: {
                            borderRight: '3px dotted grey'
                        }
                    },
                    {
                        xtype: 'quick-links',
                        flex: 1,
                        style: {
                            paddingLeft: '50px'
                        },
                        data: [ //TODO: set real data
                            {
                                link: Uni.I18n.translate('communication.widget.quicklinks.viewAll', 'DSH', 'View all communications'),
                                href: me.router.getRoute('workspace/datacommunication/communications').buildUrl()
                            },
                            {
                                link: Uni.I18n.translate('connection.widget.headerSection.title', 'DSH', 'Connection overview'),
                                href: me.router.getRoute('workspace/datacommunication/connection').buildUrl()
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
                category: 'Communication'
            },
            {
                xtype: 'breakdown',
                parent: 'communications',
                router: me.router
            }
        ];
        this.callParent(arguments);
    }
});