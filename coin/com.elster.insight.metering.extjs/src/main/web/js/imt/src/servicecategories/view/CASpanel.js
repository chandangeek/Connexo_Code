/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecategories.view.CASpanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.service-categories-cas-panel',
    requires: [
        'Imt.customattributesets.view.Grid',
        'Imt.customattributesets.view.DetailForm',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            grid: {
                xtype: 'cas-grid',
                itemId: 'cas-grid',
                store: 'Imt.servicecategories.store.CAS',
                dockedConfig: {
                    showTop: true,
                    showBottom: false,
                    showAddBtn: false
                }
            },
            emptyComponent: {
                xtype: 'uni-form-empty-message',
                itemId: 'no-cas-found-panel',
                text: Uni.I18n.translate('serviceCategories.cas.empty.list.item1', 'IMT', 'No custom attribute sets have been added yet.')
            },
            previewComponent: {
                xtype: 'cas-detail-form',
                itemId: 'cas-preview',
                frame: true,
                title: ' '
            }
        };

        me.callParent(arguments);
    }
});