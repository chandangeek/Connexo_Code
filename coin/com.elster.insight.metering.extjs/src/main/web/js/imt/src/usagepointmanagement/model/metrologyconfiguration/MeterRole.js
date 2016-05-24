Ext.define('Imt.usagepointmanagement.model.metrologyconfiguration.MeterRole', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name', 'required',
        {
            name: 'meter',
            type: 'auto'
        },
        {
            name: 'activationDate',
            type: 'date',
            dateFormat: 'time'
        }
    ]
});
