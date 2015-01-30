Ext.define('Uni.override.grid.plugin.BufferedRenderer', {
    override: 'Ext.grid.plugin.BufferedRenderer',
    rowHeight: 29, // comes from skyline theme

    init: function(grid) {
        this.callParent(arguments);
        grid.view.cls = 'uni-infinite-scrolling-grid-view';
        // grid height calculated before the toolbar is on layouts, it causes the bug: JP-3817
        grid.on('boxready', function() {
            grid.view.refresh();
        });
    },

    bindStore: function(store) {
        var me = this;
        me.trailingBufferZone = 10; // No idea why this needs to be 0, without it some grids don't work.
        me.leadingBufferZone = store.pageSize;
        me.callParent(arguments);
    }
});