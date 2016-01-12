Ext.define('CSMonitor.view.status.Status', {
    extend: 'Ext.panel.Panel',
    xtype: 'status',
    layout: {
        type : 'vbox',
        align : 'stretch'
    },
    autoScroll: true, // show scroll bars whenever needed
    border: false,
    items: [
        {
            xtype: 'generalInformation'
        },
        {
            xtype: 'runningInformation'
        },
        {
            itemId: 'remoteServersPnl',
            xtype: 'connectedServers'
        },
        {
            xtype: 'ports'
        },
        {
            xtype: 'pools'
        }
    ],

    setVisibilityOfRemoteServers: function(visible) {
        this.down('#remoteServersPnl').setVisible(visible);
    }
});
