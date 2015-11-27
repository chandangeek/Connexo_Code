Ext.define('Uni.view.search.field.DateTime', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-datetime',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine'
    ],
    text: Uni.I18n.translate('search.field.dateTime.text', 'UNI', 'DateTime'),
    menuConfig: {},

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
            clearBtn.setDisabled(!!Ext.isEmpty(value));
        }

        this.setValue(value);
    },

    reset: function () {
        var me = this;
        me.menu.items.filterBy(function(item){
            return Ext.isFunction(item.reset);
        }).each(function(item){
            item.reset();
        });

        me.onInputChange();
        this.callParent(arguments);
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
            operator: '==',
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
                    fn: me.onInputChange,
                    scope: me
                }
            }
        }, config)
    },

    cleanup: function (menu) {
        menu.items.each(function (item) {
            if (item && item.removable && Ext.isEmpty(item.getValue())) {
                menu.remove(item);
            }
        });
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
                        },
                        {
                            xtype: 'button',
                            ui: 'action',
                            disabled: true, //until 10.2
                            text: Uni.I18n.translate('general.addCriterion', 'UNI', 'Add criterion'),
                            action: 'addrange',
                            handler: me.addRangeHandler,
                            scope : me
                        }
                    ]
                }
            ]
        });

        me.callParent(arguments);
    }
});