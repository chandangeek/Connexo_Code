Ext.define('Uni.view.search.field.DateRangeField', {
    extend: 'Ext.button.Button',
    alias: 'widget.uni-view-search-field-date-range',
    xtype: 'uni-view-search-field-date-range',
    requires: [
        'Uni.view.search.field.RangeLine'
    ],
    itemId: 'date',
    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Last reading date'),
    arrowAlign: 'right',
    menuAlign: 'tr-br',
    style: {
        'background-color': '#71adc7'
    },
    layout: 'hbox',
    clearAllHandler: function () {
        var me = this;
        var items = me.down('menu').items.items;
        Ext.each(items, function (item) {
            if (item.down('datefield')) item.down('datefield').reset();
            if (item.down('#hours')) item.down('#hours').reset();
            if (item.down('#minutes')) item.down('#minutes').reset();
        });
    },
    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'container',
            margin: '5px 0px 0px 0px',
            items: [
                {
                    xtype: 'uni-view-search-field-range-line'
                }
            ]
        });
        me.down('menu').add({
            xtype: 'container',
            margin: '0px 0px 0px 0px',
            items: [
                {
                    xtype: 'uni-view-search-field-range-line'
                }
            ]
        });
    },

    initComponent: function () {
        var me = this;
        me.menu = {
            cls: 'x-menu-body-custom',
            plain: true,
            style: {
                overflow: 'visible',
                arrowAlign: 'left'
            },
            minWidth: 440,
            arrowAlign: 'left',
            items: [
                {
                    xtype: 'uni-view-search-field-range-line'
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'uni-view-search-field-range-line'
                },
                {
                    xtype: 'uni-view-search-field-range-line'
                }
            ],
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
                            text: Uni.I18n.translate('general.clearAll', 'UNI', 'Clear all'),
                            align: 'right',
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
                            flex: 0.9,
                            cls: 'x-spacers'
                        }
                    ]
                }
            ]
        };

        this.callParent(arguments);
        Ext.suspendLayouts();
        var firstItem = this.menu.items.items[0];
        firstItem.down('combo').setValue('=');
        firstItem.down('label').hidden = true;
        firstItem.down('#hours').hidden = true;
        firstItem.down('#minutes').hidden = true;
        firstItem.down('#flex').hidden = false;
        this.menu.items.items[3].down('combo').setValue('<');
        Ext.resumeLayouts(true);
    }
});