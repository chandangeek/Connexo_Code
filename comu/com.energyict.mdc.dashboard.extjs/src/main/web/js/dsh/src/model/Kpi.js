Ext.define('Dsh.model.Kpi', {
    extend: 'Ext.data.Model',
    requires: ['Dsh.model.Series'],
    fields: [
        { name: 'time'}
    ],
    hasMany: {
        model: 'Dsh.model.Series',
        name: 'series'
    }
});