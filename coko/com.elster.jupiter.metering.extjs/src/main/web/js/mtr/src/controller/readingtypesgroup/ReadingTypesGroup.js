/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.readingtypesgroup.ReadingTypesGroup', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.view.readingtypesgroup.GroupsOverview',
        'Mtr.view.readingtypesgroup.GroupsGrid',
        'Mtr.view.readingtypesgroup.GroupPreview',
        'Mtr.view.readingtypesgroup.Details',
        'Mtr.view.readingtypesgroup.ReadingTypesInGroup'
    ],

    models: ['Mtr.model.readingtypesgroup.ReadingTypeGroup'],

    requires: ['Mtr.view.readingtypesgroup.Details',
        'Mtr.util.readingtypesgroup.FilterTopPanel'
    ],

    stores: [
        'Mtr.store.readingtypes.ReadingTypes',
        'Mtr.store.readingtypesgroup.ReadingTypeGroups',
        'Mtr.store.readingtypes.ReadingTypesByAlias'
    ],


    refs: [
        {
            ref: 'page',
            selector: '#reading-types-setup'
        },
        {
            ref: 'pageReadingTypesInGroup',
            selector: '#reading-types-in-group'
        },
        {
            ref: 'previewForm',
            selector: '#reading-types-preview-form'
        },
        {
            ref: 'preview',
            selector: '#reading-types-preview'
        },
        {
            ref: 'readingTypesGrid',
            selector: '#metering-reading-types-grid'
        },
        {
            ref: 'readingTypesPreviewMenu',
            selector: '#reading-types-preview reading-types-action-menu'
        },
        {
            ref: 'readingTypesEditAliasWindow',
            selector: '#edit-alias-window'
        },
        {
            ref: 'readingTypesInGroupFilterPanel',
            selector: '#reading-types-group-filter-top-panel'
        },
        {
            ref: 'groupPreviewForm',
            selector: 'readingTypesGroup-preview-form'
        },
        {
            ref: 'groupPreview',
            selector: 'readingTypesGroup-preview'
        },
        {
            ref: 'readingTypesGroupPreviewMenu',
            selector: 'readingTypesGroup-preview readingTypesGroup-action-menu'
        },
        {
            ref: 'readingTypesGroupDetails',
            selector: 'reading-type-groups-details'
        },
        {
            ref: 'readingTypesGroupMenu',
            selector: '#mnu-reading-types-group'
        },
        {
            ref: 'readingTypesInGroup',
            selector: 'reading-types-in-group'
        },
        {
            ref: 'readingTypeGroupOverview',
            selector: '#reading-types-group-details'
        }
    ],

    init: function () {
        this.control({
            'reading-types-action-menu': {
                click: this.chooseAction
            },
            'edit-alias-window #edit-save-button': {
                click: this.changeAlias
            },
            '#add-reading-type-button': {
                click: this.browseAdd
            },
            '#overview-add-reading-type-button': {
                click: this.browseAdd
            },
            '#reading-types-bulk-action-button': {
                click: this.showBulkAction
            },
            '#edit-alias-window-textfield': {
                change: this.disableSaveButton
            },
            'reading-type-groups-grid': {
                select: this.showPreview
            },
            '#mtr-add-readingTypeGroup-button': {  //lori
                click: this.browseGroupAdd
            },
            'reading-types-in-group-grid': {
                select: this.showReadingTypePreview
            }
        });
    },

    showReadingTypePreview: function(selectionModel, record) {
        var me = this,
            menu = me.getReadingTypesPreviewMenu();
        if (menu) menu.record = record;
        me.getPreview().setTitle(Ext.String.htmlEncode(record.get('fullAliasName')));
        me.getPreviewForm().loadRecord(record);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            menu = me.getReadingTypesPreviewMenu();
        if (menu) menu.record = record;
        me.getPreview().setTitle(Ext.String.htmlEncode(record.get('fullAliasName')));
        me.getPreviewForm().loadRecord(record);
    },

    chooseAction: function (menu, item) {
        var me = this,
            gridView = me.getReadingTypesGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected(),
            model = me.getModel('Mtr.model.readingtypes.ReadingType');

        switch (item.action) {
            case 'edit':
                me.msg = Uni.I18n.translate('readingtypesmanagment.saved', 'MTR', 'saved');
                var editWindow = Ext.create('Mtr.view.readingtypes.EditAliasWindow');
                editWindow.setTitle(Uni.I18n.translate('readingtypesmanagment.editalias', 'MTR', 'Edit {0}', record.get('fullAliasName'), false));
                editWindow.down('textfield').setValue(record.get('aliasName'));
                editWindow.show();
                break;
            case 'activate':
                record.set('active', true);
                me.msg = Uni.I18n.translate('readingtypesmanagment.activated', 'MTR', 'activated');
                me.changeReadingType(item.action, record);
                break;
            case 'deactivate':
                record.set('active', false);
                me.msg = Uni.I18n.translate('readingtypesmanagment.deactivated', 'MTR', 'deactivated');
                me.changeReadingType(item.action, record);
                break;
        }
    },

    changeAlias: function () {
        var me = this,
            aliasName = this.getReadingTypesEditAliasWindow().down('form').getValues().aliasName,
            gridView = this.getReadingTypesGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected(),
            action = 'edit';

        record.set('aliasName', aliasName);
        this.changeReadingType(action, record);
        me.getReadingTypesEditAliasWindow().destroy();
    },

    changeReadingType: function (action, record) {

        var me = this, callback,
            router = me.getController('Uni.controller.history.Router');

        var model = Ext.ModelManager.getModel('Mtr.model.readingtypes.ReadingType');
        model.getProxy().setUrl(record.get('mRID'));

        callback = {
            isNotEdit: true,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('readingtypesmanagment.readingTypeActivateDeactivate', 'MTR', 'Reading type {0}', [me.msg]));
                router.getRoute('administration/readingtypes').forward(null,
                    router.getQueryStringValues()
                );
            },
            failure: function (record, operation) {
                if (operation.response.status === 400) {
                    var result = Ext.decode(operation.response.responseText, true),
                        title = Uni.I18n.translate('readingtypesmanagment.readingTypeNotActivateDeactivateTitle', 'MTR', 'Couldn\'t perform your action'),
                        message = Uni.I18n.translate('readingtypesmanagment.readingTypeNotActivateDeactivate', 'MTR', 'Reading type did not {0}', [me.msg]) + '.' + Uni.I18n.translate('general.serverError', 'MTR', 'Server error'),
                        code = '';
                    if (!Ext.isEmpty(operation.response.statusText)) {
                        message = operation.response.statusText;
                    }
                    if (result && result.message) {
                        message = result.message;
                    } else if (result && result.error) {
                        message = result.error;
                    }
                    if (result && result.errorCode) {
                        code = result.errorCode;
                    }
                    me.getApplication().getController('Uni.controller.Error').showError(title, message, code);
                }
            }
        };

        if (action != 'edit') {
            callback.url = '/api/mtr/readingtypes/' + record.get('mRID') + '/activate';
        }
        record.save(callback);
    },

    browseAdd: function () {
        var router = this.getController('Uni.controller.history.Router'),
            addController = this.getController('Mtr.controller.readingtypesgroup.AddReadingTypesGroup');
        addController.qString = router.getQueryStringValues();
        router.getRoute('administration/readingtypes/add').forward();
    },

    disableSaveButton: function (field, newValue) {
        var me = this,
            saveBtn = me.getReadingTypesEditAliasWindow().down('#edit-save-button');
        if (!newValue) {
            saveBtn.disable();
        } else {
            saveBtn.enable();
        }
    },

    showBulkAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filterPanel = me.getPage().down('reading-types-group-filter-top-panel'),
            store = me.getStore('Mtr.store.readingtypes.ReadingTypes'),
            storeBulk = me.getStore('Mtr.store.readingtypes.ReadingTypesBulk');
        storeBulk.setProxy(store.getProxy());
        storeBulk.getProxy().setExtraParam('filter',
            filterPanel.createFiltersObject(filterPanel.getFilterParams())
        );
        router.getRoute('administration/readingtypes/bulk').forward(null,
            router.getQueryStringValues()
        );
    },

    showOverview: function () {
        var me = this,
            widget,
            store = me.getStore('Mtr.store.readingtypesgroup.ReadingTypeGroups'),
            viewport = Ext.ComponentQuery.query('viewport')[0];
        widget = Ext.widget('reading-type-groups-overview', {router: me.getController('Uni.controller.history.Router')});
        me.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            menu = me.getReadingTypesGroupPreviewMenu();

        if (menu) {
            menu.record = record;
        }

        me.getGroupPreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        me.getGroupPreviewForm().loadRecord(record);
    },

    browseGroupAdd: function () {  //lori
        var router = this.getController('Uni.controller.history.Router'),
            addController = this.getController('Mtr.controller.readingtypesgroup.AddReadingTypesGroup');
        addController.qString = router.getQueryStringValues();
        router.getRoute('administration/readingtypegroups/add').forward();  //lori
    },

    showReadingTypesGroupDetails: function (aliasName) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            groupModel = me.getModel('Mtr.model.readingtypesgroup.ReadingTypeGroup'),
            view = Ext.widget('reading-type-groups-details', {
                router: router
            }),
            actionsMenu = view.down('readingTypesGroup-action-menu');

        me.fromDetail = true;
        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading();
        groupModel.load(aliasName, {
            success: function (record) {
                var detailsForm = view.down('readingTypesGroup-preview-form');
                actionsMenu.record = record;
                me.getApplication().fireEvent('readingtypesgroupload', record);
                detailsForm.loadRecord(record);
            },
            callback: function () {
                view.setLoading(false);
            }
        });
    },

    showReadingTypesInGroup: function (aliasName) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            router = me.getController('Uni.controller.history.Router'),
            model = me.getModel('Mtr.model.readingtypes.ReadingType'),
            view = Ext.widget('reading-types-in-group', {
                router: router
            }),
            readingTypesInGroup = view.down('reading-types-in-group'),
            actionsMenu = view.down('mnu-reading-types-group');

        me.fromDetail = true;

        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading();
        var store = Ext.getStore('Mtr.store.readingtypes.ReadingTypesByAlias');
        store.getProxy().setUrl(aliasName);
        store.load(function(records, operation, success) {
            view.setLoading(false);
        });

       /* model.load(aliasName, {
            success: function (record) {
                var detailsForm = readingTypesInGroup.down('reading-types-in-group');
                actionsMenu.record = record;
                Ext.suspendLayouts();
                readingTypesInGroup.down('reading-types-menu').setHeader(record.get('name'));
                me.getApplication().fireEvent('readingtypesingroupload', record);
                detailsForm.loadRecord(record);
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
        */

    }
});

