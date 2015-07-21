Ext.define('Uni.view.search.field.NumberRange', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-number-range',
    xtype: 'uni-view-search-field-number-range',
    requires: [
        'Uni.view.search.field.NumberLine'
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
                minWidth: 293,
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
                                xtype: 'numberfield',
                                margin: '3px 0px 0px 0px',
                                value: 0,
                                hideTrigger: true
                                //width: 180
                            }/*,
                             {
                             xtype: 'button',
                             iconCls: ' icon-close2',
                             action: 'remove',
                             margin: '3px 0px 0px 10px',
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
                                align: 'right'
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
        Ext.resumeLayouts(true);
    }
});