Ext.define('Uni.override.grid.Panel', {
    override: 'Ext.grid.Panel',

    enableColumnHide: false,
    enableColumnMove: false,
    enableColumnResize: false,
    sortableColumns: false,
    collapsible: false,
    overflowY: 'auto',

    selModel: {
        mode: 'SINGLE'
    },

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.on('afterlayout', function () {
            if (me.lastGridScrollPosition) {
                me.getView().getEl().scrollTo('top', me.lastGridScrollPosition.top, false);
            }
        });
        me.on('beforeselect', function () {
            this.lastGridScrollPosition = this.getView().getEl().getScroll();
        });
    }
});