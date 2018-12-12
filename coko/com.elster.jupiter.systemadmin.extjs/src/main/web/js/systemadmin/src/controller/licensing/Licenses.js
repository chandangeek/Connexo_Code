/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.licensing.Licenses', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Sam.store.Licensing'
    ],

    models: [
        'Uni.property.model.Property'
    ],

    views: [
        'licensing.Overview',
        'licensing.Details'
    ],

    refs: [
        {
            ref: 'itemPanel',
            selector: 'licensing-details'
        },
        {
            ref: 'listPanel',
            selector: 'licensing-list'
        }
    ],

    init: function () {
        this.control({
            'licensing-overview #licenses-list': {
                select: this.loadGridItemDetail
            }
        });
    },

    showOverview: function () {
        var me = this;

        me.getApplication().fireEvent('changecontentevent',  Ext.widget('licensing-overview', {
            router: me.getController('Uni.controller.history.Router')
        }));
        me.getStore('Sam.store.Licensing').load();
    },

    loadGridItemDetail: function (grid, record) {
        var me = this,
            itemPanel = this.getItemPanel(),
            licenseCoverageContainer = itemPanel.down('#license-coverage-container');

        itemPanel.setLoading();
        me.getModel('Sam.model.Licensing').load(record.getId(), {
            success: function (record) {
                var content = record.get('content') || [];

                if (itemPanel.rendered) {
                    Ext.suspendLayouts();
                    itemPanel.setTitle(record.get('applicationname'));
                    itemPanel.loadRecord(record);
                    licenseCoverageContainer.setVisible(!!content.length);
                    licenseCoverageContainer.removeAll();
                    Ext.Array.each(content, function (property) {
                        licenseCoverageContainer.add({
                            fieldLabel: Uni.I18n.translate(property.key, 'SAM', property.key),
                            value: property.value.replace(/,/g, '<br>'),
                            htmlEncode: false
                        });
                    });
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                itemPanel.setLoading(false);
            }
        });
    }
});

