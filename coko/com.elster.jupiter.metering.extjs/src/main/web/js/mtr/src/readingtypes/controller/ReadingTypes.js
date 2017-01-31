/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.controller.ReadingTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.readingtypes.view.Overview',
        'Mtr.readingtypes.view.Grid',
        'Mtr.readingtypes.view.EditAliasWindow',
        'Mtr.readingtypes.util.FilterTopPanel'
    ],

    requires: [],

    stores: [
        'Mtr.readingtypes.store.ReadingTypes'
    ],


    refs: [
        {
            ref: 'page',
            selector: '#reading-types-setup'
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
            ref: 'readingTypesFilterPanel',
            selector: '#reading-types-filter-top-panel'
        }
    ],

    init: function () {
        this.control({
            'metering-reading-types-grid': {
                select: this.showPreview
            },
            'reading-types-action-menu': {
                click: this.chooseAction
            },
            'edit-alias-window #edit-save-button': {
                click: this.changeAlias
            },
            '#add-reading-type-button' : {
                click: this.browseAdd
            },
            '#overview-add-reading-type-button' : {
                click: this.browseAdd
            },
            '#reading-types-bulk-action-button': {
                click: this.showBulkAction
            },
            '#edit-alias-window-textfield': {
                change: this.disableSaveButton
            }
        });
    },


    showOverview: function () {
        var me = this, widget,
            sorting = me.getController('Mtr.readingtypes.controller.OverviewSorting'),
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.setCurrentSort();
        widget = Ext.widget('reading-types-setup');
        me.getApplication().fireEvent('changecontentevent', widget);
        sorting.updateSortingToolbar();
        store.load();
    },

    setCurrentSort: function () {
        var me = this,
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes'),
            sorting = store.getProxy().extraParams['sort'];

        if (sorting === undefined || sorting === '[]') { // set default filters
            sorting = [];
            sorting.push({
                property: 'fullAliasName',
                direction: Uni.component.sort.model.Sort.ASC
            });
            store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        }
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            menu = me.getReadingTypesPreviewMenu();
        if(menu) menu.record = record;
        me.getPreview().setTitle(Ext.String.htmlEncode(record.get('fullAliasName')));
        me.getPreviewForm().loadRecord(record);
    },

    chooseAction: function (menu, item) {
        var me = this,
            gridView = me.getReadingTypesGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected(),
            model = me.getModel('Mtr.readingtypes.model.ReadingType');

        switch (item.action) {
            case 'edit':
                me.msg = Uni.I18n.translate('readingtypesmanagment.saved', 'MTR', 'saved');
                var editWindow = Ext.create('Mtr.readingtypes.view.EditAliasWindow');
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

        var model = Ext.ModelManager.getModel('Mtr.readingtypes.model.ReadingType');
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
                        title = Uni.I18n.translate('readingtypesmanagment.readingTypeNotActivateDeactivate', 'MTR', 'Reading type did not {0}', [me.msg]),
                        message = Uni.I18n.translate('general.serverError', 'MTR', 'Server error');
                    if (!Ext.isEmpty(operation.response.statusText)) {
                        message = operation.response.statusText;
                    }
                    if (result && result.message) {
                        message = result.message;
                    } else if (result && result.error) {
                        message = result.error;
                    }
                    me.getApplication().getController('Uni.controller.Error').showError(title, message);
                }
            }
        };

        if(action != 'edit') {
            callback.url = '/api/mtr/readingtypes/' + record.get('mRID') + '/activate';
        }
        record.save(callback);
    },

    browseAdd: function(){
        var router = this.getController('Uni.controller.history.Router'),
            addController = this.getController('Mtr.readingtypes.controller.AddReadingTypes');
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
            filterPanel = me.getPage().down('reading-types-filter-top-panel'),
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes'),
            storeBulk = me.getStore('Mtr.readingtypes.store.ReadingTypesBulk');
        storeBulk.setProxy(store.getProxy());
        storeBulk.getProxy().setExtraParam('filter',
            filterPanel.createFiltersObject(filterPanel.getFilterParams())
        );
        router.getRoute('administration/readingtypes/bulk').forward(null,
            router.getQueryStringValues()
        );
    }
});

