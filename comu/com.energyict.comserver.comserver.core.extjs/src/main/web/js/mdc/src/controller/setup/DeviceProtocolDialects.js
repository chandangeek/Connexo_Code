Ext.define('Mdc.controller.setup.DeviceProtocolDialects', {
    extend: 'Ext.app.Controller',
    mRID: null,
    requires: [
        'Mdc.store.ProtocolDialectsOfDevice'
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
        {ref: 'protocolDialectsDetailsTitle', selector: '#protocolDialectsDetailsTitle'},
        {ref: 'restoreAllButton', selector: '#deviceProtocolDialectEdit #restoreAllButton'}
    ],

    init: function () {
        //this.getDeviceProtocolDialectEditForm().on('enableRestoreAll')
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
            },
            '#deviceProtocolDialectEdit #restoreAllButton[action=restoreAll]': {
                click: this.restoreAllDefaults
            },
            '#deviceProtocolDialectEdit property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            }
        });
    },

    editProtocolDialectHistory: function (record) {
        location.href = '#/devices/' + encodeURIComponent(this.mRID) + '/protocols/' + encodeURIComponent(record.get('id')) + '/edit';
    },

    showProtocolDialectsView: function (mRID) {
        var me = this,
        viewport = Ext.ComponentQuery.query('viewport')[0];
        this.mRID = mRID;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget = Ext.widget('deviceProtocolDialectSetup', {device: device});
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                me.getDeviceProtocolDialectsGrid().getSelectionModel().doSelect(0);
            }

        });
    },

    previewProtocolDialect: function (grid, record) {
        var me = this;
        var protocolDialect = this.getDeviceProtocolDialectsGrid().getSelectionModel().getSelection();
        if (protocolDialect.length === 1) {
            var protocolDialectId = protocolDialect[0].get('id');
            var model = Ext.ModelManager.getModel('Mdc.model.DeviceProtocolDialect');
            model.getProxy().extraParams = ({mRID: encodeURIComponent(me.mRID)});
            model.load(protocolDialectId, {
                    success: function (deviceProtocolDialect) {
                        me.getDeviceProtocolDialectPreviewForm().loadRecord(deviceProtocolDialect);
                        var protocolDialectName = deviceProtocolDialect.get('name');
                        me.getDeviceProtocolDialectPreview().getLayout().setActiveItem(1);
                        if (deviceProtocolDialect.propertiesStore.data.items.length > 0) {
                            me.getProtocolDialectsDetailsTitle().setVisible(true);
                        } else {
                            me.getProtocolDialectsDetailsTitle().setVisible(false);
                        }
                        me.getDeviceProtocolDialectPreview().down('property-form').loadRecord(deviceProtocolDialect);
                        me.getDeviceProtocolDialectPreview().setTitle(protocolDialectName);
                    }
                }
            );
        } else {
            me.getDeviceProtocolDialectPreview().getLayout().setActiveItem(0);
        }
    },

    editProtocolDialectHistoryFromPreview: function () {
        this.editProtocolDialectHistory(this.getDeviceProtocolDialectPreviewForm().getRecord());
    },

    showProtocolDialectsEditView: function (mRID, protocolDialectId) {
        this.mRID = mRID;
        var me = this;
        var returnlink;

        if (me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens().indexOf('null') > -1) {
            returnlink = '#/devices/' + encodeURIComponent(mRID) + '/protocols';
        } else {
            returnlink = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();
        }


        var model = Ext.ModelManager.getModel('Mdc.model.DeviceProtocolDialect');
        model.getProxy().extraParams = ({mRID: encodeURIComponent(mRID)});
        model.load(protocolDialectId, {
            success: function (protocolDialect) {
                me.getApplication().fireEvent('loadDeviceProtocolDialect', protocolDialect);
                Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
                    success: function (device) {
                        var widget = Ext.widget('deviceProtocolDialectEdit', {
                            edit: true,
                            returnLink: returnlink,
                            device: device
                        });

                        widget.setLoading(true);
                        me.getApplication().fireEvent('loadDevice', device);
                        widget.down('form').loadRecord(protocolDialect);
                        widget.down('property-form').loadRecord(protocolDialect);
                        widget.down('#deviceProtocolDialectEditAddTitle').setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + protocolDialect.get('name') + '\'');
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

        var propertyForm = me.getDeviceProtocolDialectEditView().down('property-form');
        if (record) {
            record.set(values);
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();
            record.save({
                success: function (record) {
                    location.href = '#/devices/' + encodeURIComponent(me.mRID) + '/protocols';
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceProtocolDialect.acknowlegment', 'MDC', 'Protocol dialect saved') );
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceProtocolDialectEditForm().getForm().markInvalid(json.errors);
                        propertyForm.getForm().markInvalid(json.errors);
                    }
                }
            });
        }
    },

    showRestoreAllBtn: function(value) {
        var restoreBtn = this.getRestoreAllButton();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    },


    restoreAllDefaults: function () {
        var me = this;
        me.getDeviceProtocolDialectEditView().down('property-form').restoreAll();
        me.getRestoreAllButton().disable();
    }
});

