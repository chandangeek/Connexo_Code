Ext.define('Mdc.store.DeviceDataEstimationRulesSet', {
    extend: 'Ext.data.Store',
    requires: ['Mdc.model.DeviceDataEstimationRulesSet'],
    model: 'Mdc.model.DeviceDataEstimationRulesSet',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/estimationrulesets',
        timeout: 60000,
        reader: {
            type: 'json',
            root: 'estimationRuleSets',
            totalProperty: 'total'
        }
    }
});