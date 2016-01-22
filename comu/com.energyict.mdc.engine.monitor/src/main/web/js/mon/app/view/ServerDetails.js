Ext.define('CSMonitor.view.ServerDetails', {
    extend: 'Ext.panel.Panel',
    requires: ['Ext.layout.container.Column'],
    xtype: 'serverDetails',
    layout: {
        type: 'vbox',       // Arrange child items vertically
        align: 'stretch'    // Each takes up full width
    },
    border: false,
    defaults: {
        cls: ['server-details']
    },
    items: [
        {
            xtype: 'component',
            itemId: 'serverDetailsTitle',
            html: 'Communication server'
        },
        {
            xtype: 'container',
            layout: {
                type: 'column',
                align: 'left'
            },
            align: 'none',
            defaults: {
                columnWidth: 0.5,
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                }
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'serverId',
                            html: 'Communication server ID:'
                        },
                        {
                            xtype: 'component',
                            itemId: 'serverName',
                            html: 'Server name:'
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'started',
                            html: 'Started:'
                        },
                        {
                            xtype: 'component',
                            itemId: 'currentTime',
                            html: 'current'
                        }
                    ]
                }
            ]
        }
    ],

    setServerDetails: function(serverDetails) {
        var comServerText = "Communication server",
            comServerNameText = "Communication server name",
            comServerIDText = "Communication server ID",
            localRemoteText = "Local/Remote",
            startedText = "Started",
            currentTimeText = "Date of this information",

            serverId = serverDetails.get('serverId'),
            serverName = serverDetails.get('serverName'),
            startedInfo = serverDetails.get('started'),
            duration = serverDetails.get('duration'),
            currentInfo = serverDetails.get('currentDate');

        this.down('#serverDetailsTitle').update('<h1>' + comServerText + ' ' + serverName + '</h1>');
        this.down('#serverName').update(comServerNameText + ': <b>' + serverName + '</b>');
        this.down('#serverId').update(comServerIDText + ': <b>' + serverId + '</b>');
        this.down('#started').update(startedText + ': <b>' + startedInfo + '</b>');
        this.down('#currentTime').update(currentTimeText + ': <b>' +  currentInfo + '</b>');
    },

    setUnselectable: function() {
        this.getEl().unselectable();
    }
});
