Ext.define('Mdc.model.DateRange', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'from', type: 'date', dateFormat: 'Y-m-dTH:i:s' }
    ]
});