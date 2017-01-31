/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.model.Firmware', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'firmwareVersion', type: 'string', useNull: true},
        {name: 'firmwareFile', useNull: true},
        {name: 'fileSize', type: 'number', useNull: true},
        {
            name: 'type',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareType ? data.firmwareType.localizedValue : '';
            }
        },
        {
            name: 'status',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareStatus ? data.firmwareStatus.localizedValue : '';
            }
        }
    ],

    requires: [
        'Fwc.model.FirmwareType',
        'Fwc.model.FirmwareStatus'
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
            model: 'Fwc.model.FirmwareStatus',
            name: 'firmwareStatus',
            associationKey: 'firmwareStatus',
            getterName: 'getFirmwareStatus',
            setterName: 'setFirmwareStatus'
        }
    ],

    doValidate: function (callback) {
        var data = this.getProxy().getWriter().getRecordData(this);
        delete data.firmwareFile;

        Ext.Ajax.request({
            method: this.hasId() ? 'PUT' : 'POST',
            url: this.proxy.url + (this.hasId() ? '/' + this.getId() : '') + '/validate',
            callback: callback,
            jsonData: data
        });
    },

    doSave: function (callback, form) {
        var request = {
            method: 'POST',
            headers: {'Content-type': 'multipart/form-data'},
            url: this.proxy.url + (this.hasId() ? '/' + this.getId() : ''),
            form: form.getEl().dom,
            isUpload: true,
            hasUpload: true,
            dontTryAgain: true
        };

        if (Ext.isFunction(callback)) {
            request.callback = callback;
            Ext.Ajax.request(request);
        } else {
            Ext.Ajax.request(Ext.merge(request, callback));
        }
    },

    setFinal: function (callback) {
        var me = this;
        me.getProxy().getReader().readAssociated(me, {firmwareStatus: {id: 'final'}});
        me.save(callback);
    },

    deprecate: function (callback) {
        var me = this;
        me.getProxy().getReader().readAssociated(me, {firmwareStatus: {id: 'deprecated'}});
        me.save(callback);
    },

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/firmwares',
        reader: 'json',
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});