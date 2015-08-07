Ext.define('Uni.view.search.field.DateInterval', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-view-search-field-date-field',
    requires: [
        'Uni.view.search.field.internal.DateLine',
        'Uni.view.search.field.internal.DateRange'
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

    clearAllHandler: function () {
        var me = this;
        me.menu.items.filterBy(function(item){
            return Ext.isFunction(item.reset);
        }).each(function(item){
            item.reset();
        });

        me.onInputChange();
    },

    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'uni-view-search-field-date-range',
            listeners: {
                change: {
                    fn: me.onInputChange,
                    scope: me
                }
            }
        });
    },

    onHide: function (menu) {
        if (menu.items.length > 2) {
            menu.items.each(function (item) {
                if (item && !item.default
                    && Ext.isEmpty(item.down('#from').getValue())
                    && Ext.isEmpty(item.down('#to').getValue())
                ){
                    menu.remove(item);
                }
            });
            if (menu.items.length == 2) {
                this.addRangeHandler();
            }
        }
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
                    default: true,
                    operator: '=',
                    hideTime: true,
                    listeners: listeners
                },
                {
                    xtype: 'menuseparator',
                    default: true,
                    padding: 0
                },
                {
                    xtype: 'uni-view-search-field-date-range',
                    listeners: listeners
                }
            ]
        ;

        me.menuConfig = {
            minWidth: 150,
            defaults: {
                padding: 5
            },
            listeners: {
                hide: {
                    fn: me.onHide,
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
                            handler: me.clearAllHandler,
                            scope : me
                        },
                        {
                            xtype: 'button',
                            ui: 'action',
                            text: Uni.I18n.translate('general.addRange', 'UNI', 'Add number range'),
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