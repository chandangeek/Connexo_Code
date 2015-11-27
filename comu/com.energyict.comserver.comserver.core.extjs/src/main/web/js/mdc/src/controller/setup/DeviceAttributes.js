Ext.define('Mdc.controller.setup.DeviceAttributes', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceattributes.Setup',
        'Mdc.view.setup.deviceattributes.Edit',
        'Mdc.view.setup.deviceattributes.CustomAttributesEdit'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.DeviceAttribute',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice'
    ],

    stores: [
        'Mdc.store.UsagePointsForDeviceAttributes',
        'Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets'
    ],

    refs: [
        {ref: 'deviceAttributesDisplayForm', selector: '#device-attributes-view-form'},
        {ref: 'deviceAttributesEditForm', selector: '#device-attributes-edit-form'},
        {ref: 'deviceAttributesEditPage', selector: '#device-attributes-edit'},
        {ref: 'usagePointEmptyStoreField', selector: '#usagePointEmptyStoreField'},
        {
            ref: 'editCustomAttributePropertyForm',
            selector: '#device-custom-attributes-edit-id #device-custom-attributes-property-form'
        },
        {ref: 'deviceCustomAttributesEditView', selector: '#device-custom-attributes-edit-id'},
        {
            ref: 'restoreToDefaultBtn',
            selector: '#device-custom-attributes-edit-id  #device-custom-attributes-restore-default-btn'
        }

    ],

    init: function () {
        this.control({
            '#device-attributes-edit #deviceAttributesSaveBtn': {
                click: this.saveAttributes
            },
            '#device-attributes-edit #deviceAttributesCancelBtn': {
                click: this.goToAttributesLanding
            },
            '#device-custom-attributes-edit-id #device-custom-attributes-cancel-btn': {
                click: this.goToAttributesLanding
            },
            '#device-custom-attributes-edit-id #device-custom-attributes-restore-default-btn': {
                click: this.restoreDefaultCustomAttributes
            },
            '#device-custom-attributes-edit-id #device-custom-attributes-save-btn': {
                click: this.saveCustomAttributes
            },
            '#device-custom-attributes-edit-id #device-custom-attributes-property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            }
        });
    },

    saveCustomAttributes: function () {
        var me = this,
            form = me.getEditCustomAttributePropertyForm(),
            editView = me.getDeviceCustomAttributesEditView();

        editView.setLoading();

        form.updateRecord();
        form.getRecord().save({
            backUrl: me.getLandingUrl(),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceAttributes.saved', 'MDC', 'Device attributes saved'));
                me.goToAttributesLanding();
            },
            callback: function () {
                editView.setLoading(false);
            }
        });
    },

    showRestoreAllBtn: function (value) {
        var restoreBtn = this.getRestoreToDefaultBtn();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    },

    restoreDefaultCustomAttributes: function () {
        this.getEditCustomAttributePropertyForm().restoreAll();
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
            backUrl: me.getLandingUrl(),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceAttributes.saved', 'MDC', 'Device attributes saved'));
                me.goToAttributesLanding();
            },
            failure: function (record, operation) {
                if (operation && operation.response && operation.response.status === 400) {
                    me.formMarkInvalid(Ext.decode(operation.response.responseText));
                }
            },
            callback: function () {
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

    getLandingUrl: function () {
        return this.getController('Uni.controller.history.Router').getRoute('devices/device/attributes').buildUrl();
    },

    showDeviceAttributesView: function (mRID) {
        this.uploadAttributes('deviceAttributesSetup', mRID, true);
    },

    showEditDeviceAttributesView: function (mRID) {
        this.uploadAttributes('deviceAttributesEdit', mRID, false);
    },

    showEditCustomAttributeSetsView: function (mRID, customAttributeSetId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            model = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice'),
            widget;

        viewport.setLoading();
        model.getProxy().setUrl(mRID);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('device-custom-attributes-edit', {device: device});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);

                model.load(customAttributeSetId, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadCustomAttributeSetOnDevice', record);
                        widget.down('#custom-attribute-set-edit-panel').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [record.get('name')]));
                        widget.down('#device-custom-attributes-property-form').loadRecord(record);
                    },
                    callback: function () {
                        viewport.setLoading(false);
                    }
                })

            }
        });
    },

    uploadAttributes: function (view, mRID, showCustomAttributes) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            model = Ext.ModelManager.getModel('Mdc.model.DeviceAttribute'),
            customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets'),
            widget;

        viewport.setLoading();
        model.getProxy().setUrl(mRID);
        customAttributesStore.getProxy().setUrl(mRID);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget(view, {device: device, router: router});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);

                if (showCustomAttributes) {
                    customAttributesStore.load(function () {
                        widget.down('#custom-attribute-sets-placeholder-form-id').loadStore(this);
                    });
                }

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
        var me = this,
            device = me.getDeviceAttributesEditPage().device;

        record.beginEdit();
        record.set('device', _.pick(device.getRecordData(), 'mRID', 'version', 'parent'));
        Ext.iterate(record.getData(), function (key, value) {
            if (!Ext.isEmpty(value.available) && !Ext.isEmpty(value.editable) && value.available && value.editable) {
                var editField = me.getEditField(key);
                if (editField) {
                    if (key === 'usagePoint') {
                        record.set(key, {attributeId: editField.getValue(), available: true, editable: true});
                    } else if (me.isDateField(key)) {
                        record.set(key, {displayValue: editField.getTimeStampValue(), available: true, editable: true});
                    } else {
                        record.set(key, {displayValue: editField.getValue(), available: true, editable: true});
                    }
                }
            }
        });
        record.endEdit();
        return record;
    },

    fillEditField: function (key, value, field) {
        var me = this,
            store;

        if (me.isDateField(key)) {
            if (!Ext.isEmpty(value.displayValue)) {
                var dt = new Date(value.displayValue);
                field.setValue(dt);
            }
        } else if (key === 'usagePoint') {
            store = field.getStore();
            field.lastQuery = value.displayValue || '';
            store.getProxy().setExtraParam(field.queryParam, value.displayValue || '');
            store.load(function (records) {
                if (!records.length) {
                    field.hide();
                    me.getUsagePointEmptyStoreField().show();
                } else if (value.attributeId) {
                    field.setValue(value.attributeId);
                }
            });
        } else {
            field.setValue(value.displayValue);
        }
    },

    isDateField: function(key) {
        return (
            key === 'installationDate' ||
            key === 'deactivationDate' ||
            key === 'decommissioningDate' ||
            key === 'shipmentDate' ||
            key === 'multiplierEffectiveDate'
        );
    }
});

