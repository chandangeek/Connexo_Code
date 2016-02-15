Ext.define('Sct.model.ServiceCallType', {
    extend: 'Uni.model.Version',
    fields: [
        'name', 'versionName', 'logLevel','serviceCallLifeCycle',
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
        },
        {
            name: 'lifecycle',
            persist: false,
            mapping: function (data) {
                if(data.serviceCallLifeCycle) {
                    return data.serviceCallLifeCycle.name;
                } else {
                    return 'undefined';
                }
            }
        },
        {
            name: 'logLevelName',
            persist: false,
            mapping: function (data) {
                if(data.logLevel) {
                    return data.logLevel.displayValue;
                } else {
                    return 'undefined';
                }
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/scs/servicecalltypes',
        reader: {
            type: 'json'
        }
    }
});