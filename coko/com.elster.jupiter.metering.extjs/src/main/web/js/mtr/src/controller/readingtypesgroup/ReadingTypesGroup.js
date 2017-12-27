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
        'Mtr.store.readingtypesgroup.ReadingTypesByAlias'
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
        }
    ],

    init: function () {
        this.control({
            '#add-reading-type-button': {
                click: this.browseAdd
            },
            // '#overview-add-reading-type-button': {
            //     click: this.browseAdd
            // },
            '#mtr-add-readingTypeGroup-button': {
                click: this.browseGroupAdd
            },
            '#mtr-reading-type-groups-overview-add-button': {
                click: this.browseGroupAdd
            },
            'reading-type-groups-grid': {
                select: this.showPreview
            },
            'reading-types-in-group-grid': {
                select: this.showReadingTypePreview
            }
        });
    },

    showReadingTypePreview: function(selectionModel, record) {
        var me = this;
        me.getPreview().setTitle(Ext.String.htmlEncode(record.get('fullAliasName')));
        me.getPreviewForm().loadRecord(record);
    },

    showOverview: function () {
        var me = this,
            widget,
            store = me.getStore('Mtr.store.readingtypesgroup.ReadingTypeGroups');
        widget = Ext.widget('reading-type-groups-overview', {router: me.getController('Uni.controller.history.Router')});
        me.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    showPreview: function (selectionModel, record) {
        var me = this;

        me.getGroupPreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        me.getGroupPreviewForm().loadRecord(record);
    },

    browseGroupAdd: function () {
        var router = this.getController('Uni.controller.history.Router'),
            addController = this.getController('Mtr.controller.readingtypesgroup.AddReadingTypesGroup');
        addController.qString = router.getQueryStringValues();
        // router.getRoute('administration/readingtypegroups/add').forward();  //lori
        router.getRoute('administration/readingtypes1/add').forward();
    },

    browseAdd: function () {
        var router = this.getController('Uni.controller.history.Router'),
            addController = this.getController('Mtr.controller.readingtypesgroup.AddReadingTypesGroup');
        addController.qString = router.getQueryStringValues();
        router.getRoute('administration/readingtypes/add').forward();
    },

    showReadingTypesGroupDetails: function (aliasName) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            groupModel = me.getModel('Mtr.model.readingtypesgroup.ReadingTypeGroup'),
            view = Ext.widget('reading-type-groups-details', {
                router: router
            });


        me.fromDetail = true;
        me.getApplication().fireEvent('changecontentevent', view);
        groupModel.load(aliasName, {
            success: function (record) {
                var detailsForm = view.down('readingTypesGroup-preview-form');
                me.getApplication().fireEvent('readingtypesgroupload', record);
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
        var store = Ext.getStore('Mtr.store.readingtypesgroup.ReadingTypesByAlias');
        store.getProxy().setUrl(aliasName);
        store.load();
    }
});

