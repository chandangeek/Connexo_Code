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
    store: undefined,

    initComponent: function () {
        var me = this,
            store = Ext.getStore(me.store),
            itemsPerContainer = Math.ceil(store.getCount() / 2),
            records = store.getRange(),
            showActionsMenu;

        me.items[0].items = [];
        store.getRange(0, itemsPerContainer - 1).forEach(function (record) {
            me.items[0].items.push({
                xtype: 'rule-with-attributes-field',
                type: me.type,
                itemId: 'rule-with-attributes-field' + record.getId(),
                record: record
            });
        });

        me.items[1].items = [];
        store.getRange(itemsPerContainer).forEach(function (record) {
            me.items[1].items.push({
                xtype: 'rule-with-attributes-field',
                type: me.type,
                itemId: 'rule-with-attributes-field' + record.getId(),
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
                    itemId: 'rule-with-attributes-btn-' + me.type,
                    menu: {
                        xtype: 'rule-with-attributes-actions-menu',
                        type: me.type,
                        router: me.router,
                        store: store,
                        application: me.application,
                        itemId: 'rule-with-attributes-actions-menu-' + me.type
                    }
                }
            ];
        }

        me.callParent(arguments);
    }
});
