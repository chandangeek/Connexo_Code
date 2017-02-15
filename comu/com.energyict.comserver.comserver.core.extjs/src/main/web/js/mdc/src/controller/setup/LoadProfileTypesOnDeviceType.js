/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.LoadProfileTypesOnDeviceType', {
    extend: 'Ext.app.Controller',

    required: [
        'Mdc.store.Intervals',
        'Mdc.model.LoadProfileTypeOnDeviceType',
        'Mdc.model.DeviceType'
    ],

    views: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeOnDeviceTypeSetup',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeSideFilter',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeSorting',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeFiltering',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypePreview',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid',
        'Mdc.view.setup.loadprofiletype.EditCustomAttributeSetSetup'
    ],

    refs: [
        {ref: 'page', selector: 'loadProfileTypeSetup'},
        {ref: 'loadTypeGrid', selector: 'loadProfileTypeSetup loadProfileTypeGrid'},
        {ref: 'loadTypePreview', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypePreview'},
        {ref: 'loadTypeCountContainer', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypesCountContainer'},
        {
            ref: 'loadTypeEmptyListContainer',
            selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypeEmptyListContainer'
        },
        {ref: 'addLoadProfileTypesGrid', selector: '#loadprofile-type-add-grid'},
        {ref: 'uncheckLoadProfileButton', selector: '#uncheckAllLoadProfileTypes'},
        {ref: 'addLoadProfileTypesSetup', selector: '#loadProfileTypesAddToDeviceTypeSetup'},
        {ref: 'addLoadProfileTypePanel', selector: '#addLoadProfileTypePanel'},
        {ref: 'editLoadProfileTypePage', selector: '#edit-custom-attribute-set-setup-id'}
    ],

    requires: [
        'Mdc.store.LoadProfileTypesOnDeviceTypeAvailable'
    ],

    stores: [
        'Mdc.store.LoadProfileTypesOnDeviceType',
        'LoadProfileTypesOnDeviceTypeAvailable',
        'Mdc.store.Intervals',
        'Mdc.store.LoadProfileTypes'
    ],

    models: [
        'Mdc.model.LoadProfileTypeOnDeviceType',
        'Mdc.model.DeviceType'
    ],

    init: function () {
        this.control({
            'loadProfileTypeOnDeviceTypeSetup loadProfileTypeGrid': {
                itemclick: this.loadGridItemDetail
            },
            'loadProfileTypesAddToDeviceTypeSetup grid': {
                selectionchange: this.hideLoadProfileTypesErrorPanel
            },
            '#addButton[action=addLoadProfileTypeAction]': {
                click: this.addLoadProfileTypesToDeviceType
            },
            '#edit-custom-attribute-set-setup-id #cancel-edit-load-profile-type-button': {
                click: this.moveToLoadProfilesPage
            },
            '#edit-custom-attribute-set-setup-id #edit-load-profile-type-form-panel': {
                saverecord: this.saveCustomAttributes
            }
        });
    },

    saveCustomAttributes: function (record, value) {
        var me = this,
            editPage = me.getEditLoadProfileTypePage();

        editPage.setLoading();

        record.set('customPropertySet', value);
        record.save({
            success: function () {
                me.moveToLoadProfilesPage();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('loadprofiletype.customattribute.saved', 'MDC', 'Load profile type saved'));
                editPage.setLoading(false);
            },
            failure: function () {
                editPage.setLoading(false);
            }
        });
    },

    showConfirmationPanel: function () {
        var me = this,
            grid = me.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileTypes.confirmWindow.removeMsgOnDeviceType', 'MDC', 'This load profile type will no longer be available on this device type.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [lastSelected.get('name')]),
            config: {
                me: me
            },
            fn: me.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            page = me.getPage(),
            grid = me.getLoadTypeGrid(),
            router = me.getController('Uni.controller.history.Router'),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        page.setLoading();
        lastSelected.getProxy().setUrl(router.arguments.deviceTypeId);
        if (state === 'confirm') {
            lastSelected.destroy({
                success: function () {
                    me.handleSuccessRequest(Uni.I18n.translate('loadProfileTypes.removeSuccessMsg', 'MDC', 'Load profile type removed'));
                },
                callback: function () {
                    page.setLoading(false);
                }
            });
        }
    },

    addLoadProfileTypesToDeviceType: function () {
        var me = this,
            grid = this.getAddLoadProfileTypesGrid(),
            page = me.getAddLoadProfileTypesSetup(),
            router = me.getController('Uni.controller.history.Router'),
            selection = grid.getSelectionModel().getSelection(),
            selectedItems = [];

        if (Ext.isEmpty(selection)) {
            me.showLoadProfileTypesErrorPanel();
        } else {
            page.setLoading();
            Ext.each(selection, function (loadprofileType) {
                selectedItems.push(loadprofileType.get('id'));
            });

            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + router.arguments.deviceTypeId + '/loadprofiletypes',
                method: 'POST',
                jsonData: Ext.JSON.encode(selectedItems),
                success: function () {
                    me.handleSuccessRequest('Load profile types added');
                },
                callback: function () {
                    page.setLoading(false);
                }
            });
        }
    },

    moveToLoadProfilesPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/loadprofiles').forward();
    },

    handleSuccessRequest: function (headerText) {
        this.moveToLoadProfilesPage();
        this.getApplication().fireEvent('acknowledge', headerText);
    },

    loadGridItemDetail: function (grid, record) {
        var form = Ext.ComponentQuery.query('loadProfileTypeOnDeviceTypeSetup loadProfileTypePreview form')[0];
        Ext.suspendLayouts();
        form.loadRecord(record);
        Ext.resumeLayouts();
    },

    showDeviceTypeLoadProfileTypesView: function (deviceTypeId) {
        var me = this,
            loadProfileTypesStore = me.getStore('LoadProfileTypesOnDeviceType'),
            intervalsStore = me.getStore('Mdc.store.Intervals'),
            widget;

        loadProfileTypesStore.getProxy().setUrl(deviceTypeId);
        intervalsStore.load(function () {
            widget = Ext.widget('loadProfileTypeSetup', {
                config: {
                    deviceTypeId: deviceTypeId,
                    gridStore: loadProfileTypesStore
                },
                showCustomAttributeSet: true
            });
            me.loadDeviceTypeModel(me, widget, deviceTypeId, true);
            me.getApplication().fireEvent('changecontentevent', widget);
            Ext.Array.each(Ext.ComponentQuery.query('[action=editloadprofiletype]'), function (item) {
                item.clearListeners();
                item.on('click', function () {
                    me.moveToEditPage();
                });
            });
            Ext.Array.each(Ext.ComponentQuery.query('[action=deleteloadprofiletype]'), function (item) {
                item.clearListeners();
                item.on('click', function () {
                    me.showConfirmationPanel();
                });
            });
        });
    },

    moveToEditPage: function () {
        var me = this,
            grid = me.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router');

        router.arguments.loadProfileTypeId = lastSelected.get('id');
        router.getRoute('administration/devicetypes/view/loadprofiles/edit').forward();
    },

    showDeviceTypeLoadProfileTypesAddView: function (deviceTypeId) {
        var me = this,
            intervalsStore = me.getStore('Mdc.store.Intervals'),
            availableLoadProfilesStore = me.getLoadProfileTypesOnDeviceTypeAvailableStore(),
            widget;

        availableLoadProfilesStore.getProxy().setUrl(deviceTypeId);
        intervalsStore.load(function () {
            widget = Ext.widget('loadProfileTypesAddToDeviceTypeSetup', {
                intervalStore: intervalsStore,
                deviceTypeId: deviceTypeId
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            me.loadDeviceTypeModel(me, widget, deviceTypeId, false);
            availableLoadProfilesStore.load();
        });
    },

    showDeviceTypeLoadProfileTypesEditView: function (deviceTypeId, loadProfiletypeId) {
        var me = this,
            loadProfileTypeModel = Ext.ModelManager.getModel('Mdc.model.LoadProfileTypeOnDeviceType'),
            widget,
            form;

        loadProfileTypeModel.getProxy().setUrl(deviceTypeId);
        widget = Ext.widget('edit-custom-attribute-set-setup');
        me.loadDeviceTypeModel(me, widget, deviceTypeId, false);
        me.getApplication().fireEvent('changecontentevent', widget);
        Ext.ModelManager.getModel('Mdc.model.LoadProfileTypeOnDeviceType').load(loadProfiletypeId, {
            success: function (loadProfileType) {
                me.getApplication().fireEvent('loadprofiletypeondevicetype', loadProfileType);
                form = widget.down('#edit-load-profile-type-form-panel');
                form.setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", loadProfileType.get('name')));
                form.customLoadRecord(loadProfileType, deviceTypeId);
            }
        });
    },

    loadDeviceTypeModel: function (scope, widget, deviceTypeId, setSideMenu) {
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                scope.getApplication().fireEvent('loadDeviceType', deviceType);
                if (setSideMenu && widget.down('deviceTypeSideMenu')) {
                    widget.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
                    widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', [deviceType.get('deviceConflictsCount')]));
                }
            }
        });
    },

    showLoadProfileTypesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-errors'),
            errorPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-selection-error');

        formErrorsPanel.show();
        errorPanel.show();
    },

    hideLoadProfileTypesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-errors'),
            errorPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-selection-error');

        formErrorsPanel.hide();
        errorPanel.hide();
    }
})
;

