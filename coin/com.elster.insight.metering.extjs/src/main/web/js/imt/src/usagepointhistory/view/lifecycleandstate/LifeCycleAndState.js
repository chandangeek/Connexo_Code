/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndState', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.life-cycle-and-state',
    xtype: 'life-cycle-and-state',
    requires: [
        'Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndStateGrid',
        'Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndStatePreview'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            itemId: 'life-cycle-and-state-preview-container',
            hasNotEmptyComponent: true,
            grid: {
                xtype: 'life-cycle-and-state-grid',
                itemId: 'life-cycle-and-state-grid',
                listeners: {
                    select: {
                        fn: Ext.bind(me.onTypeSelect, me)
                    }
                }
            },
            previewComponent: {
                xtype: 'life-cycle-and-state-preview',
                itemId: 'life-cycle-and-state-preview'
            }
        };

        me.callParent(arguments);
    },

    onTypeSelect: function (selectionModel, record) {
        this.down('#life-cycle-and-state-preview').loadRecord(record);
    }
});
