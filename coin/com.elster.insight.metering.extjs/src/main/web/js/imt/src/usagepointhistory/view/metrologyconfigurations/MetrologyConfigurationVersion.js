/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.metrologyconfigurations.MetrologyConfigurationVersion', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metrology-configuration-version-overview',
    requires: [
        'Imt.usagepointhistory.view.metrologyconfigurations.MetrologyConfigurationVersionGrid',
        'Imt.usagepointhistory.view.metrologyconfigurations.MetrologyConfigurationVersionPreview'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            itemId: 'metrology-configuration-version-preview-container',
            hasNotEmptyComponent: true,
            grid: {
                xtype: 'metrology-configuration-version-grid',
                itemId: 'metrology-configuration-version-grid',
                router: me.router,
                listeners: {
                    select: {
                        fn: Ext.bind(me.onVersionSelect, me)
                    }
                }
            },
            previewComponent: {
                router: me.router,
                xtype: 'metrology-configuration-version-preview',
                itemId: 'metrology-configuration-version-preview'
            }
        };

        me.callParent(arguments);
    },

    onVersionSelect: function (selectionModel, record) {
        this.down('#metrology-configuration-version-preview').loadRecord(record);
    }
});
