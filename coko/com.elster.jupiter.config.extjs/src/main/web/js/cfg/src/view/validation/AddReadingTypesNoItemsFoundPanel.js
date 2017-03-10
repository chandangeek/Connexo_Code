/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.AddReadingTypesNoItemsFoundPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.addReadingTypesNoItemsFoundPanel',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.on({
            show: me.setCountOfSelectedReadingTypes,
            scope: me
        });

        me.items = [
            {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'component',
                        itemId: 'selectionCounter',
                        html: Uni.I18n.translate('readingType.noReadingType', 'CFG', 'No reading types selected'),
                        margin: '0 8 0 0',
                        setText: function (text) {
                            this.update(text);
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'list-of-reading-types-info-btn',
                        tooltip: Uni.I18n.translate('readingType.tooltip', 'CFG', 'Click for more information'),
                        iconCls: 'uni-icon-info-small',
                        cls: 'uni-btn-transparent',
                        width: 15,
                        style: {
                            display: 'inline-block',
                            textDecoration: 'none !important',
                            position: 'absolute',
                            top: '5px'
                        },
                        listeners: {
                            click: me.openInfoWindow,
                            scope: me
                        }
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'uncheckAllButton',
                                text: Uni.I18n.translate('readingType.uncheckAll', 'CFG', 'Uncheck all'),
                                action: 'checkAll',
                                margin: '0 0 0 8',
                                listeners: {
                                    click: me.uncheckAll,
                                    scope: me
                                }
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'no-items-found-panel',
                margin: '0 0 20 0',
                title: Uni.I18n.translate('validation.readingType.empty.title', 'CFG', 'No reading types found.'),
                reasons: [
                    Uni.I18n.translate('validation.readingType.empty.list.item1', 'CFG', 'No reading types have been added yet.'),
                    Uni.I18n.translate('validation.readingType.empty.list.item2', 'CFG', 'No reading types comply with the filter.'),
                    Uni.I18n.translate('validation.readingType.empty.list.item3', 'CFG', 'All reading types have been already added to rule.')
                ]
            }
        ];
        me.callParent(arguments);
    },

    getSelectionCounter: function () {
        return this.down('#selectionCounter');
    },

    getInfoBtn: function () {
        return this.down('#list-of-reading-types-info-btn');
    },

    getuncheckAllBtn: function () {
        return this.down('#uncheckAllButton');
    },

    openInfoWindow: function () {
        var me = this;
        me.fireEvent('openInfoWindow');
    },

    setCountOfSelectedReadingTypes: function(){
        var me = this;
        me.fireEvent('showNoFoundPanel', me);
    },

    uncheckAll: function (btn) {
        var me = this;
        me.fireEvent('uncheckAll', me);
        me.fireEvent('showNoFoundPanel', me);
    }
});