Ext.define('Imt.usagepointmanagement.model.technicalinfo.Water', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'grounded', type: 'boolean'},
        {name: 'pressure', type: 'auto', defaultValue: null},
        {name: 'physicalCapacity', type: 'auto', defaultValue: null},
        {name: 'limiter', type: 'boolean'},
        {name: 'loadLimiterType', type: 'string'},
        {name: 'loadLimit', type: 'auto', defaultValue: null},
        {name: 'bypass', type: 'boolean', useNull: true},
        {name: 'bypassStatus', type: 'auto', defaultValue: null},
        {name: 'valve', type: 'boolean', useNull: true},
        {name: 'collar', type: 'boolean', useNull: true},
        {name: 'capped', type: 'boolean', useNull: true},
        {name: 'clamped', type: 'boolean', useNull: true}
    ]
});