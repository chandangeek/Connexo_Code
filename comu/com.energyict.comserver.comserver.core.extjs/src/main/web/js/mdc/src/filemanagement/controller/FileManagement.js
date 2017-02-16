/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.filemanagement.controller.FileManagement', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.filemanagement.view.Setup',
        'Mdc.filemanagement.view.EditSpecificationsSetup'
    ],

    stores: [
        'Mdc.filemanagement.store.Files'
    ],

    models: [
        'Mdc.model.DeviceType',
        'Mdc.filemanagement.model.File'
    ],

    refs: [
        {
            ref: 'filesGrid',
            selector: '#files-grid'
        },
        {
            ref: 'editForm',
            selector: 'files-devicetype-edit-specs-form'
        },
        {
            ref: 'setup',
            selector: 'device-type-files-setup'
        }
    ],

    fromEditForm: false,
    deviceTypeId: null,
    init: function () {
        var me = this;

        me.control({
            'device-type-files-setup #edit-files-specifications': {
                click: me.goToEditPage
            },
            'files-devicetype-edit-specs-form #files-save-specs-button': {
                click: me.saveFileManagementSettings
            },
            'device-type-files-setup form filefield': {
                change: me.uploadFile
            },
            'files-grid actioncolumn': {
                removeEvent: me.removeFile
            },
            '#enable-file-management-btn': {
                click: me.goToEditPage
            }
            //'#file-management-radio': {
            //    change
            //}
        });
    },

    showFileManagementOverview: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.filemanagement.store.Files');
        store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('device-type-files-setup', {
                    deviceTypeId: deviceTypeId,
                    fileManagementEnabled: deviceType.get('fileManagementEnabled'),
                    fromEditForm: me.fromEditForm
                });
                me.fromEditForm = false;
                view.down('files-devicetype-specifications-form').loadRecord(deviceType);
                store.load({
                    callback: function (records, operation, success) {
                        if (success === true) {
                            me.updateCounter(store.getCount());
                            me.updateGridHeight();
                        }
                    }
                });
                me.deviceTypeId = deviceTypeId;
                me.getApplication().fireEvent('changecontentevent', view);
                view.suspendLayouts();
                view.setLoading(true);
                me.reconfigureMenu(deviceType, view);
            }
        });
    }
    ,

    showEditSpecifications: function (deviceTypeId) {
        var me = this,
            view;

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                view = Ext.widget('files-devicetype-edit-specs-setup', {
                    deviceTypeId: deviceTypeId,
                    fileManagementEnabled: deviceType.get('fileManagementEnabled')
                });
                view.down('files-devicetype-edit-specs-form').loadRecord(deviceType);
                me.fromEditForm = true;
                me.deviceTypeId = deviceTypeId;
                view.setLoading(true);
                view.suspendLayouts();
                me.reconfigureMenu(deviceType, view);
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    }
    ,

    goToEditPage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/devicetypes/view/filemanagement/edit', {deviceTypeId: me.deviceTypeId});
        route.forward();
    }
    ,

    reconfigureMenu: function (deviceType, view) {
        var me = this;
        me.getApplication().fireEvent('loadDeviceType', deviceType);
        if (view.down('deviceTypeSideMenu')) {
            view.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
            view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
            );
        }
        view.setLoading(false);
        view.resumeLayouts();
    }
    ,

    updateCounter: function (count) {
        var me = this;

        if (me.getFilesGrid()) {
            me.getFilesGrid().down('pagingtoolbartop #displayItem').setText(
                Uni.I18n.translatePlural('general.filesCount', count, 'MDC', 'No files', '{0} file', '{0} files')
            );
        }
    }
    ,

    saveFileManagementSettings: function () {
        var me = this,
            form = me.getEditForm(),
            formErrorsPanel = form.down('#form-errors'),
            record,
            confirmationWindow;

        form.updateRecord();
        record = form.getRecord();
        formErrorsPanel.hide();
        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.save', 'MDC', 'Save'),
        });


        if(!form.down('#files-allowed-radio-field').checked) {
            confirmationWindow.show(
                {
                    msg: Uni.I18n.translate('filemanagement.disable.msg', 'MDC', 'You will not be able to use these files anymore, existing files will be removed from the system. This action is irreversible.'),
                    title: Uni.I18n.translate('general.disableFileManagement', 'MDC', "Disable file management?"),
                    fn: function (state) {
                        if (state === 'confirm') {
                            record.beginEdit();
                            record.set('fileManagementEnabled', form.down('#files-allowed-radio-field').checked);
                            record.endEdit(true);
                            record.save({
                                success: function () {
                                    me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/filemanagement', {deviceTypeId: me.deviceTypeId}).forward();
                                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('filemanagement.specificationsSaved', 'MDC', 'File management specifications saved'))
                                },
                                failure: function (record, operation) {
                                    formErrorsPanel.show();
                                    var json = Ext.decode(operation.response.responseText);
                                    if (json && json.errors) {
                                        form.getForm().markInvalid(json.errors);
                                    }
                                }
                            });
                        }
                    }
                });
        } else {
            record.beginEdit();
            record.set('fileManagementEnabled', form.down('#files-allowed-radio-field').checked);
            record.endEdit(true);
            record.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/filemanagement', {deviceTypeId: me.deviceTypeId}).forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('filemanagement.specificationsSaved', 'MDC', 'File management specifications saved'));
                },
                failure: function (record, operation) {
                    formErrorsPanel.show();
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                    }
                }
            });
        }

    },

    uploadFile: function (fileField) {
        var me = this,
            setup = me.getSetup(),
            form,
            store = me.getStore('Mdc.filemanagement.store.Files'),
            max_file_size = 2 * 1024 * 1024,
            input,
            file;
        if (fileField.up('#no-files')) {
            form = setup.down('files-devicetype-preview-container').down('#no-files').down('form');
        } else {
            form = setup.down('files-devicetype-preview-container').down('#files-grid').down('form');
        }
        input = form.down('filefield').button.fileInputEl.dom;
        file = input.files[0];
        if(file === undefined) {
            return;
        }
        if(file.size > max_file_size) {
            me.getApplication().getController('Uni.controller.Error')
                .showError(Uni.I18n.translate('general.failed.to.upload.file', 'MDC', 'Failed to upload file'), Uni.I18n.translate('filemanagement.fileSizShouldBeLessThan', 'MDC', 'File size should be less than 2 MB'));
            fileField.reset();
        } else {
            store.getProxy().setUrl(me.deviceTypeId);
            setup.setLoading();
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/files/upload',
                method: 'POST',
                form: form.getEl().dom,
                params: {
                    fileName: me.getFileName(form.down('filefield').getValue())
                },
                headers: {'Content-type': 'multipart/form-data'},
                isFormUpload: true,
                callback: function (config, success, response) {
                    fileField.reset();
                    if (response.responseText) {
                        var responseObject = JSON.parse(response.responseText);
                        if (!responseObject.success) {
                            me.getApplication().getController('Uni.controller.Error')
                                .showError(Uni.I18n.translate('general.failed.to.upload.file', 'MDC', 'Failed to upload file'), responseObject.message);
                        }
                    }
                    store.load({
                        callback: function (records, operation, success) {
                            if (success === true) {
                                me.updateCounter(store.getCount());
                                me.updateGridHeight();
                            }
                        }
                    });
                    setup.setLoading(false);
                }
            });
        }
    },

    getFileName: function (fullPath) {
        var filename = fullPath.replace(/^.*[\\\/]/, '');
        return filename;
    },

    removeFile: function (record) {
        var me = this,
            setup = me.getSetup(),
            store = me.getStore('Mdc.filemanagement.store.Files'),
            confirmationWindow;
        record.getProxy().setUrl(me.deviceTypeId);
        confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('file.remove.msg', 'MDC', "The file will no longer be available to send to a device. Pending commands using this file won't be executed."),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", record.get('name')),
                fn: function (state) {
                    if (state === 'confirm') {
                        setup.down('files-devicetype-preview-container').down('#files-grid').setLoading();
                        record.destroy({
                            callback: function (record, operation, success) {
                                store.load({
                                    callback: function (records, operation, success) {
                                        setup.down('files-devicetype-preview-container').down('#files-grid').setLoading(false);
                                        if (success === true) {
                                            me.updateCounter(store.getCount());
                                            me.updateGridHeight();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
    },

    updateGridHeight: function () {
        var me = this,
            setup = me.getSetup(),
            grid = setup.down('files-grid'),
            maxHeight = window.innerHeight - 280;

        if(maxHeight < 400) {
            maxHeight = 400;
        }
        if(grid) {
            grid.maxHeight = maxHeight;
            grid.updateLayout();
        }
    }


})
;