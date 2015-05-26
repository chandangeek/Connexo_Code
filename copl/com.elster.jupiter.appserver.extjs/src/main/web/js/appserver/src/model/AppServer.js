Ext.define('Apr.model.AppServer', {
    extend: 'Ext.data.Model',
    fields: [
        'name', 'active', 'executionSpecs',
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
            name: 'messageServices',
            persist: false,
            mapping: function (data) {
                if (!Ext.isEmpty(data.executionSpecs)) {
                    var str = '';
                    Ext.Array.each(data.executionSpecs, function (item) {
                        str += Ext.String.htmlEncode(item.subscriberSpec.displayName) + ' (' + item.numberOfThreads + ' ' + Uni.I18n.translate('general.thread', 'APR', 'thread(s)') + ')' + '<br><br>';
                    });
                    return str;
                } else {
                    return '-';
                }
            }
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