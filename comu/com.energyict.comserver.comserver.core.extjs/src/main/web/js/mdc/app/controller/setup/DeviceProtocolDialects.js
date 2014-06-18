Ext.define('Mdc.controller.setup.DeviceProtocolDialects', {
    extend: 'Ext.app.Controller',
    mRID: null,
    requires: [
        'Mdc.store.ProtocolDialectsOfDevice',
        'Mdc.controller.setup.Properties',
        'Mdc.controller.setup.PropertiesView'
    ],

    views: [
        'setup.deviceprotocol.DeviceProtocolDialectSetup',
        'setup.deviceprotocol.DeviceProtocolDialectsGrid',
        'setup.deviceprotocol.DeviceProtocolDialectPreview',
        'setup.deviceprotocol.DeviceProtocolDialectEdit'
    ],

    stores: [
        'ProtocolDialectsOfDevice'
    ],

    refs: [
        {ref: 'deviceProtocolDialectsGrid', selector: '#deviceprotocoldialectsgrid'},
        {ref: 'deviceProtocolDialectPreviewForm', selector: '#deviceProtocolDialectPreviewForm'},
        {ref: 'deviceProtocolDialectPreview', selector: '#deviceProtocolDialectPreview'},
        {ref: 'deviceProtocolDialectPreviewTitle', selector: '#deviceProtocolDialectPreviewTitle'},
        {ref: 'deviceProtocolDialectEditView', selector: '#deviceProtocolDialectEdit'},
        {ref: 'deviceProtocolDialectEditForm', selector: '#deviceProtocolDialectEditForm'},
        {ref: 'editProtocolDialectsDetailsTitle', selector: '#editProtocolDialectsDetailsTitle'},
        {ref: 'protocolDialectsDetailsTitle', selector: '#protocolDialectsDetailsTitle'}
    ],

    init: function () {
        this.control({
            '#deviceprotocoldialectsgrid': {
                selectionchange: this.previewProtocolDialect
            },
            '#deviceprotocoldialectsgrid actioncolumn': {
                editProtocolDialect: this.editProtocolDialectHistory
            },
            '#deviceProtocolDialectPreview menuitem[action=editDeviceProtocolDialect]': {
                click: this.editProtocolDialectHistoryFromPreview
            },
            '#addEditButton[action=editDeviceProtocolDialect]': {
                click: this.editProtocolDialect
            }
        });
    },

    editProtocolDialectHistory: function (record) {
        location.href = '#/devices/' + this.mRID + '/protocols/' + record.get('id') + '/edit';
    },

    showProtocolDialectsView: function (mRID) {
        var me = this;
        this.mRID = mRID;
        var widget = Ext.widget('deviceProtocolDialectSetup', {mRID: mRID});
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getDeviceProtocolDialectsGrid().getSelectionModel().doSelect(0);
            }

        });
    },

    previewProtocolDialect: function (grid, record) {
        var protocolDialect = this.getDeviceProtocolDialectsGrid().getSelectionModel().getSelection();
        if (protocolDialect.length === 1) {
            this.getDeviceProtocolDialectPreviewForm().loadRecord(protocolDialect[0]);
            var protocolDialectName = protocolDialect[0].get('name');
            this.getDeviceProtocolDialectPreview().getLayout().setActiveItem(1);
            if (protocolDialect[0].propertiesStore.data.items.length > 0) {
                this.getProtocolDialectsDetailsTitle().setVisible(true);
            } else {
                this.getProtocolDialectsDetailsTitle().setVisible(false);
            }
            this.getPropertiesViewController().showProperties(protocolDialect[0], this.getDeviceProtocolDialectPreview());
            this.getDeviceProtocolDialectPreview().setTitle(protocolDialectName);
        } else {
            this.getDeviceProtocolDialectPreview().getLayout().setActiveItem(0);
        }
    },

    getPropertiesViewController: function () {
        return this.getController('Mdc.controller.setup.PropertiesView');
    },

    getPropertiesController: function () {
        return this.getController('Mdc.controller.setup.Properties');
    },

    editProtocolDialectHistoryFromPreview: function () {
        this.editProtocolDialectHistory(this.getDeviceProtocolDialectPreviewForm().getRecord());
    },

    showProtocolDialectsEditView: function (mRID, protocolDialectId) {
        this.mRID = mRID;
        var me = this;
        var returnlink;

        if (me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens().indexOf('null') > -1) {
            returnlink = '#/devices/' + mRID + '/protocols';
        } else {
            returnlink = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();
        }

        var widget = Ext.widget('deviceProtocolDialectEdit', {
            edit: true,
            returnLink: returnlink
        });

        widget.setLoading(true);
        var model = Ext.ModelManager.getModel('Mdc.model.DeviceProtocolDialect');
        model.getProxy().extraParams = ({mRID: mRID});
        model.load(protocolDialectId, {
            success: function (protocolDialect) {
                Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
                    success: function (device) {
                        me.getApplication().fireEvent('loadDevice', device);
                        widget.down('form').loadRecord(protocolDialect);
                        me.getPropertiesController().showProperties(protocolDialect, widget);
                        widget.down('#deviceProtocolDialectEditAddTitle').update('<h1>' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + protocolDialect.get('name') + '</h1>');
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.setLoading(false);
                    }
                });
            }
        });
    },

    editProtocolDialect: function () {
        var record = this.getDeviceProtocolDialectEditForm().getRecord(),
            values = this.getDeviceProtocolDialectEditForm().getValues(),
            me = this;

        if (record) {
            record.set(values);
            record.propertiesStore = me.getPropertiesController().updateProperties();
            record.save({
                success: function (record) {
                    location.href = '#/devices/' + me.mRID + '/protocols';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceProtocolDialectEditForm().getForm().markInvalid(json.errors);
                        me.getPropertiesController().showErrors(json.errors);
                    }
                }
            });
        }
    }

});

