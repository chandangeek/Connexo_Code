/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.meters.Meters', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usage-point-history-meters',
    requires: [
        'Imt.usagepointhistory.view.meters.MetersGrid',
        'Imt.usagepointhistory.view.meters.MetersPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormInfoMessage'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            itemId: 'meters-preview-container',
            grid: {
                xtype: 'usage-point-history-meters-grid',
                itemId: 'usage-point-history-meters-grid',
                router: me.router,
                listeners: {
                    select: {
                        fn: Ext.bind(me.onTypeSelect, me)
                    }
                }
            },
            emptyComponent: {
                xtype: 'uni-form-info-message',
                itemId: 'usage-point-history-meters-empty-message',
                text: Uni.I18n.translate('usagePoint.history.meters.empty.title', 'IMT', 'No meters have been linked to this usage point yet')
            },
            previewComponent: {
                xtype: 'usage-point-history-meters-preview',
                itemId: 'usage-point-history-meters-preview',
                router: me.router
            }
        };

        me.on('afterrender', function () {
            me.down('#usage-point-history-meters-grid').getStore().fireEvent('load');
            me.setLoading();
        }, me, {single: true});

        me.callParent(arguments);
    },

    onTypeSelect: function (selectionModel, record) {
        this.down('#usage-point-history-meters-preview').loadRecord(record);
    }
});

