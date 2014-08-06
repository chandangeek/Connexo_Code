Ext.define('Dsh.view.widget.Breakdown', {
    extend: 'Ext.container.Container',
    alias: 'widget.breakdown',
    itemId: 'breakdown',
    colspan: 4,
    items: [
        {
            xtype: 'panel',
            html: 'Breakdown widget'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});