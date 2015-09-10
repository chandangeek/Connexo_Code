Ext.define('Usr.model.MgmUserDirectory', {
    extend: 'Ext.data.Model',
    fields: [
    /*    'id',
        'name',
        'prefix',
        'url',
        'isDefault',
        'securityProtocol',
        'type',
        {
            name: 'typeDisplay',
            persist: false,
            convert: function (value, record) {
                switch(record.get('type')) {
                    case 'ACD':
                        return Uni.I18n.translate('userDirectories.type.apacheDirectory', 'USR', 'Apache directory');
                        break;
                    case 'APD':
                        return Uni.I18n.translate('userDirectories.type.apacheDS', 'USR', 'Apache DS');
                        break;
                    default:
                        return Uni.I18n.translate('userDirectories.type.none', 'USR', 'None');
                }
            }
        },
        'backupUrl',
        'baseUser',
        'baseGroup'
        */

        'importDirectory',
        'inProcessDirectory',
        'successDirectory',
        'failureDirectory',
        'pathMatcher',

        'id',
        'name',
        {
            name: 'isDefault',
            convert: function (value, record) {
                return true;
            }
        },
        {
            name: 'prefix',
            convert: function (value, record) {
                return record.get('importDirectory');
            }
        },
        {
            name: 'url',
            convert: function (value, record) {
                return record.get('inProcessDirectory');
            }
        },
        {
            name: 'securityProtocol',
            convert: function (value, record) {
                return 'SSL';
            }
        },
        {
            name: 'type',
            convert: function (value, record) {
                return 'APD';
            }
        },
        {
            name: 'typeDisplay',
            persist: false,
            convert: function (value, record) {
                switch(record.get('type')) {
                    case 'ACD':
                        return Uni.I18n.translate('userDirectories.type.activeDirectory', 'USR', 'Active directory');
                        break;
                    case 'APD':
                        return Uni.I18n.translate('userDirectories.type.apacheDS', 'USR', 'Apache DS');
                        break;
                    default:
                        return Uni.I18n.translate('userDirectories.type.none', 'USR', 'None');
                }
            }
        },
        {
            name: 'backupUrl',
            convert: function (value, record) {
                return record.get('successDirectory');
            }
        },
        {
            name: 'baseUser',
            convert: function (value, record) {
                return record.get('failureDirectory');
            }
        },
        {
            name: 'baseGroup',
            convert: function (value, record) {
                return record.get('pathMatcher');
            }
        }
    ],
    
    proxy: {
        type: 'rest',
        url: '/api/fir/importservices',
        reader: {
            type: 'json'
        }
    }
});