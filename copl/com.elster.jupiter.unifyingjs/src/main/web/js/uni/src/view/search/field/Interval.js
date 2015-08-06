Ext.define('Uni.view.search.field.Interval', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-view-search-field-number-field',

    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    emptyText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    requires: [
        'Uni.view.search.field.internal.NumberLine',
        'Uni.view.search.field.internal.NumberRange'
    ],

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
            if (menu.items.length == 2)
                menu.add({
                    xtype: 'uni-view-search-field-number-range'
                });
        }
    },

    clearAllHandler: function () {
        var me = this;
        var items = me.down('menu').items.items;
        Ext.each(items, function (item) {
            if (item.xtype == 'uni-view-search-field-number-line' && item.xtype != 'menuseparator')
                item.down('numberfield').reset();
            if (item.xtype == 'uni-view-search-field-number-range' && item.xtype != 'menuseparator') {
                var moreNumber = item.down('#more-value').down('numberfield');
                var smallerNumber = item.down('#smaller-value').down('numberfield');

                moreNumber.reset();
                smallerNumber.reset();
            }
        });
        me.setText(me.defaultText)
    },

    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'uni-view-search-field-number-range'
        });
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'uni-view-search-field-number-line',
                default: true,
                operator: '='
            },
            {
                xtype: 'menuseparator',
                default: true,
                padding: 0
            },
            {
                xtype: 'uni-view-search-field-number-range'
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
                //click: function (menu) {
                //    var edited = false;
                //    menu.items.each(function (item, index) {
                //
                //        if (item.down('numberfield').getValue() != 0
                //            && item.xtype == 'uni-view-search-field-number-line'
                //            && item.xtype != 'menuseparator') {
                //            edited = true
                //        }
                //
                //
                //        if (item.xtype == 'uni-view-search-field-number-range'
                //            && item.xtype != 'menuseparator') {
                //            var moreNumber = item.down('#more-value').down('numberfield');
                //            var smallerNumber = item.down('#smaller-value').down('numberfield');
                //
                //            if (moreNumber.getValue() != 0
                //                || smallerNumber.getValue() != 0) {
                //                edited = true;
                //            }
                //        }
                //    });
                //    var mainButton = menu.up('uni-view-search-field-number-field');
                //    var clearAllButton = menu.down('#clearall');
                //
                //    if (edited) {
                //        mainButton.setText(me.defaultText + '*');
                //        clearAllButton.enable(true)
                //    } else {
                //        mainButton.setText(me.defaultText);
                //        clearAllButton.disable(true)
                //    }
                //}
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
                            //style: {
                            //    'background-color': '#71adc7'
                            //},
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