Ext.define('Mdc.controller.setup.DeviceCommunicationProtocols', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.store.DeviceCommunicationProtocolsPaged'
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
        this.getDeviceCommunicationProtocolsPagedStore().on('load', this.onDeviceCommunicationProtocolsStoreLoad, this);
        this.control({
            '#devicecommunicationprotocolgrid': {
                selectionchange: this.previewDeviceCommunicationProtocol
            },
            '#devicecommunicationprotocolgrid actioncolumn': {
                editProtocol: this.editDeviceCommunicatonProtocolHistory
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
                    var form = me.getDeviceCommunicationProtocolPreview().down('property-form');
                    if (deviceCommunicationProtocol.properties().count()) {
                        form.loadRecord(deviceCommunicationProtocol);
                        form.show();
                    } else {
                        form.hide();
                    }
                }
            });
        } else {
            this.getDeviceCommunicationProtocolPreview().getLayout().setActiveItem(0);
        }
    },

    onDeviceCommunicationProtocolsStoreLoad: function () {
        if (this.getDeviceCommunicationProtocolsPagedStore().getCount() > 0) {
            this.getDeviceCommunicationProtocolGrid().getSelectionModel().select(0);
        }
    },

    editDeviceCommunicatonProtocolHistory: function (item) {
        location.href = '#/administration/devicecommunicationprotocols/' + encodeURIComponent(item.get('id')) + '/edit';
    },

    editDeviceCommunicationProtocolHistoryFromPreview: function () {
        location.href = '#/administration/devicecommunicationprotocols/' + encodeURIComponent(this.getDeviceCommunicationProtocolGrid().getSelectionModel().getSelection()[0].get('id')) + '/edit';
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
                me.getApplication().fireEvent('loadDeviceCommunicationProtocol', protocol);
                widget.down('form').loadRecord(protocol);
                widget.down('#deviceCommunicationProtocolEditCreateTitle').setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + protocol.get('name') + '\'');
                var form = widget.down('property-form');
                if (protocol.properties().count()) {
                    form.show();
                    form.loadRecord(protocol);
                } else {
                    form.hide();
                }

                widget.setLoading(false);
            }
        });
    },

    editDeviceCommunicationProtocol: function () {
        var record = this.getDeviceCommunicationProtocolEditForm().getRecord(),
            values = this.getDeviceCommunicationProtocolEditForm().getValues(),
            propertyForm = this.getDeviceCommunicationProtocolEditView().down('property-form'),
            me = this;

        if (record) {
            record.set(values);
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();

            record.save({
                success: function (record) {
                    location.href = '#/administration/devicecommunicationprotocols/';
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicecommunicationprotocol.acknowlegment', 'MDC', 'Protocol saved') );
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceCommunicationProtocolEditForm().getForm().markInvalid(json.errors);
                        propertyForm.getForm().markInvalid(json.errors);
                    }
                }
            });
        }
    }
});
