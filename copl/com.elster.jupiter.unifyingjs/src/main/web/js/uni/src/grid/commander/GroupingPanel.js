/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.commander.GroupingPanel
 */
Ext.define('Uni.grid.commander.GroupingPanel', {
    extend: 'Uni.grid.commander.CommanderPanel',
    xtype: 'uni-grid-commander-groupingpanel',

    items: [
        {
            xtype: 'label',
            text: 'Groups',
            width: 128
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox'
            },
            flex: 1
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.reconfigureStore(me.store);
    },

    reconfigureStore: function (store) {
        // TODO
    }
});