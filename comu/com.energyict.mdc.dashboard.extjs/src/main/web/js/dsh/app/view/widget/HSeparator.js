Ext.define('Dsh.view.widget.HSeparator', {
    extend: 'Ext.container.Container',
    alias: 'widget.h-sep',
    colspan: 4,
    style: {
        clear: 'both',
        borderBottom: '3px dotted #999'
    },
    initComponent: function () {
        this.callParent(arguments);
    }
});