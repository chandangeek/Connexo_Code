Ext.define('Cfg.model.ValidationRule', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        'id',
        'active',
        'action',
        'implementation',
        'displayName',
        'readingTypes',
        'properties',
        'name',
        {
            name: 'ruleSetVersion'
        },
        {
            name: 'ruleSetVersionId',
            persist: false,
            convert: function (value, record) {
                return record.data.ruleSetVersion.id;
            }
        },
        {
            name: 'ruleSetVersionName',
            persist: false,
            convert: function (value, record) {
                return record.data.ruleSetVersion.name;
            }
        },
        {
            name: 'rule_name',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/validation/rulesets/validationrules/' + data.ruleSetVersion.id + '/ruleoverview/' + data.id + '">' + data.name + '</a>';
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
        },
        {
            type: 'hasMany',
            model: 'Cfg.model.ReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/val/validation/{ruleSetId}/versions/{versionId}/rules',
        reader: {
            type: 'json',
            root: 'rules'
        },
        setUrl: function (ruleSetId, versionId) {
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId).replace('{versionId}', versionId);
        },

        timeout: 300000
    }
});

