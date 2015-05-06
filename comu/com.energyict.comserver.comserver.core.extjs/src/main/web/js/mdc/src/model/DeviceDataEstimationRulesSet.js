Ext.define('Mdc.model.DeviceDataEstimationRulesSet', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'active', type: 'boolean'},
        {name: 'numberOfInactiveRules', type: 'int'},
        {name: 'numberOfRules', type: 'int'},
        'parent',
        {
            name: 'numberOfActiveRules',
            persist: false,
            mapping: function (data) {
                return data.numberOfRules - data.numberOfInactiveRules;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/estimationrulesets',
        timeout: 60000,
        reader: {
            type: 'json'
        }
    }
});