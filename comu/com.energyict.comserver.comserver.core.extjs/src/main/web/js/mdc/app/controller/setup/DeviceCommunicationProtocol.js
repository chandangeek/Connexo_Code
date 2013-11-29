Ext.define('Mdc.controller.setup.DeviceCommunicationProtocol', {
    extend: 'Ext.app.Controller',

    stores: [
        'DeviceCommunicationProtocols',
        'LicensedProtocols'
    ],

    requires: [
        'Mdc.store.LicensedProtocols',
        'Mdc.store.DeviceCommunicationProtocols'
    ],

    models: [
        'DeviceCommunicationProtocol',
        'LicensedProtocol',
        'ProtocolFamily'
    ],

    views: [
        'setup.devicecommunicationprotocol.List',
        'setup.devicecommunicationprotocol.Edit',
        'setup.protocolfamily.List'
    ],
    refs: [
        {
            ref: 'deviceCommunicationProtocolGrid',
            selector: 'viewport #devicecommunicationprotocolgrid'
        },
        {
            ref: 'deviceCommunicationProtocolEdit',
            selector: 'viewport #deviceCommunicationProtocolEdit'
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
            },
            'deviceCommunicationProtocolEdit combobox': {
                change: this.onChangeLicensedProtocol
            }
        });
    },

    showEditView: function (id) {
        var me = this;
        var view = Ext.widget('deviceCommunicationProtocolEdit');
        Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationProtocol').load(id, {
            success: function (deviceCommunicationProtocol) {
                view.down('#devicecommunicationprotocolform').loadRecord(deviceCommunicationProtocol);
                me.getLicensedProtocolsStore().load({
                    params: {

                    },
                    callback: function () {
                        if (id != undefined) {
                            var deviceCommunicationProtocolId = view.down('#deviceCommunicationProtocolId');
                            deviceCommunicationProtocolId.hidden = false;
                            var licensedProtocol = deviceCommunicationProtocol.getLicensedProtocol();
                            console.log(licensedProtocol);
                            view.down('#licensedProtocol').setValue(licensedProtocol.data.protocolName);
                            view.down('#protocolJavaClassName').setValue(licensedProtocol.data.protocolJavaClassName);
                            //var licProtocol = me.getLicensedProtocolsStore().findRecord('licensedProtocolRuleCode', licensedProtocol.licensedProtocolRuleCode);
                            view.down('#protocolfamilygrid').reconfigure(licensedProtocol.protocolFamiliesStore);
                        }
                        Mdc.getApplication().getMainController().showContent(view);
                    }
                });
            }
        });
    },

    onChangeLicensedProtocol: function (field, value, options) {
        var me = this;
        var view = this.getDeviceCommunicationProtocolEdit();
        if (view != undefined && field.name === 'licensedProtocol') {
            var licensedProtocol = me.getLicensedProtocolsStore().findRecord('licensedProtocolRuleCode', value);
            if (licensedProtocol != null) {
                view.down('#protocolJavaClassName').setValue(licensedProtocol.data.protocolJavaClassName);
                view.down('#protocolfamilygrid').reconfigure(licensedProtocol.protocolFamiliesStore);
            }
        }
    },

    editDeviceCommunicationProtocol: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('devicecommunicationprotocols', record.getId());
        Ext.History.add(url);
    },
    updateDeviceCommunicationProtocol: function (button) {
        var me = this;
        var view = this.getDeviceCommunicationProtocolEdit();
        var form = view.down('#devicecommunicationprotocolform'),
            record = form.getRecord() || Ext.create(Mdc.model.DeviceCommunicationProtocol),
            values = form.getValues();

        record.set(values);
        var licensedProtocol = me.getLicensedProtocolsStore().findRecord('licensedProtocolRuleCode', values.licensedProtocol);
        record.setLicensedProtocol(licensedProtocol);

        record.save({
            success: function (record, operation) {
                record.commit();
                me.getDeviceCommunicationProtocolsStore().reload({
                    params: {

                    },
                    callback: function () {
                        me.showDeviceCommunicationProtocolOverview();
                    }
                });
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
})
;
