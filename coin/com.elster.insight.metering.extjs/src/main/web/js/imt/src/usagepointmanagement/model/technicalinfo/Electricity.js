Ext.define('Imt.usagepointmanagement.model.technicalinfo.Electricity', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'grounded', type: 'boolean'},
        {name: 'nominalServiceVoltage', type: 'auto', defaultValue: null},
        {name: 'phaseCode', type: 'auto', defaultValue: null},
        {name: 'rattedCurrent', type: 'auto', defaultValue: null},
        {name: 'ratedPower', type: 'auto', defaultValue: null},
        {name: 'estimatedLoad', type: 'auto', defaultValue: null},
        {name: 'limiter', type: 'boolean'},
        {name: 'loadLimiterType', type: 'string'},
        {name: 'loadLimit', type: 'auto', defaultValue: null},
        {name: 'collar', type: 'boolean', useNull: true},
        {name: 'interruptible', type: 'boolean'}
    ]
});