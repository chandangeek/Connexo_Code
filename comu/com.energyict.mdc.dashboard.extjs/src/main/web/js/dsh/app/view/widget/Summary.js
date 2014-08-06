Ext.define('Dsh.view.widget.Summary', {
    extend: 'Ext.container.Container',
    alias: 'widget.summary',
    itemId: 'summary',
    colspan: 2,
    style: {
        marginRight: '450px'
    },
    items: [
        {
            xtype: 'panel',
            html: 'Summary widget'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});