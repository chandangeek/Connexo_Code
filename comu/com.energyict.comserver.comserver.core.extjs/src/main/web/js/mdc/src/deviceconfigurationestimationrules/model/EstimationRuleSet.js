Ext.define('Mdc.deviceconfigurationestimationrules.model.EstimationRuleSet', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'numberOfInactiveRules', type: 'int'},
        {name: 'numberOfRules', type: 'int'},
        {name: 'parent', type: 'auto'},
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
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/estimationrulesets',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{deviceTypeId}', params.deviceTypeId).replace('{deviceConfigurationId}', params.deviceConfigurationId);
        }
    }


});
