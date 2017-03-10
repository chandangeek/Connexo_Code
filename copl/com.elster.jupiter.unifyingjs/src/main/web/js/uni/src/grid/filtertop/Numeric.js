/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by david on 6/10/2016.
 */
Ext.define('Uni.grid.filtertop.Numeric', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-filtertop-numeric',
    mixins: [
        'Uni.grid.filtertop.Base'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'button',
                action: 'chooseValues',
                text: me.text,
                style: 'margin-right: 0 !important;',
                textAlign: 'left',
                menu: [
                    {
                        width: 100,
                        xtype: 'uni-search-criteria-numeric',
                        itemId: 'uni-search-criteria-numeric',
                        validateOnChange: false
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.items.items[0].menu.onMouseOver = Ext.emptyFn;
        me.down('#criteria-toolbar').insert(0, {
            xtype: 'button',
            ui: 'action',
            action: 'apply',
            text: Uni.I18n.translate('general.apply', 'UNI', 'Apply')
        });
        me.doLayout();

        me.down('button[action=apply]').on('click', me.onApplyValues, me);
        me.down('button[action=reset]').on('click', me.onClearValues, me);
    },

    getParamValue: function () {
        var me = this,
            value = me.down('#uni-search-criteria-numeric').getValue();
        return value != null ? JSON.stringify(value[0].data) : undefined;
    },

    setFilterValue: function (data) {
        var me = this,
            data = JSON.parse(data);
        me.down('#uni-search-criteria-numeric').setValue([{
            get: function (id) {
                return data[id];
            }
        }]);
        me.updateTitle();
    },

    onApplyValues: function () {
        var me = this;

        if (me.down('uni-search-criteria-numeric').isValid()) {
            me.fireFilterUpdateEvent();
            me.down('button[action=chooseValues]').hideMenu();
            me.updateTitle();
        }
    },

    onClearValues: function () {
        var me = this;

        me.reset();
        me.fireFilterUpdateEvent();
        me.down('button[action=chooseValues]').hideMenu();
    },

    updateTitle: function () {
        var me = this,
            value = me.down('#uni-search-criteria-numeric').getValue();
        me.down('button[action=chooseValues]').setText(value ? me.text + '&nbsp;(' + value.length + ')' : me.text);
    },

    reset: function () {
        var me = this;
        me.down('#uni-search-criteria-numeric').reset();
        me.down('button[action=chooseValues]').setText(me.text);
    }
});