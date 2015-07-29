Ext.define('Uni.view.search.field.NumberField', {
    extend: 'Ext.button.Button',
    alias: 'widget.uni-view-search-field-number-field',
    xtype: 'uni-view-search-field-number-field',
    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    defaultText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    arrowAlign: 'right',
    menuAlign: 'tr-br',
    requires: [
        'Uni.view.search.field.NumberLine',
        'Uni.view.search.field.NumberRange'
    ],
    layout: 'hbox',
    style: {
        'background-color': '#71adc7'
    },

    clearAllHandler: function () {
        var me = this;
        var items = me.down('menu').items.items;
        Ext.each(items, function (item) {
            if (item.xtype != 'menuseparator') {

                if (item.xtype == 'uni-view-search-field-number-line')
                    item.down('numberfield').reset();
                if (item.xtype == 'uni-view-search-field-number-range') {
                    item.items.items[0].down('numberfield').reset();
                    item.items.items[1].down('numberfield').reset();
                }
            }
        });
        me.setText(me.defaultText)
    },
    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'uni-view-search-field-number-range',
        });
    },

    initComponent: function () {
        var me = this;
        me.menu = {
            plain: true,
            style: {
                overflow: 'visible'
            },
            cls: 'x-menu-body-custom',
            minWidth: 300,
            items: [
                {
                    xtype: 'uni-view-search-field-number-line',
                    margin: '5px 5px 3px 5px',
                    default: true,
                    operator: '='
                },
                {
                    xtype: 'menuseparator',
                    default: true
                },
                {
                    xtype: 'uni-view-search-field-number-range',
                    margin: '5px 0px 5px 5px'
                }
            ],
            listeners: {
                hide: function (menu) {
                    if (menu.items.length > 2) {
                        menu.items.each(function (item, index) {
                            if (item && !item.default &&
                                item.items.items[0].down('numberfield').getValue() === 0 &&
                                item.items.items[1].down('numberfield').getValue() === 0) {
                                menu.remove(item);
                            }
                        });
                        if (menu.items.length == 2)
                        menu.add({
                            xtype: 'uni-view-search-field-number-range',
                        });
                    }

                }
            },
            dockedItems: [
                {
                    xtype: 'toolbar',
                    background: '#71adc7',
                    dock: 'bottom',
                    items: [
                        {
                            flex: 2,
                            cls: 'x-spacers'
                        },
                        {
                            xtype: 'button',
                            itemId: 'clearall',
                            text: Uni.I18n.translate('general.clearAll', 'UNI', 'Clear all'),
                            align: 'right',
                            handler: function () {
                                me.clearAllHandler();
                            }
                        },
                        {
                            xtype: 'button',
                            ui: 'action',
                            text: Uni.I18n.translate('general.addRange', 'UNI', 'Add number range'),
                            action: 'addrange',
                            margin: '0 0 0 0',
                            handler: function () {
                                me.addRangeHandler();
                            }
                        },
                        {
                            flex: 1,
                            cls: 'x-spacers'
                        }
                    ]
                }
            ]
        };


        this.callParent(arguments);
    }
});