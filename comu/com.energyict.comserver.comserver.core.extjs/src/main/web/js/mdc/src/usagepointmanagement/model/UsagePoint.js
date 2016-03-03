Ext.define('Mdc.usagepointmanagement.model.UsagePoint', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'serviceCategory', type: 'string'},
        {name: 'version', type: 'number', useNull: true},
        {
            name: 'created',
            persist: false,
            mapping: function (data) {
                return Uni.DateTime.formatDateTimeLong(new Date(data.createTime));
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }
});
