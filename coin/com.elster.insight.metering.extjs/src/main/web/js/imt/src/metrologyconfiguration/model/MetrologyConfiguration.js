Ext.define('Imt.metrologyconfiguration.model.MetrologyConfiguration', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'status', type: 'auto', defaultValue: null},
        {name: 'serviceCategory', type: 'auto', defaultValue: null},
        {name: 'meterRoles', type: 'auto', defaultValue: null},
        {name: 'description', type: 'string'},
        {name: 'active', type: 'boolean'},
        {name: 'requirements', type: 'auto'},
        {name: 'purposes', type: 'auto', defaultValue: null},
        {name: 'customPropertySets', type: 'auto', defaultValue: null, useNull: true},
        {
            name: 'created',
            persist: false,
            mapping: function(data) {
            	return data.createTime;
            }
        },
        {
            name: 'updated',
            persist: false,
            mapping: function(data) {
           		return data.modTime;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations',
        reader: {
            type: 'json'
        }
    }
});