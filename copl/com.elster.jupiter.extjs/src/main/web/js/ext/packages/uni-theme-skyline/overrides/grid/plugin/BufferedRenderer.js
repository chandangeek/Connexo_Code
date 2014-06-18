Ext.define('Skyline.grid.plugin.BufferedRenderer', {
    override: 'Ext.grid.plugin.BufferedRenderer',
    rowHeight: 29, // comes from skyline theme

    bindStore: function(store) {
        var me = this;
        me.trailingBufferZone = Math.ceil(store.pageSize / 2);
        me.leadingBufferZone = store.pageSize;
        this.callParent(arguments);
    }
});