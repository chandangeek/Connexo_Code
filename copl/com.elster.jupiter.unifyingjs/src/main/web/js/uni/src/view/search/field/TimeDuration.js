Ext.define('Uni.view.search.field.TimeDuration', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-timeduration',

    text: Uni.I18n.translate('search.field.numeric.text', 'UNI', 'Numeric'),

    requires: [
        'Uni.view.search.field.internal.NumberLine'
    ],

    getValue: function () {
        var me = this,
            value = [];

        me.menu.items.filterBy(function (item) {
            return Ext.isFunction(item.getValue);
        }).each(function (item) {
            var itemValue;

            if (!Ext.isEmpty(item.getValue())) {
                itemValue = item.getValue();
                itemValue.set('criteria', itemValue.get('criteria') + ':' + me.getUnitField().getValue());
                value.push(itemValue);
            }
        });

        return Ext.isEmpty(value) ? null : value;
    },

    getUnitField: function () {
        return this.menu.down('combobox[valueField=code]');
    },

    onInputChange: function () {
        var value = this.getValue(),
            clearBtn = this.down('#clearall');

        if (clearBtn) {
            clearBtn.setDisabled(!!Ext.isEmpty(value));
        }
        this.setValue(value);
    },

    cleanup: function(menu) {
        menu.items.each(function (item) {
            if (item && item.removable && Ext.isEmpty(item.getValue())) {
                menu.remove(item);
            }
        });
    },

    reset: function () {
        var me = this;

        me.getUnitField().reset();
        me.menu.items.filterBy(function(item){
            return Ext.isFunction(item.reset);
        }).each(function(item){
            item.reset();
        });

        me.onInputChange();
        this.callParent(arguments);
    },

    addCriteria: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'uni-search-internal-numberline',
            operator: '=',
            removable: true,
            onRemove: function() {
                me.menu.remove(this);
                me.onInputChange();
            },
            listeners: {
                change: {
                    fn: me.onInputChange,
                    scope: me
                }
            }
        });
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'checkbox',
                        boxLabel: Uni.I18n.translate('general.noSpecifiedValue', 'UNI', 'No specified value'),
                        disabled: true,
                        width: 200
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.unit', 'UNI', 'Unit'),
                        store: 'Uni.property.store.TimeUnits',
                        displayField: 'localizedValue',
                        valueField: 'code',
                        forceSelection: false,
                        editable: false,
                        width: 250,
                        queryMode: 'local',
                        value: 14
                    }
                ]
            },
            {
                xtype: 'uni-search-internal-numberline',
                operator: '=',
                listeners: {
                    change: {
                        fn: me.onInputChange,
                        scope: me
                    }
                }
            }
        ];

        me.menuConfig = {
            minWidth: 150,
            defaults: {
                margin: 0,
                padding: 5
            },
            listeners: {
                show: {
                    fn: me.cleanup,
                    scope: me
                }
            },
            dockedItems: [
                {
                    xtype: 'toolbar',
                    padding: '5 10',
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
                            style: {
                                'background-color': '#71adc7'
                            },
                            handler: me.reset,
                            scope : me
                        },
                        {
                            xtype: 'button',
                            ui: 'action',
                            text: Uni.I18n.translate('general.addCriterion', 'UNI', 'Add criterion'),
                            action: 'addcriteria',
                            handler: me.addCriteria,
                            disabled: true,
                            scope : me
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});