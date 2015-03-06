Ext.define('Mdc.model.DataCollectionKpi', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto'},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'displayRange', type: 'auto', defaultValue: null},
        {name: 'connectionTarget', type: 'integer', useNull: true},
        {name: 'communicationTarget', type: 'integer', useNull: true},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/kpis'
    }
});