Ext.define('Mdc.controller.setup.DeviceCommunicationProtocols', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.controller.setup.PropertiesView',
        'Mdc.controller.setup.Properties'
    ],

    views: [
        'setup.devicecommunicationprotocol.DeviceCommunicationProtocolFilter',
        'setup.devicecommunicationprotocol.DeviceCommunicationProtocolSetup',
        'setup.devicecommunicationprotocol.DeviceCommunicationProtocolGrid',
        'setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview',
        'setup.devicecommunicationprotocol.DeviceCommunicationProtocolEdit'
    ],

    stores: [
        'DeviceCommunicationProtocolsPaged'
    ],

    refs: [
        {ref: 'deviceCommunicationProtocolGrid', selector: '#devicecommunicationprotocolgrid'},
        {ref: 'deviceCommunicationProtocolPreviewForm', selector: '#deviceCommunicationProtocolPreviewForm'},
        {ref: 'deviceCommunicationProtocolPreview', selector: '#deviceCommunicationProtocolPreview'},
        {ref: 'deviceCommunicationProtocolEditView', selector: '#deviceCommunicationProtocolEdit'},
        {ref: 'deviceCommunicationProtocolEditForm', selector: '#deviceCommunicationProtocolEditForm'}
    ],

    init: function () {

        this.control({
            '#devicecommunicationprotocolgrid': {
                selectionchange: this.previewDeviceCommunicationProtocol
            },
            '#devicecommunicationprotocolgrid actioncolumn': {
                editItem: this.editDeviceCommunicatonProtocolHistory
            },
            '#deviceCommunicationProtocolPreview menuitem[action=editProtocol]': {
                click: this.editDeviceCommunicationProtocolHistoryFromPreview
            },
            '#createEditButton[action=editDeviceCommunicationProtocol]': {
                click: this.editDeviceCommunicationProtocol
            }
        });
    },

    previewDeviceCommunicationProtocol: function (grid, record) {
        var deviceCommunicationProtocols = this.getDeviceCommunicationProtocolGrid().getSelectionModel().getSelection();
        var me = this;
        if (deviceCommunicationProtocols.length === 1) {
            Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationProtocol').load(deviceCommunicationProtocols[0].get('id'), {
                success: function (deviceCommunicationProtocol) {
                    me.getDeviceCommunicationProtocolPreviewForm().loadRecord(deviceCommunicationProtocol);
                    me.getDeviceCommunicationProtocolPreview().getLayout().setActiveItem(1);
                    me.getDeviceCommunicationProtocolPreview().setTitle(deviceCommunicationProtocol.get('name'));
                    me.getPropertiesViewController().showProperties(deviceCommunicationProtocol, me.getDeviceCommunicationProtocolPreview());
                }
            });
        } else {
            this.getDeviceCommunicationProtocolPreview().getLayout().setActiveItem(0);
        }
    },

    getPropertiesViewController: function () {
        return this.getController('Mdc.controller.setup.PropertiesView');
    },

    getPropertiesController: function () {
        return this.getController('Mdc.controller.setup.Properties');
    },


    editDeviceCommunicatonProtocolHistory: function (item) {
        location.href = '#/administration/devicecommunicationprotocols/' + item.get('id') + '/edit';
    },

    editDeviceCommunicationProtocolHistoryFromPreview: function () {
        location.href = '#/administration/devicecommunicationprotocols/' + this.getDeviceCommunicationProtocolGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    showDeviceCommunicationProtocolEditView: function (deviceCommunicationProtocol) {
        var widget = Ext.widget('deviceCommunicationProtocolEdit', {
            edit: true
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationProtocol').load(deviceCommunicationProtocol, {
            success: function (protocol) {
                widget.down('form').loadRecord(protocol);
                widget.down('#deviceCommunicationProtocolEditCreateTitle').update('<h1>' + protocol.get('name') + ' > ' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + Uni.I18n.translate('deviceCommunicationProtocol.protocol', 'MDC', 'Protocol') + '</h1>');
                me.getPropertiesController().showProperties(protocol, widget, true);
                widget.setLoading(false);
            }
        });
    },

    editDeviceCommunicationProtocol: function () {
        var record = this.getDeviceCommunicationProtocolEditForm().getRecord(),
            values = this.getDeviceCommunicationProtocolEditForm().getValues(),
            me = this;

        if (record) {
            record.set(values);
            record.propertiesStore = me.getPropertiesController().updateProperties();
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicecommunicationprotocols/';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceCommunicationProtocolEditForm().getForm().markInvalid(json.errors);
                        me.getPropertiesController().showErrors(json.errors);
                    }
                }
            });
        }
    },
});
