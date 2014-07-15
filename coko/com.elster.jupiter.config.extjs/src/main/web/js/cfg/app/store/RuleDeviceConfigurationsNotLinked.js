Ext.define('Cfg.store.RuleDeviceConfigurationsNotLinked', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.RuleDeviceConfiguration',
    pageSize: 100,
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

