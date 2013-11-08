Ext.define('Mdc.controller.setup.DeviceCommunicationProtocol', {
    extend: 'Ext.app.Controller',

    stores: [
        'DeviceCommunicationProtocols'
    ],

    models: [
        'DeviceCommunicationProtocol'
    ],

    views: [
        'setup.devicecommunicationprotocol.List',
        'setup.devicecommunicationprotocol.Edit'
    ],

    init: function () {
        this.control({
            '#setupDeviceCommunicationProtocols': {
                itemdblclick: this.editDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=save]': {
                click: this.updateDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=cancel]': {
                click: this.cancelDeviceCommunicationProtocol
            }
        });
    },

    showEditView: function (id) {
        var view = Ext.widget('deviceCommunicationProtocolEdit');
        Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationProtocol').load(id, {
            success: function (deviceCommunicationProtocol) {
                view.down('form').loadRecord(deviceCommunicationProtocol);
                Mdc.getApplication().getMainController().showContent(view);
            }
        });
    },

    editDeviceCommunicationProtocol: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('devicecommunicationprotocols', record.getId());
        Ext.History.add(url);
    },
    updateDeviceCommunicationProtocol: function (button) {
        var win = button.up('panel') ,
            form = win.down('form'),
            record = form.getRecord(),
            values = form.getValues();

        record.set(values);
        record.save();
    },

    cancelDeviceCommunicationProtocol: function () {
        console.log('cancel');
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('devicecommunicationprotocols');
        Ext.History.add(url);
    }
});
