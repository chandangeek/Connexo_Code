Ext.define('Dxp.model.DataSource', {
    extend: 'Ext.data.Model',
    fields: [
        'mRID', 'active', 'serialNumber', 'readingType', 'occurrenceId',
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
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'lastExportedDate',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});
