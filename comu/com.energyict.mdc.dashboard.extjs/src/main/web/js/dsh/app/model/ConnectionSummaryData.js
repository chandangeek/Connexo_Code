Ext.define('Dsh.model.ConnectionSummaryData', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'total', type: 'int'}
    ],

    hasMany: {
        model: 'Dsh.model.ConnectionCounter',
        name: 'counters'
    }
});