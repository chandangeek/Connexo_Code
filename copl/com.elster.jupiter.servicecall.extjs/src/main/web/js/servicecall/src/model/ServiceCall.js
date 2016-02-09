Ext.define('Scs.model.ServiceCall', {
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
                    return Uni.I18n.translate('general.active', 'SCS', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'SCS', 'Inactive');
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