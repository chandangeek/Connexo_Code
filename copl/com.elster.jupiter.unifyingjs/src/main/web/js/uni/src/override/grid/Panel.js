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
    }
});