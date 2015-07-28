Ext.define('Uni.view.search.field.NumberRange', {
    extend: 'Ext.button.Button',
    alias: 'widget.uni-view-search-field-number-range',
    xtype: 'uni-view-search-field-number-range',
    text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    defaultText: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
    arrowAlign: 'right',
    menuAlign: 'tr-br',
    requires: [
        'Uni.view.search.field.NumberLine'
    ],
    layout: 'hbox',
    style: {
        'background-color': '#71adc7'
    },
    mixins: [
        'Ext.util.Bindable'
    ],

    clearAllHandler: function () {
        var me = this;
        var items = me.down('menu').items.items;
        Ext.each(items, function (item) {
            if (item.down('numberfield')) item.down('numberfield').reset();
        });
        me.setText( me.defaultText)
    },
    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'container',
            margin: '5px 0px 0px 0px',
            items: [
                {
                    xtype: 'uni-view-search-field-number-line',
                    operator: '>'
                }
            ]
        });
        me.down('menu').add({
            xtype: 'container',
            margin: '0px 0px 0px 0px',
            items: [
                {
                    xtype: 'uni-view-search-field-number-line',
                    operator: '<'
                }
            ]
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
            minWidth: 273,
            items: [
                {
                    xtype: 'uni-view-search-field-number-line',
                    default: true,
                    operator: '='
                },
                {
                    xtype: 'menuseparator',
                    default: true
                },
                {
                    xtype: 'uni-view-search-field-number-line',
                    default: true,
                    operator: '>'
                },
                {
                    xtype: 'uni-view-search-field-number-line',
                    default: true,
                    operator: '<'
                }
            ],
            listeners: {
                hide: function (menu) {
                    Ext.each(menu.items.items, function (item) {
                        if (item && !item.default && item.down('numberfield').getValue() == 0) {
                            menu.remove(item);
                        }
                    });
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
                            flex: 1.35,
                            cls: 'x-spacers'
                        }
                    ]
                }
            ]
        };


        this.callParent(arguments);
        Ext.suspendLayouts();
        this.menu.items.items[0].down('combo').setValue(this.menu.items.items[0].operator);
        this.menu.items.items[2].down('combo').setValue(this.menu.items.items[2].operator);
        this.menu.items.items[3].down('combo').setValue(this.menu.items.items[3].operator);
        Ext.resumeLayouts(true);
    }
});