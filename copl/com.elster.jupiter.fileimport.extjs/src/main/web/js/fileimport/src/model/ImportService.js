Ext.define('Fim.model.ImportService', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'id',
        'name',
        'active',
		'deleted',
        'application',
		'importerAvailable',
        {
            name: 'applicationDisplay',
            persist: false,
            convert: function (value, record) {
                return Uni.I18n.translate(record.get('application'), 'USR', record.get('application'));
            }
        },        
        'importDirectory',
        'inProcessDirectory',
        'successDirectory',
        'failureDirectory',
        'pathMatcher',
        'importerName',
        'scanFrequency',		
        'importerInfo',
        {
            name: 'statusDisplay',
            persist: false,
            convert: function (value, record) {
				return record.get('deleted') ? Uni.I18n.translate('general.removed', 'FIM', 'Removed') :
						!record.get('importerAvailable') ? Uni.I18n.translate('general.notAvailable', 'FIM', 'Not available') :
						record.get('active') ? Uni.I18n.translate('general.active', 'FIM', 'Active') : Uni.I18n.translate('general.inactive', 'FIM', 'Inactive');                                
            }
        },
        {
            name: 'scanFrequencyDisplay',
            persist: false,
            convert: function (value, record) {
                return Ext.String.format(Uni.I18n.translate('general.minutes', 'FIM', 'Every {0} minute(s)'), record.get('scanFrequency'));
            }
        },
        {
            name: 'fileImporter',
            persist: false,
            convert: function (value, record) {
                if (record.data.importerInfo && record.data.importerInfo.displayName) {
                    return record.data.importerInfo.displayName;
                }
                return null;
            }
        }
    ],

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
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