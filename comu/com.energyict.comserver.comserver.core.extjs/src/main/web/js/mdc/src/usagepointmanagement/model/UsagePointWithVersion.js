Ext.define('Mdc.usagepointmanagement.model.UsagePointWithVersion', {
    extend: 'Uni.model.Version',
    requires: [],

    fields: [
        // {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'serviceCategory', type: 'string', defaultValue: null, useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'installationTime', type: 'int', defaultValue: null, useNull: true},
        {name: 'metrologyConfigurationVersion', type: 'auto', defaultValue: null, useNull: true},
        {name: 'meterActivation', type: 'auto', defaultValue: null, useNull: true},
        {
            name: 'id',
            type: 'number',
            persist: false,
            mapping: function (record) {
                return record.get('metrologyConfigurationVersion').id;
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/mtr/usagepoints/{mRID}/metrologyconfigurationversion',
        timeout: 240000,
        reader: {
            type: 'json'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }

});
