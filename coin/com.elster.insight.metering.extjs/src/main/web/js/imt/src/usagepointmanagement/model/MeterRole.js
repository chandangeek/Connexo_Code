Ext.define('Imt.usagepointmanagement.model.MeterRole', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {
            name: 'name',
            mapping: 'meterRole.name'
        },
        {
            name: 'required',
            mapping: 'meterRole.required'
        },
        {
            name: 'activationTime',
            mapping: 'meterRole.activationTime'
        },
        {
            name: 'meter',
            mapping: 'meter.name'
        },
        {
            name: 'url',
            mapping: 'meter.activationTime'
        }
    ]
});