Ext.define('Sct.model.ServiceCallType', {
    extend: 'Uni.model.Version',
    fields: [
        'type', 'versionName', 'loglevel','lifecycle',
        {
            name: 'version',
            defaultValue: 0
        },
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.active) {
                    return Uni.I18n.translate('general.active', 'SCT', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'SCT', 'Inactive');
                }
            }
        }
    ],
/*
    proxy: {
        type: 'rest',
        url: '/api/apr/appserver',
        reader: {
            type: 'json'
        }
    }*/
});