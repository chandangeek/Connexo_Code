Ext.define('Mdc.store.LogbookOfDeviceData', {
    extend: 'Uni.data.store.Filterable',
    model: 'Mdc.model.LogbookOfDeviceData',
    storeId: 'LogbookOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/logbooks{logbookId}data',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,

        setUrl: function (mRID, logbookId) {
            if (Ext.isDefined(logbookId)) {
                this.url = this.urlTpl.replace('{mRID}', mRID).replace('{logbookId}', '/' + logbookId + '/');
            } else {
                this.url = this.urlTpl.replace('{mRID}', mRID).replace('{logbookId}', '/');
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