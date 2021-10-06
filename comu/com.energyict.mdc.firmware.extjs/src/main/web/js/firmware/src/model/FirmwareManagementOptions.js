/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.model.FirmwareManagementOptions', {
    extend: 'Uni.model.Version',
    alternateClassName: 'FirmwareManagementOptions',
    fields: [
        {
            name: 'isAllowed',
            type: 'boolean',
            useNull: true
        },
        {
            name: 'validateFirmwareFileSignature',
            type: 'boolean',
            useNull: true
        },
        'supportedOptions',
        'allowedOptions',
        {
            name: 'selectedOptions',
            type: 'auto',
            useNull: true
        },
        {
            name: 'checkOptions',
            type: 'auto',
            useNull: true
        },
        {
            name: 'masterOptions',
            persist: false,
            mapping: function (data) {
                var masterData = {};
                if (data && data.checkOptions) {
                    if (data.checkOptions['MASTER_FIRMWARE_CHECK']) return data.checkOptions['MASTER_FIRMWARE_CHECK'];
                } else {
                    return null;
                }
            }
        },
        {
            name: 'currOptions',
            persist: false,
            mapping: function (data) {
                var masterData = {};
                if (data && data.checkOptions) {
                    if (data.checkOptions['CURRENT_FIRMWARE_CHECK']) return data.checkOptions['CURRENT_FIRMWARE_CHECK'];
                } else {
                    return null;
                }
            }
        },
        {
            name: 'targetOptions',
            persist: false,
            mapping: function (data) {
                if (data && data.checkOptions && data.checkOptions['TARGET_FIRMWARE_STATUS_CHECK']) {
                    return data.checkOptions['TARGET_FIRMWARE_STATUS_CHECK'];
                }
                return null;
            }
        }

    ],
    associations: [
        {
            type: 'hasMany',
            model: 'FirmwareManagementOption',
            name: 'supportedOptions',
            associatedModel: 'FirmwareManagementOption'
        },
        {
            type: 'hasMany',
            model: 'FirmwareManagementOption',
            name: 'allowedOptions',
            associatedModel: 'FirmwareManagementOption'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/firmwaremanagementoptions',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    },
    save: function () {
        var allowed = [];
        var supported = this.get('supportedOptions');
        var selected = new Array().concat(this.get('selectedOptions'));
        for (i = 0; i < supported.length; i++) {
            if (selected.indexOf(supported[i]["id"]) >= 0) {
                allowed.push(supported[i]);
            }
        }
        this.set('allowedOptions', allowed);

        this.callParent(arguments);
    }
});

