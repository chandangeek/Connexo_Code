Ext.define('Uni.view.search.field.DateRangeField', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-date-range',
    xtype: 'uni-view-search-field-date-range',
    requires: [
        'Uni.view.search.field.RangeLine'
    ],
    layout: 'hbox',
    clearAllHandler: function () {
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
        var me = this,
            menu = Ext.create('Ext.menu.Menu', {
                id: 'mainMenu',
                plain: true,
                style: {
                    overflow: 'visible',
                    arrowAlign: 'left'
                },
                minWidth: 477,
                arrowAlign: 'left',
                items: [
                    {
                        xtype: 'container',
                        margin: 5,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combo',
                                value: '=',
                                disabled: true,
                                width: 55,
                                margin: '3px 40px 0px 0px'
                            },
                            {
                                xtype: 'datefield',
                                margin: '3px 0px 0px 0px',
                                value: new Date()
                            }
                            /*{
                             flex: 1
                             },
                             {
                             xtype: 'button',
                             iconCls: ' icon-close2',
                             action: 'remove',
                             margin: '3px 0px 0px 80px',
                             handler: function () {
                             me.removeHandler()
                             }
                             }*/
                        ]
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
                        background: '#71adc7',
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
                                align: 'right'
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
                text: Uni.I18n.translate('search.overview.lastReadingDate.emptyText', 'UNI', 'Last reading date'),
                arrowAlign: 'right',
                menuAlign: 'tr-br',
                menu: menu
            }
        ];

        this.callParent(arguments);
        Ext.suspendLayouts();
        Ext.resumeLayouts(true);
    }
});