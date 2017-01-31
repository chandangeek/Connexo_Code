/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.model.Firmware', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {
            name: 'type',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareType ? data.firmwareType.localizedValue : '';
            }
        },
        {
            name: 'version',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.activeVersion ? data.activeVersion.firmwareVersion : Uni.I18n.translate('device.firmware.version.unknown', 'FWC', 'Unknown');
            }
        }
    ],

    requires: [
        'Fwc.model.FirmwareType',
        'Fwc.devicefirmware.model.FirmwareVersion'
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Fwc.model.FirmwareType',
            name: 'firmwareType',
            associationKey: 'firmwareType'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'activeVersion',
            associatedName: 'activeVersion',
            associationKey: 'activeVersion',
            getterName: 'getActiveVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'pendingVersion',
            associatedName: 'pendingVersion',
            associationKey: 'pendingVersion',
            getterName: 'getPendingVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'failedVersion',
            associatedName: 'failedVersion',
            associationKey: 'failedVersion',
            getterName: 'getFailedVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'ongoingVersion',
            associatedName: 'ongoingVersion',
            associationKey: 'ongoingVersion',
            getterName: 'getOngoingVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'needVerificationVersion',
            associatedName: 'needVerificationVersion',
            associationKey: 'needVerificationVersion',
            getterName: 'getVerificationVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'failedVerificationVersion',
            associatedName: 'failedVerificationVersion',
            associationKey: 'failedVerificationVersion',
            getterName: 'getFailedVerificationVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'wrongVerificationVersion',
            associatedName: 'wrongVerificationVersion',
            associationKey: 'wrongVerificationVersion',
            getterName: 'getWrongVerificationVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'needActivationVersion',
            associatedName: 'needActivationVersion',
            associationKey: 'needActivationVersion',
            getterName: 'getNeedActivationVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'ongoingActivatingVersion',
            associatedName: 'ongoingActivatingVersion',
            associationKey: 'ongoingActivatingVersion',
            getterName: 'getOngoingActivatingVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'failedActivatingVersion',
            associatedName: 'failedActivatingVersion',
            associationKey: 'failedActivatingVersion',
            getterName: 'getFailedActivatingVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'activatingVersion',
            associatedName: 'activatingVersion',
            associationKey: 'activatingVersion',
            getterName: 'getActivatingVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'ongoingVerificationVersion',
            associatedName: 'ongoingVerificationVersion',
            associationKey: 'ongoingVerificationVersion',
            getterName: 'getOngoingVerificationVersion'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/fwc/devices/{deviceId}/firmwares',
        reader: 'json'
    }
});