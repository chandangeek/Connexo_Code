Ext.define('Mdc.controller.setup.DeviceAttributes', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceattributes.Setup',
        'Mdc.view.setup.deviceattributes.Edit'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.DeviceAttribute'
    ],

    stores: [
        'Mdc.store.UsagePointsForDeviceAttributes'
    ],

    refs: [
        {ref: 'deviceAttributesDisplayForm', selector: '#device-attributes-view-form'},
        {ref: 'deviceAttributesEditForm', selector: '#device-attributes-edit-form'},
        {ref: 'deviceAttributesEditPage', selector: '#device-attributes-edit'},
        {ref: 'usagePointEmptyStoreField', selector: '#usagePointEmptyStoreField'}

    ],

    init: function () {
        this.control({
            '#device-attributes-edit #deviceAttributesSaveBtn': {
                click: this.saveAttributes
            },
            '#device-attributes-edit #deviceAttributesCancelBtn': {
                click: this.goToAttributesLanding
            }
        });
    },

    saveAttributes: function () {
        var me = this,
            editPage = me.getDeviceAttributesEditPage(),
            editForm = me.getDeviceAttributesEditForm(),
            attributesRecord = editForm.getRecord(),
            updatedRecord;

        editPage.setLoading(true);
        updatedRecord = me.getUpdatedRecord(attributesRecord);
        editForm.getForm().clearInvalid();

        updatedRecord.save({
            callback: function (model, operation, success) {
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceAttributes.saved', 'MDC', 'Device attributes saved.'));
                    me.goToAttributesLanding();
                } else {
                    me.formMarkInvalid(Ext.decode(operation.response.responseText));
                }
                editPage.setLoading(false);
            }
        });
    },

    formMarkInvalid: function (response) {
        var me = this;

        Ext.each(response.errors, function (error) {
            var failedField = me.getEditField(error.id);

            if (failedField) {
                failedField.markInvalid(error.msg);
            }
        });
    },

    getEditField: function (key) {
        var editForm = this.getDeviceAttributesEditForm();

        if (editForm) {
            return editForm.down('#' + key + 'Edit');
        } else {
            return null
        }
    },

    getViewField: function (key) {
        var editForm = this.getDeviceAttributesEditForm();

        if (editForm) {
            return editForm.down('#' + key + 'View');
        } else {
            return null
        }
    },

    goToAttributesLanding: function () {
        this.getController('Uni.controller.history.Router').getRoute('devices/device/attributes').forward();
    },

    showDeviceAttributesView: function (mRID) {
        this.uploadAttributes('deviceAttributesSetup', mRID);
    },

    showEditDeviceAttributesView: function (mRID) {
        this.uploadAttributes('deviceAttributesEdit', mRID);
    },

    uploadAttributes: function (view, mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            model = Ext.ModelManager.getModel('Mdc.model.DeviceAttribute'),
            widget;

        viewport.setLoading();
        model.getProxy().setUrl(mRID);

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget(view, {device: device, router: router});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                model.load('attributes', {
                    success: function (attributes) {
                        if (view === 'deviceAttributesSetup') {
                            me.getDeviceAttributesDisplayForm().loadRecord(attributes);
                        } else {
                            var editForm = me.getDeviceAttributesEditForm();
                            editForm.loadRecord(attributes);
                            me.showHiddenFields(attributes);
                        }
                        viewport.setLoading(false);
                    }
                });
            }
        });
    },

    showHiddenFields: function (attributes) {
        var me = this;
        Ext.iterate(attributes.getData(), function (key, value) {
            if (!Ext.isEmpty(value.available) && !Ext.isEmpty(value.editable) && value.available) {
                if (value.editable) {
                    var editField = me.getEditField(key);
                    if (editField) {
                        editField.show();
                        me.fillEditField(key, value, editField);
                    }
                } else {
                    var viewField = me.getViewField(key);
                    if (viewField) {
                        viewField.show();
                    }
                }
            }
        });
    },

    getUpdatedRecord: function (record) {
        var me = this;

        Ext.iterate(record.getData(), function (key, value) {
            if (!Ext.isEmpty(value.available) && !Ext.isEmpty(value.editable) && value.available && value.editable) {
                var editField = me.getEditField(key);
                if (editField) {
                    if (key === 'usagePoint') {
                        record.set(key, {attributeId: editField.getValue(), available: true, editable: true});
                    } else if (key === 'installationDate' || key === 'deactivationDate' || key === 'decommissioningDate' || key === 'shipmentDate') {
                        record.set(key, {displayValue: editField.getTimeStampValue(), available: true, editable: true});
                    } else {
                        record.set(key, {displayValue: editField.getValue(), available: true, editable: true});
                    }
                }
            }
        });
        return record;
    },

    fillEditField: function (key, value, field) {
        var me = this;

        if (key === 'installationDate' || key === 'deactivationDate' || key === 'decommissioningDate' || key === 'shipmentDate') {
            if (!Ext.isEmpty(value.displayValue)) {
                var dt = new Date(value.displayValue);
                field.setValue(dt);
            }
        } else if (key === 'usagePoint') {
            field.getStore().load({
                callback: function () {
                    if (field.getStore().getCount() === 0) {
                        field.hide();
                        me.getUsagePointEmptyStoreField().show();
                    }

                    if (value.attributeId) {
                        field.setValue(value.attributeId);
                    }
                }
            });
        } else {
            field.setValue(value.displayValue);
        }
    }

});

