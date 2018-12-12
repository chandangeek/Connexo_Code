/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.commander.SortingPanel
 */
Ext.define('Uni.grid.commander.SortingPanel', {
    extend: 'Uni.grid.commander.CommanderPanel',
    xtype: 'uni-grid-commander-sortingpanel',

    items: [
        {
            xtype: 'label',
            text: 'Sort',
            width: 128
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox'
            },
            flex: 1
        },
        {
            xtype: 'button',
            text: 'Add sort'
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