Ext.define('Mdc.store.LogbookOfDeviceData', {
    extend: 'Uni.data.store.Filterable',
    model: 'Mdc.model.LogbookOfDeviceData',
    storeId: 'LogbookOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/logbooks{logbookId}data',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,

        setUrl: function (params) {
            if (Ext.isDefined(params.logbookId)) {
                this.url = this.urlTpl.replace('{deviceId}', encodeURIComponent(params.deviceId)).replace('{logbookId}', '/' + params.logbookId + '/');
            } else {
                this.url = this.urlTpl.replace('{deviceId}', encodeURIComponent(params.deviceId)).replace('{logbookId}', '/');
            }
        }
    },
    setFilterModel: function (model) {
        var data = model.getData(),
            filters = [];

        Ext.iterate(data, function (key, value) {
            if (value) {
                if (Ext.isDate(value)) {
                    if (key === 'intervalEnd') {
                        value = moment(value).endOf('day').toDate();
                    }
                    value = value.getTime();
                }
                filters.push({
                    property: key,
                    value: value
                });
            }
        });

        this.clearFilter(true);
        this.addFilter(filters, false);
    }
});