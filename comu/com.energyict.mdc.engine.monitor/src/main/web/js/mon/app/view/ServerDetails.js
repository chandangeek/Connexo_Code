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
        cls: ['server-details'],
    },
    items: [
        {
            xtype: 'component',
            itemId: 'serverDetailsTitle',
            html: '<h1>Communication server</h1>'
        },
        {
            xtype: 'form',
            defaultType: 'textfield',
            baseCls: 'server-details',
            items: [{
                fieldLabel: 'Started',
                name: 'started',
                labelWidth: 150,
                width: 500,
                labelCls: 'server-details',
                emptyCls: 'server-details-field',
                fieldCls: 'server-details-field'
            },
            {
                fieldLabel: 'Date of this information',
                name: 'currentDate',
                labelWidth: 150,
                width: 500,
                labelCls: 'server-details',
                emptyCls: 'server-details-field',
                fieldCls: 'server-details-field'
            }]
        }
    ],

    setServerDetails: function(serverDetails) {

        var comServerText = "Communication server",
            serverName = serverDetails.get('serverName');
        //    startedText = "Started",
        //    currentTimeText = "Date of this information",
        //    serverName = serverDetails.get('serverName'),
        //    startedInfo = serverDetails.get('started'),
        //    duration = serverDetails.get('duration'),
        //    currentInfo = serverDetails.get('currentDate');
        //
        this.down('#serverDetailsTitle').update('<h1>' + comServerText + ' ' + serverName + '</h1>');
        this.down('form').loadRecord(serverDetails);
        //this.down('#started').update(startedText + ': <b>' + startedInfo + '</b>');
        //this.down('#currentTime').update(currentTimeText + ': <b>' +  currentInfo + '</b>');
    },

    setUnselectable: function() {
        this.getEl().unselectable();
    }
});
