/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.Numeric', {
    extend: 'Uni.view.search.field.internal.Criteria',
    xtype: 'uni-search-criteria-numeric',
    text: Uni.I18n.translate('search.field.numeric.text', 'UNI', 'Numeric'),
    requires: [
        'Uni.view.search.field.internal.CriteriaLine'
    ],
    items: [],
    itemsDefaultConfig: {},
    validateOnChange: true,
    minWidth: 455,
    defaults: {
        margin: 0,
        padding: 5
    },

    onValueChange: function () {
        var value = this.getValue(),
            clearBtn = this.down('button[action=reset]');

        this.callParent(arguments);

        if (clearBtn) {
            clearBtn.setDisabled(!!Ext.isEmpty(value));
        }
    },

    cleanup: function () {
        var me = this;

        me.items.each(function (item) {
            if (item && item.removable && Ext.isEmpty(item.getValue())) {
                me.remove(item);
            }
        });
    },

    addCriteria: function () {
        var me = this;

        me.add(me.createCriteriaLine({
            removable: true
        }));
    },

    createCriteriaLine: function(config) {
        var me = this;

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            itemsDefaultConfig: me.itemsDefaultConfig,
            validateOnChange: me.validateOnChange,
            width: '455',
            operator: '==',
            removable: false,
            operatorMap: {
                '==': 'uni-search-internal-numberfield',
                //'!=': 'uni-search-internal-numberfield',
                //'>': 'uni-search-internal-numberfield',
                //'>=': 'uni-search-internal-numberfield',
                //'<': 'uni-search-internal-numberfield',
                //'<=': 'uni-search-internal-numberfield',
                'BETWEEN': 'uni-search-internal-numberrange'
            },
            listeners: {
                change: {
                    fn: me.onValueChange,
                    scope: me
                }
            }
        }, config);
    },

    initComponent: function () {
        var me = this;

        me.items = me.createCriteriaLine();
        me.dockedItems = [
            {
                xtype: 'toolbar',
                itemId: 'criteria-toolbar',
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
                    //    text: Uni.I18n.translate('general.addCriterion', 'UNI', 'Add criterion'),
                    //    action: 'addcriteria',
                    //    handler: me.addCriteria,
                    //    disabled: true,
                    //    scope : me
                    //}
                ]
            }
        ];

        me.callParent(arguments);
        me.on('show', me.cleanup);
    }
});