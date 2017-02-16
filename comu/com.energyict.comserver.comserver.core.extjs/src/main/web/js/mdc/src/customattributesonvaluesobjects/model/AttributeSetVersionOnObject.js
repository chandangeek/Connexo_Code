/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],
    idProperty: 'versionId',
    fields: [
        {name: 'versionId', type: 'integer', useNull: true},
        {name: 'objectTypeId', type: 'integer'},
        {name: 'objectTypeVersion', type: 'integer'},
        {name: 'name', type: 'string'},
        {name: 'editable', type: 'boolean'},
        {name: 'timesliced', type: 'boolean'},
        {name: 'startTime', type: 'timestamp'},
        {name: 'endTime', type: 'timestamp'},
        {
            name: 'period',
            type: 'string',
            persist: false,
            mapping: function (data) {
                var periodStr = '';

                if (data.startTime) {
                    periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.from', 'MDC', 'From'), Uni.DateTime.formatDateTimeShort(new Date(data.startTime)));
                }
                if (data.startTime && data.endTime) {
                    periodStr += ' - ';
                }
                if (data.endTime) {
                    periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.until', 'MDC', 'Until'), Uni.DateTime.formatDateTimeShort(new Date(data.endTime)));
                }
                if (!data.endTime && !data.startTime) {
                    periodStr += Uni.I18n.translate('general.infinite', 'MDC', 'Infinite');
                }

                return periodStr;
            }
        }
    ],

    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
});
