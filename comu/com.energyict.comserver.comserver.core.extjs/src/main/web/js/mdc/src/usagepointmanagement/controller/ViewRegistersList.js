/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.controller.ViewRegistersList', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.usagepointmanagement.model.UsagePoint'
    ],

    stores: [
        'Mdc.usagepointmanagement.store.Registers'
    ],

    views: [
        'Mdc.usagepointmanagement.view.ViewRegistersList'
    ],

    refs: [
        {
            ref: 'preview',
            selector: '#view-registers-list #usage-point-register-preview'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#view-registers-list #usage-point-registers-grid': {
                select: me.showPreview
            }
        });
    },

    showOverview: function (usagePointId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            registersStore = me.getStore('Mdc.usagepointmanagement.store.Registers'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('view-registers-list', {
                itemId: 'view-registers-list',
                router: router,
                usagePointId: usagePointId
            });

        pageMainContent.setLoading();
        me.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(usagePointId, {
            success: function (usagePoint) {
                app.fireEvent('usagePointLoaded', usagePoint);
                app.fireEvent('changecontentevent', widget);
                registersStore.getProxy().setExtraParam('usagePointId', usagePointId);
                registersStore.load();
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview();

        Ext.suspendLayouts();
        preview.setTitle(record.get('readingType').fullAliasName);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});