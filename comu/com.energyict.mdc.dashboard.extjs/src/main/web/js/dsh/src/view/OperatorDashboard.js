Ext.define('Dsh.view.OperatorDashboard', {
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
    alias: 'widget.operator-dashboard',
    itemId: 'operator-dashboard',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '0 20px'
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
                        itemId: 'connection-summary',
                        flex: 2,
                        wTitle: Uni.I18n.translate('connection.widget.summary.title', 'DSH', 'Connections summary'),
                        router: me.router,
                        parent: 'connections'
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router
                    }
                ]
            },
            {
                xtype: 'summary',
                itemId: 'communication-summary',
                wTitle: Uni.I18n.translate('communication.widget.summary.title', 'DSH', 'Communications summary'),
                parent: 'communications',
                router: me.router,
                style: {
                    marginRight: '20px',
                    padding: '20px'
                },
            }
        ];

        this.callParent(arguments);
    }
});