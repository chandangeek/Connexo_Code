Ext.define('Uni.view.search.field.Numeric', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-numeric',

    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    emptyText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),

    requires: [
        'Uni.view.search.field.internal.NumberLine'
    ],

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
        this.onChange(this, value);
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
                            scope : me
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});