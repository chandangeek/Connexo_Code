Ext.define('Apr.model.UnservedImportService', {
    extend: 'Ext.data.Model',
    fields: [
        'id','name',
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
        },
        {
            name: 'importService',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        }
    ]
});