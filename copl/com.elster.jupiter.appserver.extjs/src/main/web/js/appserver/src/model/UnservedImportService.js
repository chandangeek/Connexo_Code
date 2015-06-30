Ext.define('Apr.model.UnservedImportService', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name',
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                return data.deleted ? Uni.I18n.translate('general.removed', 'APR', 'Removed') :
                    !data.importerAvailable ? Uni.I18n.translate('general.notAvailable', 'APR', 'Not available') :
                        data.active ? Uni.I18n.translate('general.active', 'APR', 'Active') : Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
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