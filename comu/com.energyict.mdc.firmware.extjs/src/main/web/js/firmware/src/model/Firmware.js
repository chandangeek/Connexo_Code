Ext.define('Fwc.model.Firmware', {
    extend: 'Ext.data.Model',
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
            associationKey: 'firmwareStatus'
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

    setFinal: function (callback) {
        var me = this;
        me.getProxy().getReader().readAssociated(me, {firmwareStatus: {id: 'final'}});
        me.save(callback);
    },

    deprecate: function (callback) {
        this.destroy(callback);
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