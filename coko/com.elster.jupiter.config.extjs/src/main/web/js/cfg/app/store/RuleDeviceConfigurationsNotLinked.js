Ext.define('Cfg.store.RuleDeviceConfigurationsNotLinked', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.RuleDeviceConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/dtc/validationruleset/{ruleSetId}/linkabledeviceconfigurations',
        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'deviceConfigurations'
        }
    }
});

