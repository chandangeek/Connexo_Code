Ext.define('Mdc.store.ValidationRuleSetsForDeviceConfig', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.ValidationRuleSet',
    buffered: true,
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/linkablevalidationrulesets',
        reader: {
            type: 'json',
            root: 'validationRuleSets',
            totalProperty: 'total'
        }
    }
});
