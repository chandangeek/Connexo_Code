Ext.define('Idv.view.NonEstimatedDataGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'no-estimated-data-grid',
    title: 'Employees',
    //store: null,
    requires: [
        'Ext.grid.feature.Grouping'
    ],
    columns: [
        { text: 'startTime',     dataIndex: 'startTime' },
        { text: 'endTime',     dataIndex: 'endTime' },
        { text: 'amountOfSuspects', dataIndex: 'amountOfSuspects' }
    ],
    features: [{
        ftype:'grouping',
        //groupHeaderTpl: 'sdf'
    }]
});