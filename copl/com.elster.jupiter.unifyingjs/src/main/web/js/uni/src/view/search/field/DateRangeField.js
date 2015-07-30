Ext.define('Uni.view.search.field.DateRangeField', {
    extend: 'Ext.button.Button',
    alias: 'widget.uni-view-search-field-date-field',
    xtype: 'uni-view-search-field-date-field',
    requires: [
        'Uni.view.search.field.DateLine',
        'Uni.view.search.field.DateRange'
    ],
    itemId: 'date',
    textAlign: 'left',
    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Last reading date'),
    defaultText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Last reading date'),
    arrowAlign: 'right',
    menuAlign: 'tr-br',
    width: 150,
    style: {
        'background-color': '#71adc7'
    },
    layout: 'hbox',
    clearAllHandler: function () {
        var me = this;
        var items = me.down('menu').items.items;
        Ext.each(items, function (item) {
            if (item.xtype != 'menuseparator') {
                if (item.xtype == 'uni-view-search-field-date-line')
                    if (item.down('datefield')) item.down('datefield').reset();
                    if (item.down('#hours')) item.down('#hours').reset();
                    if (item.down('#minutes')) item.down('#minutes').reset();
                if (item.xtype == 'uni-view-search-field-date-range') {
                    if (item.items.items[0].down('datefield')) {
                        item.items.items[0].down('datefield').reset();
                        if (item.items.items[0].down('datefield').minValue) item.items.items[0].down('datefield').minValue = null;
                        if (item.items.items[0].down('datefield').maxValue) item.items.items[0].down('datefield').maxValue = null;
                    }
                    if (item.items.items[0].down('#hours')) item.items.items[0].down('#hours').reset();
                    if (item.items.items[0].down('#minutes')) item.items.items[0].down('#minutes').reset();
                    if (item.items.items[1].down('datefield')) {
                        item.items.items[1].down('datefield').reset();
                        if (item.items.items[1].down('datefield').minValue) item.items.items[1].down('datefield').minValue = null;
                        if (item.items.items[1].down('datefield').maxValue) item.items.items[1].down('datefield').maxValue = null;
                    }
                    if (item.items.items[1].down('#hours')) item.items.items[1].down('#hours').reset();
                    if (item.items.items[1].down('#minutes')) item.items.items[1].down('#minutes').reset();
                }
            }
        });
        me.setText( me.defaultText)
    },
    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'uni-view-search-field-date-range',
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
                    xtype: 'uni-view-search-field-date-line',
                    margin: '5px 5px 3px 5px',
                    default: true,
                    operator: '=',
                    hideTime: true
                },
                {
                    xtype: 'menuseparator',
                    default: true
                },
                {
                    xtype: 'uni-view-search-field-date-range',
                    margin: '5px 0px 5px 5px'
                }
            ],
            listeners: {
                hide: function (menu) {
                    if (menu.items.length > 2) {
                        menu.items.each(function (item, index) {
                            if (item && !item.default &&
                                item.items.items[0].down('datefield').getValue() == null &&
                                item.items.items[0].down('#hours').getValue() == 0 &&
                                item.items.items[0].down('#minutes').getValue() == 0 &&
                                item.items.items[1].down('datefield').getValue() == null &&
                                item.items.items[1].down('#hours').getValue() == 0 &&
                                item.items.items[1].down('#minutes').getValue() == 0) {
                                menu.remove(item);
                            }
                        });
                        if (menu.items.length == 2)
                            menu.add({
                                xtype: 'uni-view-search-field-date-range',
                                margin: '10px 0px 5px 5px'
                            });
                    }

                },
                click: function (menu) {
                    // setting of text to component button (+ * logic) and enable/disable 'clear all' button
                    var edited = false;
                    menu.items.each(function (item, index) {
                        if (item.xtype != 'menuseparator') {
                            if (item.xtype == 'uni-view-search-field-date-line')
                                if (item.down('datefield').getValue() != null || item.down('#hours').getValue() != 0 || item.down('#minutes').getValue() != 0) edited = true
                            if (item.xtype == 'uni-view-search-field-date-range') {
                                if (item.items.items[0].down('datefield').getValue() != null || item.items.items[0].down('#hours').getValue() != 0 ||
                                    item.items.items[0].down('#minutes').getValue() != 0 ||
                                    item.items.items[1].down('datefield').getValue() != null || item.items.items[1].down('#hours').getValue() != 0 ||
                                    item.items.items[1].down('#minutes').getValue() != 0) edited = true;

                                // setting date constraints
                                if (item.items.items[0].down('datefield').getValue() != null) item.items.items[1].down('datefield').setMinValue(item.items.items[0].down('datefield').getValue())
                                if (item.items.items[1].down('datefield').getValue() != null) item.items.items[0].down('datefield').setMaxValue(item.items.items[1].down('datefield').getValue())
                            }
                        }
                    });
                    if (edited) {
                        menu.up('uni-view-search-field-date-field').setText(me.defaultText + '*');
                        menu.down('#clearall').enable(true)
                    } else {
                        menu.up('uni-view-search-field-date-field').setText(me.defaultText);
                        menu.down('#clearall').disable(true)
                    }
                }
            },
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    cls: 'x-docked-bottom-date-field',
                    items: [
                        {
                            flex: 6,
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
                            text: Uni.I18n.translate('general.addRange', 'UNI', 'Add date range'),
                            action: 'addrange',
                            margin: '0 0 0 0',
                            handler: function () {
                                me.addRangeHandler();
                            }
                        },
                        {
                            flex: 0.8,
                            cls: 'x-spacers'
                        }
                    ]
                }
            ]
        };

        this.callParent(arguments);
    }
});