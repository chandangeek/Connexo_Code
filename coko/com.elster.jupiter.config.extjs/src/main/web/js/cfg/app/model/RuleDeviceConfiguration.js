Ext.define('Cfg.model.RuleDeviceConfiguration', {
    extend: 'Ext.data.Model',
    fields: [
        'deviceconfiguration' ,
        'devicetype'
    ],
    proxy: {
        type: 'rest',
        url: '/api/val/validation/rulesets/{ruleSetId}/deviceconfigurations',
        reader: {
            type: 'json',
            totalProperty: 'total'
        }
    }
});

