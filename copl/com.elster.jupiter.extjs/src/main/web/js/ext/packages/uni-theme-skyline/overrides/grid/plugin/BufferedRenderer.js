Ext.define('Skyline.grid.plugin.BufferedRenderer', {
    override: 'Ext.grid.plugin.BufferedRenderer',
    rowHeight: 29, // comes from skyline theme

    init: function(grid) {
        this.callParent(arguments);

        // grid height calculated before the toolbar is on layouts, it causes the bug: JP-3817
        grid.on('boxready', function() {
            grid.view.refresh();
        })
    },

    bindStore: function(store) {
        var me = this;
        me.trailingBufferZone = 0;
        me.leadingBufferZone = store.pageSize;
        this.callParent(arguments);
    }
});