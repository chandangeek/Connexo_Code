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
                if (item.xtype == 'uni-view-search-field-date-line') {
                    var date = item.down('datefield');
                    var hours = item.down('#hours');
                    var minutes = item.down('#minutes');

                    if (date) {
                        date.reset();
                    }
                    if (hours) {
                        hours.reset();
                    }
                    if (minutes) {
                        minutes.reset();
                    }
                }
                if (item.xtype == 'uni-view-search-field-date-range') {
                    // reset more block
                    var moreLine = item.down('#more-value');
                    var moreDate = moreLine.down('datefield');
                    if (moreDate) {
                        moreDate.reset();
                        // reset picker border values
                        if (moreDate.minValue) {
                            moreDate.setMinValue(null);
                        }
                        if (moreDate.maxValue) {
                            moreDate.setMaxValue(null);
                        }
                    }
                    if (moreLine.down('#hours')) {
                        moreLine.down('#hours').reset();
                    }
                    if (moreLine.down('#minutes')) {
                        moreLine.down('#minutes').reset();
                    }
                    // reset smaller block
                    var smallerLine = item.down('#smaller-value');
                    var smollerDate = smallerLine.down('datefield');

                    if (smollerDate) {
                        smollerDate.reset();

                        // reset picker border values
                        if (smollerDate.minValue) {
                            smollerDate.setMinValue(null);
                        }
                        if (smollerDate.maxValue) {
                            smollerDate.setMaxValue(null);
                        }
                    }
                    if (smallerLine.down('#hours')) {
                        smallerLine.down('#hours').reset();
                    }
                    if (smallerLine.down('#minutes')) {
                        smallerLine.down('#minutes').reset();
                    }
                }
            }
        });
        me.setText(me.defaultText)
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
                            var moreLine = item.down('#more-value');
                            var smallerLine = item.down('#smaller-value');
                            if (item && !item.default
                                && moreLine.down('datefield').getValue() == null
                                && moreLine.down('#hours').getValue() == 0
                                && moreLine.down('#minutes').getValue() == 0
                                && smallerLine.down('datefield').getValue() == null
                                && smallerLine.down('#hours').getValue() == 0
                                && smallerLine.down('#minutes').getValue() == 0) {
                                menu.remove(item);
                            }
                        });
                        if (menu.items.length == 2)
                            menu.add({
                                xtype: 'uni-view-search-field-date-range',
                                margin: '5px 0px 5px 5px'
                            });
                    }

                },
                click: function (menu) {
                    // setting of text to component button (+ * logic) and enable/disable 'clear all' button
                    var edited = false;
                    menu.items.each(function (item, index) {
                        if (item.xtype != 'menuseparator') {
                            if (item.xtype == 'uni-view-search-field-date-line')
                                if (item.down('datefield').getValue() != null
                                    || item.down('#hours').getValue() != 0
                                    || item.down('#minutes').getValue() != 0) {
                                    edited = true
                                }
                            if (item.xtype == 'uni-view-search-field-date-range') {
                                var moreLine = item.down('#more-value');
                                var smallerLine = item.down('#smaller-value');

                                if (moreLine.down('datefield').getValue() != null || moreLine.down('#hours').getValue() != 0
                                    || moreLine.down('#minutes').getValue() != 0 || smallerLine.down('datefield').getValue() != null
                                    || smallerLine.down('#hours').getValue() != 0 || smallerLine.down('#minutes').getValue() != 0) {
                                    edited = true;
                                }

                                // setting date constraints
                                var valueOfMore = moreLine.down('datefield').getValue();

                                if (valueOfMore != null) {
                                    smallerLine.down('datefield').setMinValue(valueOfMore)
                                }
                                var valueOfSmaller = smallerLine.down('datefield').getValue();

                                if (valueOfSmaller != null) {
                                    moreLine.down('datefield').setMaxValue(valueOfSmaller)
                                }
                            }
                        }
                    });

                    var mainButton = menu.up('uni-view-search-field-date-field');
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