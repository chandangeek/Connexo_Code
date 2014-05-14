Ext.define('Mdc.controller.setup.DeviceCommunicationProtocols', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
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
        {ref: 'deviceCommunicationProtocolEditForm', selector: '#deviceCommunicationProtocolEditForm'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'}
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
            }, '#deviceCommunicationProtocolSetup breadcrumbTrail': {
                afterrender: this.overviewBreadCrumb
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
        location.href = '#setup/devicecommunicationprotocols/' + item.get('id') + '/edit';
    },

    editDeviceCommunicationProtocolHistoryFromPreview: function () {
        location.href = '#setup/devicecommunicationprotocols/' + this.getDeviceCommunicationProtocolGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    showDeviceCommunicationProtocolEditView: function (deviceCommunicationProtocol) {
        console.log('show device communication protocol edit');
        var widget = Ext.widget('deviceCommunicationProtocolEdit', {
            edit: true
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationProtocol').load(deviceCommunicationProtocol, {
            success: function (protocol) {
                me.editBreadCrumb(protocol.get('name'), deviceCommunicationProtocol);
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
                    location.href = '#setup/devicecommunicationprotocols/';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceCommunicationProtocolEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });
        }
    },

    overviewBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('deviceCommunicationProtocols.protocols', 'MDC', 'Protocols'),
            href: 'devicecommunicationprotocols'
        });

        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    editBreadCrumb: function (registerTypeName, registerTypeId) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('deviceCommunicationProtocol.prototocls', 'MDC', 'Protocols'),
            href: 'devicecommunicationprotocols'
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('deviceCommunicationProtocol.edit', 'MDC', 'Edit protocol'),
            href: 'edit'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    }

});
