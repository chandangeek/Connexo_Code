/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.search.field.internal.Adapter
 */
Ext.define('Uni.view.search.field.internal.Adapter', {
    extend: 'Ext.container.Container',
    xtype: 'uni-search-internal-adapter',

    layout: {
        type: 'hbox'
    },

    /**
     * Widget that needs to be wrapped.
     */
    widget: undefined,

    /**
     * Search property to be wrapped.
     */
    property: undefined,

    initComponent: function () {
        var me = this;

        if (Ext.isDefined(me.widget)) {
            me.property = me.widget.property;
            me.dataIndex = me.widget.dataIndex;

            me.defaults = {
                margin: 0
            };

            me.items = [
                me.widget,
                {
                    margin: '0 0 0 2',
                    xtype: 'button',
                    iconCls: 'icon-cancel-circle2',
                    action: 'remove'
                }
            ];
        }

        me.callParent(arguments);

        me.down('uni-search-internal-button').on('destroy', function(){
            me.destroy();
        });

        if (Ext.isDefined(me.widget)) {
            me.down('button[action=remove]').on('click', me.removeHandler, me);
        }
    }
});

