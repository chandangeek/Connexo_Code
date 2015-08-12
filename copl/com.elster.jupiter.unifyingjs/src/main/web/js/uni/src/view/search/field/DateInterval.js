Ext.define('Uni.view.search.field.DateInterval', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-view-search-field-date-field',
    requires: [
        'Uni.view.search.field.internal.DateLine'
    ],
    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Last reading date'),
    emptyText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Last reading date'),

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
        var value = this.getValue();
        this.down('#clearall').setDisabled(!!Ext.isEmpty(value));
        this.onChange(this, value);
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
        me.down('menu').add({
            xtype: 'uni-view-search-field-date-line',
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

    cleanup: function (menu) {
        menu.items.each(function (item) {
            if (item && item.removable && Ext.isEmpty(item.getValue())) {
                menu.remove(item);
            }
        });
    },

    initComponent: function () {
        var me = this,
            listeners = {
                change: {
                    fn: me.onInputChange,
                    scope: me
                }
            };

        me.items = [
                {
                    xtype: 'uni-view-search-field-date-line',
                    operator: '=',
                    listeners: listeners
                }
            ]
        ;

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
                            action: 'addrange',
                            handler: me.addRangeHandler,
                            scope : me
                        }
                    ]
                }
            ]
        };

        this.callParent(arguments);
    }
});