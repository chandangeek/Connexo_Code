Ext.define('Mdc.store.DevicesBuffered', {
    extend: 'Mdc.store.Devices',
    buffered: true,
    pageSize: 200,
    initComponent: function () {
        var me = this, parent;
        this.callParent(arguments);
        parent = Ext.getStore('Mdc.store.Devices');
        if (parent != null) {
            parent.on('beforeload', function () {
                me.getProxy().extraParams = parent.getProxy().extraParams;
            }, me);
        }
    }
});
