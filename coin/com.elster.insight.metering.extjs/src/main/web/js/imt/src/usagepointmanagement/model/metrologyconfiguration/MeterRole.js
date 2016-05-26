Ext.define('Imt.usagepointmanagement.model.metrologyconfiguration.MeterRole', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name', 'required', 'mRID',
        {
            name: 'activationTime',
            type: 'date',
            dateFormat: 'time'
        }
    ]
});
