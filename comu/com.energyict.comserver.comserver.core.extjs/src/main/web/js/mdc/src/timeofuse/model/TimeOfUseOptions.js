/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.model.TimeOfUseOptions', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.timeofuse.model.TimeOfUseOption'
    ],
    fields: [
        {
            name: 'isAllowed',
            type: 'boolean',
            useNull: true
        },
        {
            name: 'version',
            type: 'number'
        },
        {
            name: 'id',
            persist: false,
            type: 'number'
        }
    ],
    associations: [
        {
            name: 'supportedOptions',
            type: 'hasMany',
            model: 'Mdc.timeofuse.model.TimeOfUseOption',
            associationKey: 'supportedOptions',
            foreignKey: 'supportedOptions',
            getTypeDiscriminator: function (node) {
                return 'Mdc.timeofuse.model.TimeOfUseOption';
            }
        },
        {
            name: 'allowedOptions',
            type: 'hasMany',
            model: 'Mdc.timeofuse.model.TimeOfUseOption',
            associationKey: 'allowedOptions',
            foreignKey: 'allowedOptions',
            getTypeDiscriminator: function (node) {
                return 'Mdc.timeofuse.model.TimeOfUseOption';
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/timeofuseoptions',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});

