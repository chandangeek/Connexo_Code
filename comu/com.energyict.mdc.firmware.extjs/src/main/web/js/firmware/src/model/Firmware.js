Ext.define('Fwc.model.Firmware', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'version', type: 'string', useNull: true},
        {name: 'type', type: 'string', useNull: true},
        {name: 'status', type: 'string', useNull: true},
        {name: 'file', useNull: true},
        {name: 'fileSize', type: 'number', useNull: true}
    ],

    doValidate: function (callback) {
        Ext.Ajax.request({
            method: 'POST',
            url: this.proxy.url + '/validate',
            success: callback,
            jsonData: {
                version: this.get('version'),
                type: this.get('type'),
                status: this.get('status'),
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