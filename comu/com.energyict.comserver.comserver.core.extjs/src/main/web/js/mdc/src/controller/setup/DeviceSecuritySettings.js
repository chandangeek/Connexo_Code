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
        {ref: 'addEditButton', selector: '#addEditButton'}
    ],

    init: function () {
        this.control({
            '#devicesecuritysettinggrid': {
                selectionchange: this.previewDeviceSecuritySetting
            },
            '#devicesecuritysettinggrid actioncolumn': {
                editDeviceSecuritySetting: this.editDeviceSecuritySettingHistory
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
                dirtychange: this.enableRestoreAllButton
            },
            '#deviceSecuritySettingEdit property-form component': {
                enableRestoreAll: this.enableRestoreAllButton
            }
        });
    },

    showDeviceSecuritySettings: function (mrid) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        this.mrid = mrid;

        viewport.setLoading();

        var securitySettingsOfDeviceStore = Ext.StoreManager.get('SecuritySettingsOfDevice');
        securitySettingsOfDeviceStore.getProxy().setExtraParam('mrid', mrid);
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
            me.getDeviceSecuritySettingPreview().setTitle(deviceSecuritySettingName);
            me.getDeviceSecuritySettingPreviewForm().loadRecord(deviceSecuritySetting[0]);
            if (deviceSecuritySetting[0].propertiesStore.data.items.length > 0) {
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
                }
            } else {
                me.getDeviceSecuritySettingPreviewTitle().setVisible(false);
            }
            me.getDeviceSecuritySettingPreview().setTitle(deviceSecuritySettingName);
        } else {
            me.getDeviceSecuritySettingPreview().getLayout().setActiveItem(0);
        }
    },

    editDeviceSecuritySettingHistory: function (record) {
        location.href = '#/devices/' + this.mrid + '/securitysettings/' + record.get('id') + '/edit';
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
                location.href = '#/devices/' + me.mrid + '/securitysettings/';
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.saveSuccess.msg.edit', 'MDC', 'Connection method saved'));
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
                deviceSecuritySettingModel.getProxy().setExtraParam('mrid', mrid);
                deviceSecuritySettingModel.load(deviceSecuritySettingId, {
                    success: function (deviceSecuritySetting) {
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadDeviceSecuritySetting', deviceSecuritySetting);
                        var widget = Ext.widget('deviceSecuritySettingEdit', {
                            edit: true,
                            returnLink: '#/devices/' + me.mrid + '/securitysettings'
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.setLoading(true);

                        var title = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + deviceSecuritySetting.get('name') + '\'';
                        widget.down('#deviceSecuritySettingEditAddTitle').update('<h1>' + title + '</h1>');
                        var generalForm = widget.down('#deviceSecuritySettingEditForm');
                        generalForm.loadRecord(deviceSecuritySetting);
                        var form = widget.down('property-form');
                        if (deviceSecuritySetting.properties().count()) {
                            if (deviceSecuritySetting.get('userHasViewPrivilege') && deviceSecuritySetting.get('userHasEditPrivilege')) {
                                form.show();
                                form.loadRecord(deviceSecuritySetting);
                                me.getDeviceSecuritySettingDetailTitle().setVisible(true);
                            } else if (!deviceSecuritySetting.get('userHasViewPrivilege') && deviceSecuritySetting.get('userHasEditPrivilege')) {
                                form.show();
                                form.loadRecord(deviceSecuritySetting);
                                me.getDeviceSecuritySettingDetailTitle().setVisible(true);
                            } else if (deviceSecuritySetting.get('userHasViewPrivilege') && !deviceSecuritySetting.get('userHasEditPrivilege')) {
                                // only view
                                form.isReadOnly = true;
                                form.show();
                                me.getRestoreAllButton().hide();
                                me.getAddEditButton().hide();
                                form.loadRecord(deviceSecuritySetting);
                                me.getDeviceSecuritySettingDetailTitle().setVisible(true);

                            } else {
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

    enableRestoreAllButton: function (form, dirty) {
        var me = this;
        if (typeof(me.getRestoreAllButton()) !== 'undefined') {
            me.getRestoreAllButton().disable();
            var restoreAllButtons = Ext.ComponentQuery.query('uni-default-button');
            if (restoreAllButtons != null) {
                restoreAllButtons.forEach(function (restoreButton) {
                    if (!restoreButton.isHidden()) {
                        me.getRestoreAllButton().enable();
                    }
                })
            }
        }
    },

    restoreAllDefaults: function () {
        var me = this;
        me.getDeviceSecuritySettingEditView().down('property-form').restoreAll();
        me.getRestoreAllButton().disable();
    }

})
;