Ext.define('Uni.view.search.field.Numeric', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-numeric',

    text: Uni.I18n.translate('search.field.numeric.text', 'UNI', 'Numeric'),

    requires: [
        'Uni.view.search.field.internal.CriteriaLine'
    ],
    items: [],
    menuConfig: {},
    itemsDefaultConfig: {},

    getValue: function() {
        var value = [];

        this.menu.items.filterBy(function(item){
            return Ext.isFunction(item.getValue);
        }).each(function(item){
            if (!Ext.isEmpty(item.getValue())) {value.push(item.getValue());}
        });

        return Ext.isEmpty(value) ? null : value;
    },

    onInputChange: function () {
        var value = this.getValue(),
            clearBtn = this.down('#clearall');

        if (clearBtn) {
            clearBtn.setDisabled(Ext.isEmpty(value));
        }
        this.setValue(value);
    },

    cleanup: function(menu) {
        menu.items.each(function (item) {
            if (item && item.removable && Ext.isDefined(item.getValue())) {
                menu.remove(item);
            }
        });
    },

    reset: function () {
        var me = this;

        me.menu.items.filterBy(function(item){
            return Ext.isFunction(item.reset);
        }).each(function(item){
            item.reset();
        });

        me.onInputChange();
        me.callParent(arguments);
    },

    addCriteria: function () {
        var me = this;

        me.down('menu').add(me.createCriteriaLine({
            removable: true
        }));
    },

    createCriteriaLine: function(config) {
        var me = this;

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            itemsDefaultConfig: me.itemsDefaultConfig,
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
                    fn: me.onInputChange,
                    scope: me
                }
            }
        }, config)
    },

    initComponent: function () {
        var me = this;

        me.items = me.createCriteriaLine();

        Ext.apply(me.menuConfig, {
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
            ]
        });

        me.callParent(arguments);
    }
});