/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecategories.controller.ServiceCategories', {
    extend: 'Ext.app.Controller',
    stores: [
        'Imt.servicecategories.store.ServiceCategories',
        'Imt.servicecategories.store.CAS'
    ],
    views: [
        'Imt.servicecategories.view.Setup'
    ],

    refs: [
        {
            ref: 'serviceCategoryPreview',
            selector: '#service-categories-setup #service-category-preview'
        },
        {
            ref: 'casPreview',
            selector: '#service-categories-setup #cas-preview'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#service-categories-setup #service-categories-grid': {
                select: me.showServiceCategoryPreview
            },
            '#service-categories-setup #cas-grid': {
                select: me.showServiceCasPreview
            }
        });
    },

    showOverview: function () {
        var me = this,
            widget = Ext.widget('service-categories-setup', {
                itemId: 'service-categories-setup'
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        me.getStore('Imt.servicecategories.store.ServiceCategories').load();
    },

    showServiceCategoryPreview: function (selectionModel, record) {
        var me = this,
            serviceCategoryPreview = me.getServiceCategoryPreview(),
            store = me.getStore('Imt.servicecategories.store.CAS');

        Ext.suspendLayouts();
        serviceCategoryPreview.setTitle(record.get('displayName'));
        serviceCategoryPreview.loadRecord(record);
        Ext.resumeLayouts(true);
        store.getProxy().setExtraParam('serviceCategoryId', record.getId());
        store.load();
    },

    showServiceCasPreview: function (selectionModel, record) {
        var me = this,
            casPreview = me.getCasPreview();

        Ext.suspendLayouts();
        casPreview.setTitle(record.get('name'));
        casPreview.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});