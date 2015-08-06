Ext.define('Uni.view.search.field.Interval', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-view-search-field-number-field',

    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    emptyText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    requires: [
        'Uni.view.search.field.internal.NumberLine',
        'Uni.view.search.field.internal.NumberRange'
    ],

    getValue: function() {
        var value = [];

        this.menu.items.filterBy(function(item){
            return Ext.isFunction(item.getValue);
        }).each(function(item){
            value.push(item.getValue());
        });

        return value;
    },

    onInputChange: function () {
        var value = this.getValue();
        this.down('#clearall').setDisabled(!!Ext.isEmpty(value));
        this.onChange(this, value);
    },

    onHide: function() {
        var me = this,
            menu = me.menu;

        if (menu.items.length > 2) {
            menu.items.each(function (item, index) {
                if (!item.default) {
                    var moreNumber = item.down('#more-value').down('numberfield');
                    var smallerNumber = item.down('#smaller-value').down('numberfield');
                }
                if (item && !item.default
                    && moreNumber.getValue() === 0
                    && smallerNumber.getValue() === 0) {
                    menu.remove(item);
                }
            });
            if (menu.items.length == 2) {
                me.addRangeHandler();
            }
        }
    },

    clearAllHandler: function () {
        var me = this;

        this.menu.items.filterBy(function(item){
            return Ext.isFunction(item.reset);
        }).each(function(item){
            item.reset();
        });

        me.onInputChange();
        this.down('#clearall').setDisabled(true);
    },

    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'uni-view-search-field-number-range',
            listeners: {
                change: {
                    fn: me.onInputChange,
                    scope: me
                }
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
                xtype: 'uni-view-search-field-number-line',
                default: true,
                operator: '=',
                listeners: listeners
            },
            {
                xtype: 'menuseparator',
                default: true,
                padding: 0
            },
            {
                xtype: 'uni-view-search-field-number-range',
                listeners: listeners
            }
        ];

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

        me.callParent(arguments);
    }
});