Ext.define('Dsh.view.widget.Overview', {
    extend: 'Ext.container.Container',
    alias: 'widget.overview',
    itemId: 'overview',
    colspan: 4,
    items: [
        {
            xtype: 'panel',
            html: 'Overview widget'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});