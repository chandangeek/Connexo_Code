Ext.define('Mdc.store.RuleDeviceConfigurationsNotLinked', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.RuleDeviceConfiguration',
    buffered: true,
    storeId: 'RuleDeviceConfigurationsNotLinked',
    pageSize: 10,
    trailingBufferZone: 5,
    leadingBufferZone: 5,
    purgePageCount: 0,
    scrollToLoadBuffer: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/validationruleset/{ruleSetId}/linkabledeviceconfigurations',
        timeout: 240000,
        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'deviceConfigurations'
        }
    }
});

