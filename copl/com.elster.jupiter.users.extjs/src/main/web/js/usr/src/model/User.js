Ext.define('Usr.model.User', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'authenticationName',
        'description',
        'version',
        'domain',
        'active',
        {
            name: 'statusDisplay',
            persist: false,
            mapping: function (data) {
                if(data.active){
                    return Uni.I18n.translate('general.active', 'USR', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'USR', 'Inactive');
                }
            }
        },
        {name: 'language', type: 'auto', defaultValue: {}},
        'createdOn',
        'modifiedOn',
        'lastSuccessfulLogin',
        'lastUnSuccessfulLogin'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.Group',
            associationKey: 'groups',
            name: 'groups'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        reader: {
            type: 'json'
        }
    }
});