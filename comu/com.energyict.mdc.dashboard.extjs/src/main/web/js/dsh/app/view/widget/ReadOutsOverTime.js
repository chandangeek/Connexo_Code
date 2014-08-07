Ext.define('Dsh.view.widget.ReadOutsOverTime', {
    extend: 'Ext.container.Container',
    alias: 'widget.read-outs-over-time',
    itemId: 'read-outs-over-time',
    colspan: 4,
    items: [
        {
            xtype: 'panel',
            html: 'Read-outs over time widget'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});