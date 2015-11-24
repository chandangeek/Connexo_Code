Ext.define('Mtr.readingtypes.attributes.store.Status',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    data : [
        {code: 'true', displayName: Uni.I18n.translate('readingtypesmanagment.general.active', 'MTR', 'Active')},
        {code: 'false', displayName: Uni.I18n.translate('readingtypesmanagment.general.inactive', 'MTR', 'Inactive')}
    ]
});
