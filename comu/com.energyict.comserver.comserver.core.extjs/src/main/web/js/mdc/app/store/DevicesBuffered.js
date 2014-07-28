Ext.define('Mdc.store.DevicesBuffered',{
    extend: 'Mdc.store.Devices',
    buffered: true,
    pageSize: 12,
    constructor: function () {
        var parent;
        this.callParent(arguments);
        parent = Ext.getStore('Mdc.store.Devices');
        parent.on('beforeload', function () {
            this.getProxy().extraParams = parent.getProxy().extraParams;
        }, this);
    }
});
