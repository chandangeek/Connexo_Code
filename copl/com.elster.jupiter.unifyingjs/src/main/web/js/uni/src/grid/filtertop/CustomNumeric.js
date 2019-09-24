/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.filtertop.CustomNumeric', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-filtertop-customnumeric',
    mixins: [
        'Uni.grid.filtertop.Base'
    ],
    margin: '0 0 10 0',


    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'button',
                action: 'chooseValues',
                text: me.text,
                menu: [
                    {
                        xtype: 'uni-search-criteria-numeric',
                        itemId: 'uni-search-criteria-numeric',
                        minWidth: undefined,
                        width: 280,
                        itemsDefaultConfig: {
                            isFilterField: false
                        },
                        customOperatorMap: {
                            '==': 'uni-search-internal-input',
                            '!=': 'uni-search-internal-input',
                        }

                    }
                ],
                listeners: {
                    menuhide: function (btn, menu) {
                        menu.query('numberfield').map(function (fld) {
                            if (!fld.isValid()) {
                                fld.clearInvalid();
                            }
                        });
                    }
                }
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

        if (value) {
            me.updateTitle();
        }

        return value != null ? value[0].data : undefined;
    },

    setFilterValue: function (data) {
        var me = this;

        me.down('#uni-search-criteria-numeric').setValue([{
            get: function (id) {
                return data[id];
            }
        }]);

        me.updateTitle();
    },

    onApplyValues: function () {
        var me = this;

        me.query('numberfield').map(function (fld) {
            if (!fld.isValid()) {
                fld.clearInvalid();
            }
        });

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

        me.down('button[action=chooseValues]').setText(value ? me.text + '&nbsp;&nbsp;(' + value.length + ')' : me.text);
    },

    reset: function () {
        var me = this;
        me.updateHref();
        me.down('#uni-search-criteria-numeric').reset();
        me.down('button[action=chooseValues]').setText(me.text);
    },

    updateHref: function () {
        var me = this,
            queryStringValues = Uni.util.QueryString.getQueryStringValues(false),
            url = location.href.split('?')[0],
            key = Object.keys(queryStringValues).find(function (key) {
                return key.split('.')[0] == me.dataIndex;
            });

        if (key) {
            delete queryStringValues[me.dataIndex + ".operator"];
            delete queryStringValues[me.dataIndex + ".criteria"];
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            location.href =  url + '?' + Ext.Object.toQueryString(queryStringValues, false);
        }
    }
});