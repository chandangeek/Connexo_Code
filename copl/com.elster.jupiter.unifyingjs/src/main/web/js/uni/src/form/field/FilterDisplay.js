/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.FilterDisplay
 */
Ext.define('Uni.form.field.FilterDisplay', {
    extend: 'Ext.form.FieldContainer',
    requires: [
        'Ext.form.field.Display',
        'Ext.button.Button'
    ],
    xtype: 'filter-display',
    emptyText: '',
    layout: 'hbox',

    initComponent: function () {
        var me = this,
            name = me.name;

        me.items = [
            {
                xtype: 'displayfield',
                name: name,
                renderer: function (value, field) {
                    var filterBtn = field.nextSibling('#filter-display-button'),
                        result = value;

                    if (Ext.isFunction(me.renderer)) {
                        result = me.renderer(value, field);
                    }

                    filterBtn.filterValue = value;
                    filterBtn.setVisible(result ? true : false);

                    return result ? result : me.emptyText;
                }
            },
            {
                xtype: 'button',
                itemId: 'filter-display-button',
                filterBy: me.name,
                cls: 'uni-btn-transparent',
                iconCls: 'uni-icon-filter',
                ui: 'blank',
                shadow: false,
                hidden: true,
                margin: '5 0 0 10',
                width: 16
            }
        ];

        me.callParent(arguments);
    }
});