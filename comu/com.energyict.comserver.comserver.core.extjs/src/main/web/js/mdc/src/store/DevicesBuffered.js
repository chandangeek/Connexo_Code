Ext.define('Mdc.store.DevicesBuffered', {
    extend: 'Mdc.store.Devices',
    buffered: true,
    pageSize: 200,
    constructor: function () {
        var parent;
        this.callParent(arguments);
        parent = Ext.getStore('Mdc.store.Devices');
        if (parent != null) {
            parent.on('beforeload', function () {
                this.getProxy().extraParams = parent.getProxy().extraParams;
            }, this);
        }
    }
});
