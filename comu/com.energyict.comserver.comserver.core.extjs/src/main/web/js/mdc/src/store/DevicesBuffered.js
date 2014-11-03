Ext.define('Mdc.store.DevicesBuffered',{
    extend: 'Mdc.store.Devices',
    buffered: true,
    //pageSize: 20,
    pageSize: 10,
    trailingBufferZone: 5,
    leadingBufferZone: 5,
    purgePageCount: 0,
    scrollToLoadBuffer: 10,
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
