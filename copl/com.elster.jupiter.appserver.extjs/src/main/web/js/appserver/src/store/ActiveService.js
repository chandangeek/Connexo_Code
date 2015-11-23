Ext.define('Apr.store.ActiveService', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ActiveService',
    autoLoad: false,

    data: [
        {
            active: true,
            displayName: Uni.I18n.translate('general.active', 'APR', 'Active')
        },
        {
            active: false,
            displayName: Uni.I18n.translate('general.inactive', 'APR', 'Inactive')
        }
    ]
});
