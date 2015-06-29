Ext.define('Apr.model.AppServer', {
    extend: 'Ext.data.Model',
    fields: [
        'name', 'active', 'executionSpecs', 'importServices',
        {
            name: 'id',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        },
        {
            name: 'exportPath',
            persist: false
        },
        {
            name: 'importPath',
            persist: false
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.active) {
                    return Uni.I18n.translate('general.active', 'APR', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
                }
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/apr/appserver',
        reader: {
            type: 'json'
        }
    }
});