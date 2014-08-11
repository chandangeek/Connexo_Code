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
        padding: '15px'
    },
    defaults: {
        style: {
            marginTop: '30px',
            paddingTop: '30px',
            borderTop: '3px dotted grey'
        }
    },
    items: [
        {
            xtype: 'header-section',
            wTitle: 'Connection overview', //TODO: localize
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
                    wTitle: 'Connection summary', //TODO: localize
                    style: {
                        paddingRight: '150px'
                    }
                },
                {
                    xtype: 'communication-servers',
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
                    }
                }
            ]
        },
        {
            xtype: 'read-outs-over-time'
        },
        {
            xtype: 'overview'
        },
        {
            xtype: 'breakdown'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});