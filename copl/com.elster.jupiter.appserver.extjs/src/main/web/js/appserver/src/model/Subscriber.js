Ext.define('Apr.model.Subscriber', {
    extend: 'Uni.model.Version',
    idProperty: 'subscriber',
    fields: [
        'subscriber', 'displayName', 'destination',
        {
            name: 'numberOfThreads',
            type: 'int'
        },
        {
            name: 'active',
            type: 'string',
            mapping:  function (value) {
                if (value === 0){
                    return Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
                }else{
                    return Uni.I18n.translate('general.active', 'APR', 'Active');
                }
            }
        }
    ]
});
