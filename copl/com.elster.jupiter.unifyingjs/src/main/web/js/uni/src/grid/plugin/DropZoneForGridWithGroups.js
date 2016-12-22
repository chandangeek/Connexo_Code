Ext.define('Uni.grid.plugin.DropZoneForGridWithGroups', {
    extend: 'Ext.grid.ViewDropZone',
    groupedByHiddenField: null,

    onNodeOver: function (nodeData, source, e, data) {
        var me = this;
        if (data && data.records && data.records[0] && me.groupedByHiddenField) {
            //Drop is allowed only on elements with group id of current record
            if (nodeData.innerHTML.indexOf(data.records[0].get(me.groupedByHiddenField)) < 0) {
                return me.dropNotAllowed;
            }
        }

        return this.callParent(arguments);
    },
    onContainerOver: function (source, e, data) {
        return this.dropNotAllowed;
    }
});