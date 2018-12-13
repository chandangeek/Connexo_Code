/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.model.ServiceCall', {
    extend: 'Uni.model.Version',
    requires: [
        'Scs.model.AttributeSetOnServiceCall'
    ],
    fields: [
        'name', 'externalReference', 'state', 'type', 'origin',
        {
            name: 'version',
            defaultValue: 0
        },
        {
            name: 'parents',
            type: 'auto'
        },
        {
            name: 'children',
            type: 'auto'
        },
        {
            name: 'targetObject',
            type: 'auto'
        },
        {
            name: 'canCancel',
            type: 'boolean'
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
            name: 'creationTimeDisplayLong',
            type: 'string',
            persist: false,
            convert: function (value, record) {
                var creationTime = record.get('creationTime');
                if (creationTime && (creationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(creationTime));
                }
                return '-';
            }
        },
        {
            name: 'lastModificationTimeDisplayLong',
            type: 'string',
            persist: false,
            convert: function (value, record) {
                var lastModificationTime = record.get('lastModificationTime');
                if (lastModificationTime && (lastModificationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(lastModificationTime));
                }
                return '-';
            }
        },
        {
            name: 'creationTimeDisplayShort',
            type: 'string',
            persist: false,
            convert: function (value, record) {
                var creationTime = record.get('creationTime');
                if (creationTime && (creationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeShort(new Date(creationTime));
                }
                return '-';
            }
        },
        {
            name: 'lastModificationTimeDisplayShort',
            type: 'string',
            persist: false,
            convert: function (value, record) {
                var lastModificationTime = record.get('lastModificationTime');
                if (lastModificationTime && (lastModificationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeShort(new Date(lastModificationTime));
                }
                return '-';
            }
        },
        {
            name: 'lastCompletedTimeDisplay',
            type: 'string',
            persist: false,
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