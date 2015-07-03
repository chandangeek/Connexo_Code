Ext.define('Mdc.usagepointmanagement.model.UsagePointElectricity', {
    extend: 'Mdc.usagepointmanagement.model.UsagePoint',
    fields: [
        {name: 'nominalServiceVoltage', type: 'auto'},
        {name: 'ratedCurrent', type: 'auto'},
        {name: 'ratedPower', type: 'auto'},
        {name: 'estimatedLoad', type: 'auto'},
        {name: 'phaseCode', type: 'string'},
        {name: 'grounded', type: 'boolean'}
    ]
});
