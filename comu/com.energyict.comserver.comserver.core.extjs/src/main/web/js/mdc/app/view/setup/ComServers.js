Ext.define('Mdc.view.setup.ComServers', {
    extend: 'Ext.container.Container',
    alias: 'widget.setupComServers',
    overflowY: 'auto',
    layout: 'fit',
    items: [
        {
            xtype: 'component',
            cls: 'content-container',
            html: 'comservers'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});