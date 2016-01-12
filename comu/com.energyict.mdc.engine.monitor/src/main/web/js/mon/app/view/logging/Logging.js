Ext.define('CSMonitor.view.logging.Logging', {
    extend: 'Ext.panel.Panel',
    xtype: 'logging',
    layout: {
        type : 'vbox',
        align : 'stretch'
    },
    autoScroll: true, // show scroll bars whenever needed
    border: false,
    items: [
        {
            xtype: 'comServer'
        },
        {
            xtype: 'communication'
        },
        {
            xtype: 'criteria'
        }
    ]
});
