Ext.define('Usr.store.SecurityProtocols', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.SecurityProtocol',
    proxy: {
        type: 'memory'
    },
    data:  [
        {
            name: Uni.I18n.translate('userDirectories.securityProtocol.none', 'USR', 'None'),
            value: 'None'
        },
        {
            name: Uni.I18n.translate('userDirectories.securityProtocol.ssl', 'USR', 'SSL'),
            value: 'SSL'
        },
        {
            name: Uni.I18n.translate('userDirectories.securityProtocol.none', 'USR', 'TLS'),
            value: 'TLS'
        }
    ]
});
