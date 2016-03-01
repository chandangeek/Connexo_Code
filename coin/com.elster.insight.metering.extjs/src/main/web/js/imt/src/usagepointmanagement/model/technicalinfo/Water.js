Ext.define('Imt.usagepointmanagement.model.technicalinfo.Water', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'grounded', type: 'boolean'},
        {name: 'pressure', type: 'auto', defaultValue: null},
        {name: 'physicalCapacity', type: 'auto', defaultValue: null},
        {name: 'limiter', type: 'boolean'},
        {name: 'loadLimiterType', type: 'string'},
        {name: 'loadLimit', type: 'auto', defaultValue: null},
        {name: 'bypass', type: 'string', useNull: true, defaultValue: 'UNKNOWN'},
        {name: 'bypassStatus', type: 'auto', defaultValue: null},
        {name: 'valve', type: 'string', useNull: true, defaultValue: 'UNKNOWN'},
        {name: 'collar', type: 'string', useNull: true, defaultValue: 'UNKNOWN'},
        {name: 'capped', type: 'string', useNull: true, defaultValue: 'UNKNOWN'},
        {name: 'clamped', type: 'string', useNull: true, defaultValue: 'UNKNOWN'}
    ]
});