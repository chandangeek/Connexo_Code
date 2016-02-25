Ext.define('Imt.usagepointmanagement.model.technicalinfo.Thermal', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'pressure', type: 'auto', defaultValue: null},
        {name: 'physicalCapacity', type: 'auto', defaultValue: null},
        {name: 'bypass', type: 'boolean', useNull: true},
        {name: 'bypassStatus', type: 'auto', defaultValue: null},
        {name: 'valve', type: 'boolean', useNull: true},
        {name: 'collar', type: 'boolean', useNull: true},
        {name: 'interruptible', type: 'boolean'}
    ]
});