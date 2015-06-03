Ext.define('Mdc.controller.setup.DeviceSecuritySettings', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.SecuritySettingsOfDevice'
    ],

    views: [
        'setup.devicesecuritysettings.DeviceSecuritySettingSetup',
        'setup.devicesecuritysettings.DeviceSecuritySettingGrid',
        'setup.devicesecuritysettings.DeviceSecuritySettingPreview',
        'setup.devicesecuritysettings.DeviceSecuritySettingEdit'
    ],

    stores: [
        'SecuritySettingsOfDevice'
    ],

    refs: [
        {ref: 'deviceSecuritySettingGrid', selector: '#devicesecuritysettinggrid'},
        {ref: 'deviceSecuritySettingPreview', selector: '#deviceSecuritySettingPreview'},
        {ref: 'deviceSecuritySettingPreviewTitle', selector: '#deviceSecuritySettingDetailsTitle'},
        {ref: 'deviceSecuritySettingPreviewForm', selector: '#deviceSecuritySettingPreviewForm'},
        {ref: 'deviceSecuritySettingEditView', selector: '#deviceSecuritySettingEdit'},
        {ref: 'deviceSecuritySettingEditForm', selector: '#deviceSecuritySettingEditForm'},
        {ref: 'deviceSecuritySettingComboBox', selector: '#deviceSecuritySettingComboBox'},
        {ref: 'deviceSecuritySettingDetailTitle', selector: '#deviceSecuritySettingDetailsTitle'},
        {ref: 'restoreAllButton', selector: '#restoreAllButton'},
        {ref: 'addEditButton', selector: '#addEditButton'},
        {ref: 'showValueDeviceSecuritySetting', selector: '#showValueDeviceSecuritySetting'},
        {ref: 'hideValueDeviceSecuritySetting', selector: '#hideValueDeviceSecuritySetting'}
    ],

    init: function () {
        this.control({
            '#devicesecuritysettinggrid': {
                selectionchange: this.previewDeviceSecuritySetting
            },
            '#devicesecuritysettinggrid actioncolumn': {
                editDeviceSecuritySetting: this.editDeviceSecuritySettingHistory,
                showValueDeviceSecuritySetting: this.showValues,
                hideValueDeviceSecuritySetting:  this.hideValues
            },
            '#deviceSecuritySettingPreview menuitem[action=editDeviceSecuritySetting]': {
                click: this.editDeviceSecuritySettingHistoryFromPreview
            },
            '#deviceSecuritySettingEdit #addEditButton[action=editDeviceSecuritySetting]': {
                click: this.editDeviceSecuritySetting
            },
            '#deviceSecuritySettingEdit #restoreAllButton[action=restoreAll]': {
                click: this.restoreAllDefaults
            },
            '#deviceSecuritySettingEdit property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            },
            '#deviceSecuritySettingPreview menuitem[action=showValueDeviceSecuritySetting]': {
                click: this.showValues
            },
            '#deviceSecuritySettingPreview menuitem[action=hideValueDeviceSecuritySetting]': {
                click: this.hideValues
            },
            '#deviceSecuritySettingEdit checkbox' :{
                change: this.showValueInEdit
            }
        });
    },

    showValues: function() {
        this.showPropertyValues(true);
    },

    hideValues: function() {
        this.showPropertyValues(false);
    },

    showDeviceSecuritySettings: function (mrid) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        this.mrid = mrid;

        viewport.setLoading();

        var securitySettingsOfDeviceStore = Ext.StoreManager.get('SecuritySettingsOfDevice');
        securitySettingsOfDeviceStore.getProxy().setExtraParam('mrid', encodeURIComponent(mrid));
        Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
            success: function (device) {
                var widget = Ext.widget('deviceSecuritySettingSetup', {device: device, mrid: mrid});
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                securitySettingsOfDeviceStore.load({
                    callback: function () {
                        me.getDeviceSecuritySettingGrid().getSelectionModel().doSelect(0);

                    }
                });

            }
        });


    },


    previewDeviceSecuritySetting: function () {
        var me = this;
        var deviceSecuritySetting = me.getDeviceSecuritySettingGrid().getSelectionModel().getSelection();
        me.getDeviceSecuritySettingPreview().down('property-form').remove();
        if (deviceSecuritySetting.length == 1) {
            var deviceSecuritySettingName = deviceSecuritySetting[0].get('name');
            me.getDeviceSecuritySettingPreview().getLayout().setActiveItem(1);
            me.getDeviceSecuritySettingPreview().setTitle(Ext.String.htmlEncode(deviceSecuritySettingName));
            me.getDeviceSecuritySettingPreviewForm().loadRecord(deviceSecuritySetting[0]);
            if (deviceSecuritySetting[0].propertiesStore.data.items.length > 0) {
                me.getDeviceSecuritySettingPreview().down('property-form').readOnly = true;
                me.getDeviceSecuritySettingPreview().down('property-form').loadRecord(deviceSecuritySetting[0]);
                if (deviceSecuritySetting[0].get('userHasViewPrivilege') || deviceSecuritySetting[0].get('userHasEditPrivilege')) {
                    me.getDeviceSecuritySettingPreviewTitle().setVisible(true);
                } else {
                    me.getDeviceSecuritySettingPreviewTitle().setVisible(false);
                }
                if (!deviceSecuritySetting[0].get('userHasEditPrivilege')) {
                    me.getDeviceSecuritySettingPreview().getHeader().down('button').hide();
                } else {
                    me.getDeviceSecuritySettingPreview().getHeader().down('button').setVisible(true);
                    if (deviceSecuritySetting[0].get('userHasViewPrivilege')) {
                        me.showPropertyValues(false);
                    }
                }
            } else {
                me.getDeviceSecuritySettingPreviewTitle().setVisible(false);
            }
            me.getDeviceSecuritySettingPreview().setTitle(Ext.String.htmlEncode(deviceSecuritySettingName));
        } else {
            me.getDeviceSecuritySettingPreview().getLayout().setActiveItem(0);
        }
    },

    editDeviceSecuritySettingHistory: function (record) {
        location.href = '#/devices/' + encodeURIComponent(this.mrid) + '/securitysettings/' + encodeURIComponent(record.get('id')) + '/edit';
    },

    editDeviceSecuritySettingHistoryFromPreview: function () {
        this.editDeviceSecuritySettingHistory(this.getDeviceSecuritySettingPreviewForm().getRecord());

    },

    editDeviceSecuritySetting: function () {
        var record = this.getDeviceSecuritySettingEditForm().getRecord(),
            values = this.getDeviceSecuritySettingEditForm().getValues(),
            propertyForm = this.getDeviceSecuritySettingEditView().down('property-form'),
            me = this;
        record.set('saveAsIncomplete', false);
        me.saveRecord(record, values, propertyForm);

    },

    saveRecord: function (record, values, propertyForm) {
        var me = this;
        record.getProxy().extraParams = ({mrid: me.mrid});
        if (propertyForm) {
            propertyForm.updateRecord(record);
            record.propertiesStore = propertyForm.getRecord().properties();
        }
        record.save({
            success: function (record) {
                location.href = '#/devices/' + encodeURIComponent(me.mrid) + '/securitysettings/';
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicesecuritysetting.saveSuccess.msg.edit', 'MDC', 'Security setting saved'));
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    if (json.errors.every(function (error) {
                        return error.id === 'status'
                    })) {
                        Ext.create('Uni.view.window.Confirmation', {
                            confirmText: Uni.I18n.translate('general.yes', 'UNI', 'Yes'),
                            cancelText: Uni.I18n.translate('general.no', 'UNI', 'No')
                        }).show({
                            msg: Uni.I18n.translate('devicesecuritysetting.createIncomplete.msg', 'MDC', 'Are you sure you want to add this incomplete security setting?'),
                            title: Uni.I18n.translate('devicesecuritysetting.createIncomplete.title', 'MDC', 'One or more required attributes are missing'),
                            config: {
                                me: me,
                                record: record
                            },

                            fn: me.saveAsIncomplete
                        });
                    } else {
                        me.getDeviceSecuritySettingEditForm().getForm().markInvalid(json.errors);
                        //             me.getPropertiesController().showErrors(json.errors);
                    }
                }
            }
        });
    },

    saveAsIncomplete: function (btn, text, opt) {
        if (btn === 'confirm') {
            var record = opt.config.record;
            record.set('saveAsIncomplete', true);
            opt.config.me.saveRecord(record);
        }
    },

    showDeviceSecuritySettingEditView: function (mrid, deviceSecuritySettingId) {
        this.mrid = mrid;
        var me = this;
        var deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');
        var deviceSecuritySettingModel = Ext.ModelManager.getModel('Mdc.model.DeviceSecuritySetting');

        deviceModel.load(mrid, {
            success: function (device) {
                deviceSecuritySettingModel.getProxy().setExtraParam('mrid', encodeURIComponent(mrid));
                deviceSecuritySettingModel.load(deviceSecuritySettingId, {
                    success: function (deviceSecuritySetting) {
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadDeviceSecuritySetting', deviceSecuritySetting);
                        var widget = Ext.widget('deviceSecuritySettingEdit', {
                            edit: true,
                            returnLink: '#/devices/' + encodeURIComponent(me.mrid) + '/securitysettings',
                            device: device
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.setLoading(true);

                        var title = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + deviceSecuritySetting.get('name') + '\'';
                        widget.down('#deviceSecuritySettingEditAddTitle').setTitle(title);
                        var generalForm = widget.down('#deviceSecuritySettingEditForm');
                        generalForm.loadRecord(deviceSecuritySetting);
                        var form = widget.down('property-form');
                        form.inputType = 'password';
                        form.passwordAsTextComponent = true;
                        if (deviceSecuritySetting.properties().count()) {
                            if (deviceSecuritySetting.get('userHasViewPrivilege') && deviceSecuritySetting.get('userHasEditPrivilege')) {
                                widget.down('#device-security-setting-show-value').setVisible(true);
                                form.userHasEditPrivilege = true;
                                form.userHasViewPrivilege = true;
                                form.show();
                                form.loadRecord(deviceSecuritySetting);
                                me.getDeviceSecuritySettingDetailTitle().setVisible(true);
                                //widget.down('#editDeviceSecuritySetting').setVisible(false);
                                //widget.down('#showValueDeviceSecuritySetting').setVisible(true);
                                //widget.down('#hideValueDeviceSecuritySetting').setVisible(false);
                            } else if (!deviceSecuritySetting.get('userHasViewPrivilege') && deviceSecuritySetting.get('userHasEditPrivilege')) {
                                widget.down('#device-security-setting-show-value').setVisible(false);
                                form.userHasEditPrivilege = true;
                                form.userHasViewPrivilege = false;
                                form.show();
                                form.loadRecord(deviceSecuritySetting);
                                me.getDeviceSecuritySettingDetailTitle().setVisible(true);
                            } else if (deviceSecuritySetting.get('userHasViewPrivilege') && !deviceSecuritySetting.get('userHasEditPrivilege')) {
                                // only view
                                widget.down('#device-security-setting-show-value').setVisible(false);
                                form.userHasEditPrivilege = false;
                                form.userHasViewPrivilege = true;
                                form.isReadOnly = true;
                                form.show();
                                me.getRestoreAllButton().hide();
                                me.getAddEditButton().hide();
                                form.loadRecord(deviceSecuritySetting);
                                me.getDeviceSecuritySettingDetailTitle().setVisible(true);
                            } else {
                                widget.down('#device-security-setting-show-value').setVisible(false);
                                form.hide();
                                me.getRestoreAllButton().hide();
                                me.getAddEditButton().hide();
                                me.getDeviceSecuritySettingDetailTitle().setVisible(false);
                            }
                        } else {
                            form.hide();
                            me.getDeviceSecuritySettingDetailTitle().setVisible(false);
                        }
                        widget.setLoading(false);
                    }
                });
            }
        });
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
        me.getDeviceSecuritySettingEditView().down('property-form').restoreAll();
        me.getRestoreAllButton().disable();
    },

    showPropertyValues: function (show) {
        var showBtns = Ext.ComponentQuery.query('#showValueDeviceSecuritySetting'),
            hideBtns = Ext.ComponentQuery.query('#hideValueDeviceSecuritySetting'),
            propertyForm = this.getDeviceSecuritySettingPreview().down('property-form');

        Ext.each(showBtns, function (btn) {
            btn.setVisible(!show);
        });

        Ext.each(hideBtns, function (btn) {
            btn.setVisible(show);
        });

        if (show) {
            propertyForm.showValues();
        } else {
            propertyForm.hideValues();
        }

        this.getDeviceSecuritySettingPreviewForm().focus();
    },

    showValueInEdit: function (field, newValue, oldValue, options){
        var me = this;
        if (newValue) {
            me.getDeviceSecuritySettingEditView().down('property-form').showValues();
        } else {
            me.getDeviceSecuritySettingEditView().down('property-form').hideValues();
        }
    }
})
;