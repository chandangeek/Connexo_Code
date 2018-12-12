/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.ReadingTypesGroup', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.view.GroupsOverview',
        'Mtr.view.GroupsGrid',
        'Mtr.view.GroupPreview',
        'Mtr.view.Details',
        'Mtr.view.ReadingTypesInGroup',
        'Mtr.view.EditAliasWindow'
    ],

    models: ['Mtr.model.ReadingTypeGroup'],

    requires: [
        'Mtr.view.Details',
        'Mtr.util.readingtypesgroup.FilterTopPanel'
    ],

    stores: [
        'Mtr.store.ReadingTypes',
        'Mtr.store.ReadingTypeGroups',
        'Mtr.store.ReadingTypesByAlias'
    ],

    refs: [
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
            selector: '#reading-types-groups-readingtype-preview'
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
        },
        {
            ref: 'readingTypesGroupPreviewMenu',
            selector: 'readingTypesGroup-preview readingTypesGroup-action-menu'
        },
        {
            ref: 'readingTypesInGroupGrid',
            selector: '#reading-types-in-group-grid'
        },
        {
            ref: 'readingTypesInGroupPreviewMenu',
            selector: '#reading-types-groups-readingtype-preview reading-types-action-menu'
        },
        {
            ref: 'readingTypeGroupsGrid',
            selector: '#metering-reading-types-group-grid'
        },
        {
            ref: 'readingTypesEditAliasWindow',
            selector: '#edit-alias-window'
        }
    ],

    init: function () {
        this.control({
            '#mtr-add-readingTypeGroup-button': {
                click: this.browseGroupAdd
            },
            '#mtr-reading-type-groups-overview-add-button': {
                click: this.browseGroupAdd
            },
            'reading-type-groups-grid': {
                select: this.showTypesGroupPreview
            },
            'reading-types-in-group-grid': {
                select: this.showReadingTypePreview
            },
            '#reading-types-set-bulk-action-button': {
                click: this.showSetBulkAction
            },
            '#reading-types-add-action-button-disabled-fields': {  // CXO-8276
                click: this.uploadAddForm
            },
            'reading-types-action-menu': {
                click: this.chooseAction
            },
            'readingTypesGroup-action-menu': {
                click: this.chooseGroupAction
            },
            'edit-alias-window #edit-save-button': {
                click: this.changeAlias
            }
        });
    },

    showReadingTypePreview: function(selectionModel, record) {
        var me = this,
            menu = me.getReadingTypesInGroupPreviewMenu();
        if (menu) menu.record = record;

        me.getPreview().setTitle(Ext.String.htmlEncode(record.get('fullAliasName')));
        me.getPreviewForm().loadRecord(record);

    },

    showOverview: function () {
        var me = this,
            widget,
            store = me.getStore('Mtr.store.ReadingTypeGroups');
        widget = Ext.widget('reading-type-groups-overview', {router: me.getController('Uni.controller.history.Router')});
        me.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    showTypesGroupPreview: function (selectionModel, record) {
        var me = this;

        me.getGroupPreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        me.getGroupPreviewForm().loadRecord(record);
        me.getGroupPreview().down('readingTypesGroup-action-menu').record = record;

    },

    browseGroupAdd: function () {
        var router = this.getController('Uni.controller.history.Router'),
            addController = this.getController('Mtr.controller.AddReadingTypesGroup');

        addController.qString = router.getQueryStringValues();
        router.getRoute('administration/readingtypes/add').forward();
    },

    uploadAddForm: function () {   // CXO-8276
        var router = this.getController('Uni.controller.history.Router'),
            me = this,
            addController = this.getController('Mtr.controller.AddReadingTypesGroup');
        gridView = me.getReadingTypesInGroupGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected();

        var mRID = "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0";
        if (record) {
            mRID = record.get('mRID');
        }

        var cimValues = mRID.split(".").map(function (item, index) {
            if (index == 5 || index == 4 || index == 6 || index == 16)
                return parseInt(item, 10);
            return 0;
        });

        mRID = cimValues.join('.');


        addController.qString = router.getQueryStringValues();

        // create a new route : administration/readingtypes/aliasName/readingtypes/add
        router.getRoute('administration/readingtypes/readingtypes/add').forward({aliasName: router.arguments.aliasName},
            {
                mRID: mRID
            });
        me.fromDetail = true;
    },

    showReadingTypesGroupDetails: function (aliasName) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            groupModel = me.getModel('Mtr.model.ReadingTypeGroup'),
            view = Ext.widget('reading-type-groups-details', {
                router: router
            });


        me.fromDetail = true;
        me.getApplication().fireEvent('changecontentevent', view);

        groupModel.load(aliasName, {
            success: function (record) {
                var detailsForm = view.down('readingTypesGroup-preview-form');
                view.down('readingTypesGroup-action-menu').record = record;
                me.getApplication().fireEvent('groupdetailsloaded', record.get('name'));
                detailsForm.loadRecord(record);
            }
        });
    },

    showReadingTypesInGroup: function (aliasName) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('reading-types-in-group', {
                router: router
            });

        me.fromDetail = true;

        me.getApplication().fireEvent('changecontentevent', view);
        var store = Ext.getStore('Mtr.store.ReadingTypesByAlias');
        store.getProxy().setUrl(aliasName);
        store.load();
        me.getApplication().fireEvent('groupdetailsloaded', aliasName);
    },

    showSetBulkAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filterPanel = me.getReadingTypesInGroupFilterPanel(),
            store = me.getStore('Mtr.store.ReadingTypesByAlias'),
            storeBulk = me.getStore('Mtr.store.ReadingTypesBulk');
        storeBulk.setProxy(store.getProxy());
        storeBulk.getProxy().setExtraParam('filter',
            filterPanel.createFiltersObject(filterPanel.getFilterParams())
        );

        router.getRoute('administration/readingtypes/bulk').forward({aliasName: router.arguments.aliasName},
            router.getQueryStringValues()
        );

    },

    chooseAction: function (menu, item) {
        var me = this,
            gridView = me.getReadingTypesInGroupGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected(),
            model = me.getModel('Mtr.model.ReadingType');

        switch (item.action) {
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

    changeReadingType: function (action, record) {

        var me = this, callback,
            router = me.getController('Uni.controller.history.Router');

        var model = Ext.ModelManager.getModel('Mtr.model.ReadingType');
        model.getProxy().setUrl(record.get('mRID'));

        callback = {
            isNotEdit: true,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('readingtypesmanagment.readingTypeActivateDeactivate', 'MTR', 'Reading type {0}', [me.msg]));
                router.getRoute('administration/readingtypes/readingtypes').forward({aliasName: router.arguments.aliasName},
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

        callback.url = '/api/mtr/readingtypes/' + record.get('mRID') + '/activate';
        record.save(callback);
    },
    chooseGroupAction: function (menu, item) {
        var me = this,
            store = me.getStore('Mtr.store.ReadingTypesByAlias'),
            record = menu.record;

        switch (item.action) {
            case 'edit':
                me.msg = Uni.I18n.translate('readingtypesmanagment.saved', 'MTR', 'saved');
                var editWindow = Ext.create('Mtr.view.EditAliasWindow');
                editWindow.down('#edit-save-button').record = record;
                editWindow.setTitle(Uni.I18n.translate('readingtypesmanagment.editalias', 'MTR', 'Edit {0}', record.get('name'), false));
                editWindow.down('textfield').setValue(record.get('name'));
                editWindow.show();
                break;
        }
    },

    changeReadingGroup: function (action, record, aliasName) {

        var me = this,
            router = me.getController('Uni.controller.history.Router');

        var oldName = record.get('name');

        Ext.Ajax.request({
            url: '/api/mtr/readingtypes/groups/' + oldName + '/' + aliasName,
            method: 'PUT',
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('readingtypesmanagment.readingTypeActivateDeactivate', 'MTR', 'Reading type {0}', [me.msg]));
                router.getRoute('administration/readingtypes').forward(null,
                    router.getQueryStringValues()
                );
            }
        });
    },
    changeAlias: function (button) {
        var me = this,
            aliasName = this.getReadingTypesEditAliasWindow().down('form').getValues().aliasName,
            record = button.record,
            action = 'edit';
        if (record.get('name') !== aliasName) {
            this.changeReadingGroup(action, record, aliasName);
        }
        me.getReadingTypesEditAliasWindow().destroy();
    }
});

