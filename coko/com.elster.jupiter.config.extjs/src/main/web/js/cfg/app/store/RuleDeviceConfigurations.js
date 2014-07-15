Ext.define('Cfg.store.RuleDeviceConfigurations', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.RuleDeviceConfiguration',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '/api/dtc/validationruleset/{ruleSetId}/deviceconfigurations',
        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'deviceConfigurations'
        }
    }
});
