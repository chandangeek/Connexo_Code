Ext.define('Skyline.grid.Panel', {
    override: 'Ext.grid.Panel',
//    border: false,
//    frame: true,
    bodyBorder: true,
    enableColumnHide: false,
    enableColumnMove: false,
    enableColumnResize: false,
    sortableColumns: false,
    collapsible: false,
    selModel: {
        mode: 'SINGLE'
    }
});