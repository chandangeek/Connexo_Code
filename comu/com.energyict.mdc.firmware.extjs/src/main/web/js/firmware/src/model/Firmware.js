Ext.define('Fwc.model.Firmware', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'firmwareVersion', type: 'string', useNull: true},
        {name: 'firmwareType', type: 'string', useNull: true},
        {name: 'firmwareStatus', type: 'string', useNull: true},
        {name: 'firmwareFile', useNull: true},
        {name: 'fileSize', type: 'number', useNull: true}
    ],

    doValidate: function (callback) {
        Ext.Ajax.request({
            method: 'POST',
            url: this.proxy.url + '/validate',
            callback: callback,
            jsonData: {
                firmwareVersion: this.get('firmwareVersion'),
                firmwareType: this.get('firmwareType'),
                firmwareStatus: this.get('firmwareStatus'),
                fileSize: this.get('fileSize')
            }
        });
    },

    setFinal: function (callback) {
        var me = this;
        me.set('status', 'final');
        me.save(callback);
    },

    deprecate: function (callback) {
        this.destroy(callback);
    },

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/firmwares',
        reader: {
            type: 'json',
            root: 'firmwares',
            totalProperty: 'total'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});