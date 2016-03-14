Ext.define('Scs.model.ServiceCall', {
    extend: 'Uni.model.Version',
    requires: [
        'Scs.model.AttributeSetOnServiceCall'
    ],
    fields: [
        'name', 'externalReference', 'state', 'type', 'parents', 'children', 'origin', 'targetObject',
        {
            name: 'version',
            defaultValue: 0
        },
        {
            name: 'creationTime',
            type: 'number'
        },
        {
            name: 'lastModificationTime',
            type: 'number'
        },
        {
            name: 'lastCompletedTime',
            type: 'number'
        },
        {
            name: 'numberOfChildren',
            type: 'number'
        },
        {
            name: 'topLevelParent',
            persist: false,
            mapping: function (data) {
                 if(data.parents) {
                     return data.parents.length > 1 ? data.parents[0] : "";
                 }
                return "";
            }
        },
        {
            name: 'parent',
            persist: false,
            mapping: function (data) {
                if(data.parents) {
                    return data.parents.length > 0 ? data.parents[data.parents.length - 1] : "";
                }
            }
        },
        {
            name: 'creationTimeDisplay',
            type: 'string',
            convert: function (value, record) {
                var creationTime = record.get('creationTime');
                if (creationTime && (creationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(creationTime));
                }
                return '-';
            }
        },
        {
            name: 'lastModificationTimeDisplay',
            type: 'string',
            convert: function (value, record) {
                var lastModificationTime = record.get('lastModificationTime');
                if (lastModificationTime && (lastModificationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(lastModificationTime));
                }
                return '-';
            }
        },
        {
            name: 'lastCompletedTimeDisplay',
            type: 'string',
            convert: function (value, record) {
                var lastCompletedTime = record.get('lastModificationTime');
                if (lastCompletedTime && (lastCompletedTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(lastCompletedTime));
                }
                return '-';
            }
        }
    ],

    associations: [
        {
            name: 'customPropertySets',
            type: 'hasMany',
            model: 'Scs.model.AttributeSetOnServiceCall',
            associationKey: 'customPropertySets',
            foreignKey: 'customPropertySets',
            getTypeDiscriminator: function (node) {
                return 'Scs.model.AttributeSetOnServiceCall';
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/scs/servicecalls',
        timeout: 120000,
        reader: {
            type: 'json'
        }
    }
});