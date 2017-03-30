/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.DateTime', {
    extend: 'Uni.view.search.field.internal.Criteria',
    xtype: 'uni-search-criteria-datetime',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine',
        'Uni.form.field.DateTime'
    ],
    text: Uni.I18n.translate('search.field.dateTime.text', 'UNI', 'DateTime'),
    defaults: {
        margin: 0,
        padding: 5
    },
    minWidth: 455,

    onValueChange: function () {
        var value = this.getValue(),
            clearBtn = this.down('#clearall');

        this.callParent(arguments);

        if (clearBtn) {
            clearBtn.setDisabled(!!Ext.isEmpty(value));
        }
    },

    addRangeHandler: function () {
        var me = this;

        me.down('menu').add(me.createCriteriaLine({
            removable: true
        }));
    },

    createCriteriaLine: function(config) {
        var me = this;

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            width: '455',
            operator: 'BETWEEN',
            removable: false,
            operatorMap: {
                '==': 'uni-search-internal-datetimefield',
                //'!=': 'uni-search-internal-datetimefield',
                //'>': 'uni-search-internal-datetimefield',
                //'>=': 'uni-search-internal-datetimefield',
                //'<': 'uni-search-internal-datetimefield',
                //'<=': 'uni-search-internal-datetimefield',
                'BETWEEN': 'uni-search-internal-daterange'
            },
            listeners: {
                change: {
                    fn: me.onValueChange,
                    scope: me
                }
            }
        }, config)
    },

    cleanup: function () {
        var me = this;

        me.items.each(function (item) {
            if (item && item.removable && Ext.isEmpty(item.getValue())) {
                me.remove(item);
            }
        });
    },

    initComponent: function () {
        var me = this;

        me.items = me.createCriteriaLine();
        me.dockedItems = [
            {
                xtype: 'toolbar',
                padding: '0 5 5 5',
                dock: 'bottom',
                style: {
                    'background-color': '#fff !important'
                },
                items: [
                    {
                        xtype: 'button',
                        itemId: 'clearall',
                        text: Uni.I18n.translate('general.clearAll', 'UNI', 'Clear all'),
                        align: 'right',
                        action: 'reset',
                        disabled: true,
                        handler: me.reset,
                        scope : me
                    }
                    //{
                    //    xtype: 'button',
                    //    ui: 'action',
                    //    disabled: true, //until 10.2
                    //    text: Uni.I18n.translate('general.addCriterion', 'UNI', 'Add criterion'),
                    //    action: 'addrange',
                    //    handler: me.addRangeHandler,
                    //    scope : me
                    //}
                ]
            }
        ];

        me.callParent(arguments);
        me.on('show', me.cleanup);
    }
});