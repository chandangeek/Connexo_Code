Ext.define('Mdc.usagepointmanagement.model.UsagePointWithVersion', {
    extend: 'Uni.model.Version',
    requires: [],
    idProperty: 'versionId',

    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'serviceCategory', type: 'string', defaultValue: null, useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'installationTime', type: 'int', defaultValue: null, useNull: true},
        {name: 'metrologyConfigurationVersion', type: 'auto'},
        {name: 'meterActivations', type: 'auto', defaultValue: null, useNull: true},
        {
            name: 'versionId',
            type: 'number',
            persist: false,
            mapping: function (record) {
                return record.metrologyConfigurationVersion.id;
            },
            defaultValue: null,
            useNull: true
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/{usagePointId}/metrologyconfigurationversion',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});
