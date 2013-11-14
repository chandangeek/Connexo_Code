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
    refs: [
        {
            ref: 'deviceCommunicationProtocolGrid',
            selector: 'viewport #devicecommunicationprotocolgrid'
        }
    ],

    init: function () {
        this.control({
            'setupDeviceCommunicationProtocols': {
                itemdblclick: this.editDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=save]': {
                click: this.updateDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=cancel]': {
                click: this.cancelDeviceCommunicationProtocol
            },
            'setupDeviceCommunicationProtocols button[action=add]': {
                click: this.add
            },
            'setupDeviceCommunicationProtocols button[action=delete]': {
                click: this.delete
            }
        });
    },

    showEditView: function (id) {
        var view = Ext.widget('deviceCommunicationProtocolEdit');
        Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationProtocol').load(id, {
            success: function (deviceCommunicationProtocol) {
                view.down('form').loadRecord(deviceCommunicationProtocol);
                if (id != undefined) {
                    var deviceCommunicationProtocolId = view.down('#deviceCommunicationProtocolId');
                    deviceCommunicationProtocolId.hidden = false;
                }
                Mdc.getApplication().getMainController().showContent(view);
            }
        });
    },

    editDeviceCommunicationProtocol: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('devicecommunicationprotocols', record.getId());
        Ext.History.add(url);
    },
    updateDeviceCommunicationProtocol: function (button) {
        var me = this;
        var pnl = button.up('panel') ,
            form = pnl.down('form'),
            record = form.getRecord() || Ext.create(Mdc.model.DeviceCommunicationProtocol),
            values = form.getValues();

        record.set(values);
        record.save({
            success: function (record, operation) {
                me.showComServerOverview();
            }
        });
    },

    showDeviceCommunicationProtocolOverview: function () {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('devicecommunicationprotocols');
        Ext.History.add(url);
    },

    cancelDeviceCommunicationProtocol: function () {
        this.showDeviceCommunicationProtocolOverview();
    },

    add: function () {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeAddDeviceCommunicationProtocol();
        Ext.History.add(url);
    },

    delete: function () {
        var recordArray = this.getDeviceCommunicationProtocolGrid().getSelectionModel().getSelection();
        if (recordArray.length > 0) {
            recordArray[0].destroy();
        }
    }
});
