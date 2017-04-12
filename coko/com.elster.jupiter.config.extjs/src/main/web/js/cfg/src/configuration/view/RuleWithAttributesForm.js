/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.configuration.view.RuleWithAttributesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.rule-with-attributes-form',
    requires: [
        'Cfg.configuration.view.RuleWithAttributesField',
        'Uni.button.Action',
        'Cfg.configuration.view.RuleWithAttributesActionsMenu'
    ],
    router: null,
    application: null,
    padding: '10 0 0 0',
    layout: 'column',
    type: null,
    kindOfReadingType: '',
    items: [
        {
            xtype: 'container',
            itemId: 'left-container',
            columnWidth: 0.5,
            items: []
        },
        {
            xtype: 'container',
            itemId: 'right-container',
            columnWidth: 0.5,
            items: []
        }
    ],
    records: null,

    initComponent: function () {
        var me = this,
            records = me.records,
            itemsPerContainer = Math.ceil(records.length / 2),
            showActionsMenu;

        me.items[0].items = [];
        records.slice(0, itemsPerContainer).forEach(function (record) {
            me.items[0].items.push({
                xtype: 'rule-with-attributes-field',
                type: me.type,
                kindOfReadingType: me.kindOfReadingType,
                itemId: 'rule-with-attributes-field' + record.getId() + me.kindOfReadingType,
                record: record
            });
        });

        me.items[1].items = [];
        records.slice(itemsPerContainer).forEach(function (record) {
            me.items[1].items.push({
                xtype: 'rule-with-attributes-field',
                type: me.type,
                kindOfReadingType: me.kindOfReadingType,
                itemId: 'rule-with-attributes-field' + record.getId() + me.kindOfReadingType,
                record: record
            });
        });

        records.find(function(record) {
            var properties = record.properties().getRange();
            if (properties.length) {
                showActionsMenu = properties.find(function(property) {
                    return property.get('canBeOverridden');
                });
                return showActionsMenu;
            }
        });

        if (showActionsMenu) {
            me.tools = [
                {
                    xtype: 'uni-button-action',
                    itemId: 'rule-with-attributes-btn-' + me.type + me.kindOfReadingType,
                    menu: {
                        xtype: 'rule-with-attributes-actions-menu',
                        kindOfReadingType: me.kindOfReadingType,
                        type: me.type,
                        router: me.router,
                        records: records,
                        application: me.application,
                        itemId: 'rule-with-attributes-actions-menu-' + me.type + me.kindOfReadingType
                    }
                }
            ];
        }

        me.callParent(arguments);
    }
});
