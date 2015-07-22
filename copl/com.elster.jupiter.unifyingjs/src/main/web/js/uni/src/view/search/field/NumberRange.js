Ext.define('Uni.view.search.field.NumberRange', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-number-range',
    xtype: 'uni-view-search-field-number-range',
    requires: [
        'Uni.view.search.field.NumberLine'
    ],
    layout: 'hbox',
        clearAllHandler: function () {
            var me = this;
            var items = me.down('menu').items.items;
            Ext.each(items, function( item ) {
                if (item.down('numberfield')) item.down('numberfield').reset();
            });
        },
    addRangeHandler: function () {
        var me = this;
        me.down('menu').add({
            xtype: 'container',
            margin: '5px 0px 0px 0px',
            items: [
                {
                    xtype: 'uni-view-search-field-number-line'
                }
            ]
        });
        me.down('menu').add({
            xtype: 'container',
            margin: '0px 0px 0px 0px',
            items: [
                {
                    xtype: 'uni-view-search-field-number-line'
                }
            ]
        });
    },

    initComponent: function () {
        var me = this,
            menu = Ext.create('Ext.menu.Menu', {
                plain: true,
                style: {
                    overflow: 'visible'
                },
                cls: 'x-menu-body-custom',
                minWidth: 293,
                items: [
                    {
                        xtype: 'uni-view-search-field-number-line'
                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        xtype: 'uni-view-search-field-number-line'
                    },
                    {
                        xtype: 'uni-view-search-field-number-line'
                    }
                ],
                dockedItems: [
                    {
                        xtype: 'toolbar',
                        background: '#71adc7',
                        dock: 'bottom',
                        items: [
                            {
                                flex: 1,
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
                                flex: 1,
                                cls: 'x-spacers'
                            }
                        ]
                    }
                ]
            });
        this.items = [
            {
                xtype: 'button',
                itemId: 'date',
                text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Interval'),
                arrowAlign: 'right',
                menuAlign: 'tr-br',
                menu: menu
            }
        ];

        this.callParent(arguments);
        Ext.suspendLayouts();
        this.down('menu').items.items[0].down('combo').setValue('=');
        this.down('menu').items.items[3].down('combo').setValue('<');
        Ext.resumeLayouts(true);
    }
});