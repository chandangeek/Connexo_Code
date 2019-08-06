/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.AutoclosureExclusion', {
    extend: 'Uni.model.Version',
    requires: [
        'Mdc.model.AutoclosureExclusionIssueType',
		'Mdc.model.AutoclosureExclusionTemplate',
		'Mdc.model.AutoclosureExclusionReason'
    ],
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int',
            useNull: true
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'priority',
            type: 'auto'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date',
            persist: false
        },
        {
            name: 'modificationDate',
            dateFormat: 'time',
            type: 'date',
            persist: false
        },
        {
            name: 'version',
            type: 'int'
        },
        {
            name: 'dueIn',
            type: 'auto'
        },
		{
            name: 'title',
            persist: false,
            mapping: 'name'
        },
        {
            name: 'issueType_name',
            persist: false,
            mapping: 'issueType.name'
        },
		{
            name: 'reason_name',
            persist: false,
            mapping: 'reason.name'
        },
        {
            name: 'template_name',
            persist: false,
            mapping: 'template.displayName'
        },
        {
            name: 'active'
        }
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Mdc.model.AutoclosureExclusionIssueType',
            associatedName: 'issueType',
            associationKey: 'issueType',
            getterName: 'getIssueType',
            setterName: 'setIssueType'
        },
		{
            type: 'hasOne',
            model: 'Mdc.model.AutoclosureExclusionTemplate',
            associatedName: 'template',
            associationKey: 'template',
            getterName: 'getTemplate',
            setterName: 'setTemplate'
        },
        {
            type: 'hasOne',
            model: 'Mdc.model.AutoclosureExclusionReason',
            associatedName: 'reason',
            associationKey: 'reason',
            getterName: 'getReason',
            setterName: 'setReason'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules/device/{deviceId}/excludedfromautoclosurerules',
        reader: {
            type: 'json'
        }
    }
});
