Ext.define('Uni.view.search.field.NumberField', {
    extend: 'Ext.button.Button',
    xtype: 'uni-view-search-field-number-field',
    textAlign: 'left',
    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    defaultText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    arrowAlign: 'right',
    menuAlign: 'tr-br',
    requires: [
        'Uni.view.search.field.NumberLine',
        'Uni.view.search.field.NumberRange'
    ],
    layout: 'hbox',
    width: 150,
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
                    var moreNumber = item.down('#more-value').down('numberfield');
                    var smallerNumber = item.down('#smaller-value').down('numberfield');

                    moreNumber.reset();
                    smallerNumber.reset();
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
                                xtype: 'uni-view-search-field-number-range',
                                margin: '5px 0px 5px 5px'
                            });
                    }

                },
                click: function (menu) {
                    var edited = false;
                    menu.items.each(function (item, index) {

                        if (item.xtype != 'menuseparator') {
                            if (item.xtype == 'uni-view-search-field-number-line') {
                                if (item.down('numberfield').getValue() != 0) {
                                    edited = true
                                }
                            }

                            if (item.xtype == 'uni-view-search-field-number-range') {
                                var moreNumber = item.down('#more-value').down('numberfield');
                                var smallerNumber = item.down('#smaller-value').down('numberfield');

                                if (moreNumber.getValue() != 0
                                    || smallerNumber.getValue() != 0) {
                                    edited = true;
                                }
                            }
                        }
                    });
                    var mainButton = menu.up('uni-view-search-field-number-field');
                    var clearAllButton = menu.down('#clearall');

                    if (edited) {
                        mainButton.setText(me.defaultText + '*');
                        clearAllButton.enable(true)
                    } else {
                        mainButton.setText(me.defaultText);
                        clearAllButton.disable(true)
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
                            disabled: true,
                            style: {
                                'background-color': '#71adc7'
                            },
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