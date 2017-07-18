/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * A wrapper over criteria with operator
 */
Ext.define('Uni.view.search.field.internal.CriteriaLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-criterialine',
    layout: 'fit',

    requires: [
        'Uni.view.search.field.internal.Input',
        'Uni.view.search.field.internal.NumberField',
        'Uni.view.search.field.internal.DateTimeField',
        'Uni.view.search.field.internal.DateRange',
        'Uni.view.search.field.internal.NumberRange',
        'Uni.view.search.field.internal.Operator',
        'Uni.model.search.Value',
        'Uni.view.search.field.internal.QuantityField',
        'Uni.view.search.field.internal.QuantityRange'
    ],

    defaults: {
        margin: '0 10 0 0'
    },
    removable: false,
    items: [],
    operator: '==',
    operatorMap: {
        '==': 'uni-search-internal-input',
        '!=': 'uni-search-internal-input',
        '>': 'uni-search-internal-numberfield',
        '>=': 'uni-search-internal-numberfield',
        '<': 'uni-search-internal-numberfield',
        '<=': 'uni-search-internal-numberfield',
        'BETWEEN': 'uni-search-internal-numberrange'
    },
    itemsDefaultConfig: {},

    onOperatorChange: function(operator, value, oldValue) {
        var me = this,
            xtype = this.operatorMap[value];

        if (xtype) {
            Ext.suspendLayouts();

            me.remove(me.getField());
            me.field = me.add(Ext.apply({
                xtype: xtype,
                listeners: {
                    change: function() {
                        me.fireEvent('change', me.getValue())
                    },
                    reset: function() {
                        me.fireEvent('reset')
                    }
                }
            }, me.itemsDefaultConfig));

            Ext.resumeLayouts(true);

            if (me.rendered) {
                me.fireEvent('change', null);
            }
        }
    },

    getField: function () {
        return this.field;
    },

    getValue: function() {
        var value = this.getField().getValue();

        return !Ext.isEmpty(value) ? Ext.create('Uni.model.search.Value', {
            operator: this.down('#filter-operator').getValue(),
            criteria: value
        }) : null;
    },

    setValue: function (value) {
        Ext.suspendLayouts();
        if (!Ext.isEmpty(value)) {
            this.down('#filter-operator').setValue(value.get('operator'));
            this.getField().setValue(value.get('criteria'));
        } else {
            this.reset();
        }
        Ext.resumeLayouts(true);
    },

    isValid: function() {
        var field = this.getField();
        return Ext.isFunction(field.isValid) ? field.isValid() : true;
    },

    reset: function() {
        this.down('#filter-operator').reset();
        this.getField().reset();
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.dockedItems = [{
            xtype: 'toolbar',
            dock: 'left',
            margin: '0 5 0 0',
            padding: 0,
            items: [{
                itemId: 'filter-operator',
                xtype: 'uni-search-internal-operator',
                operator: me.operator,
                operators: _.keys(me.operatorMap),
                margin: 0,
                listeners: {
                    change: {
                        fn: me.onOperatorChange,
                        scope: me
                    }
                }
            }]
        }];

        if (me.removable) {
            me.dockedItems.push({
                dock: 'right',
                width: 15,
                items: {
                    xtype: 'button',
                    itemId: 'filter-clear',
                    ui: 'plain',
                    tooltip: Uni.I18n.translate('search.field.remove', 'UNI', 'Remove filter'),
                    iconCls: ' icon-close4',
                    hidden: true,
                    style: {
                        fontSize: '16px'
                    },
                    handler: me.destroy,
                    scope: me
                }
            });
        }

        me.callParent(arguments);

        if (me.removable) {
            me.on('render', function() {
                var button = me.down('#filter-clear');
                me.getEl().on('mouseover', function () {
                    button.setVisible(true);
                });
                me.getEl().on('mouseout', function () {
                    button.setVisible(false);
                });
            });
        }

        me.down('#filter-operator').setValue(me.operator);
    }
});