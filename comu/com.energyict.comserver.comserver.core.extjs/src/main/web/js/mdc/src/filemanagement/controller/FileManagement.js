Ext.define('Mdc.filemanagement.controller.FileManagement', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.filemanagement.view.Setup',
        'Mdc.fileManagement.view.EditSpecificationsSetup'
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
        }
    ],

    deviceTypeId: null,
    init: function () {
        var me = this;

        me.control({
            'device-type-files-setup #edit-files-specifications': {
                click: me.goToEditPage
            },
            'files-devicetype-edit-specs-form #files-save-specs-button': {
                click: this.saveFileManagementSettings
            },
            'device-type-files-setup #add-file-btn': {
                click: this.openDialog
            }
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
                    fileManagementEnabled: deviceType.get('fileManagementEnabled')
                });
                view.down('files-devicetype-specifications-form').loadRecord(deviceType);
                store.load({
                    callback: function (records, operation, success) {
                        if (success === true) {
                            me.updateCounter(store.getCount());
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
                me.getEditForm().loadRecord(deviceType);
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
            record;

        form.updateRecord();
        record = form.getRecord();
        formErrorsPanel.hide();
        record.beginEdit();
        record.set('fileManagementEnabled', form.down('#files-allowed-radio-field').checked);
        record.endEdit(true);
        debugger;
        record.save({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/filemanagement', {deviceTypeId: me.deviceTypeId}).forward();
            },
            failure: function (record, operation) {
                formErrorsPanel.show();
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                }
            }
        });
    },

    openDialog: function () {

    }

})
;