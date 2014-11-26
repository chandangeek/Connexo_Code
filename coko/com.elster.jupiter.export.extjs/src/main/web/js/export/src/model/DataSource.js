Ext.define('Dxp.model.DataSource', {
    extend: 'Ext.data.Model',
    fields: [
        'mRID', 'active', 'serialNumber', 'readingType',
        {
            name: 'active',
            mapping: function (data) {
                if (data.active) {
                    return Uni.I18n.translate('general.active', 'DES', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'DES', 'Inactive');
                }
            }
        },
        {
            name: 'lastRun',
            mapping: function (data) {
                return moment(data.lastRun).format('ddd, DD MMM YYYY HH:mm:ss');
            }
        },
        {
            name: 'lastExportedDate',
            mapping: function (data) {
                return moment(data.lastExportedDate).format('ddd, DD MMM YYYY HH:mm:ss');
            }
        }
    ]
});
