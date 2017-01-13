Ext.define('Uni.grid.plugin.DragDropForGridWithGroups', {
    extend: 'Ext.grid.plugin.DragDrop',
    alias: 'plugin.gridviewwithgroupsdragdrop',
    requires: [
        'Uni.grid.plugin.DropZoneForGridWithGroups'
    ],

    groupedByHiddenField: null,

    onViewRender: function (view) {
        var me = this;
        me.callParent(arguments);

        me.dropZone = Ext.create('Uni.grid.plugin.DropZoneForGridWithGroups', {
            groupedByHiddenField: me.groupedByHiddenField,
            view: view,
            ddGroup: me.dropGroup || me.ddGroup
        });
    }
});
