/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycles-preview-form',
    isOverview: false,
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'cycle-name',
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                name: 'name'
            },
            {
                itemId: 'number-of-states',
                hidden: !me.isOverview,
                fieldLabel: Uni.I18n.translate('general.states', 'DLC', 'States'),
                name: 'statesCount'
            },
            {
                itemId: 'number-of-transitions',
                hidden: !me.isOverview,
                fieldLabel: Uni.I18n.translate('general.transitions', 'DLC', 'Transitions'),
                name: 'actionsCount'
            },
            {
                xtype: 'fieldcontainer',
                name: 'deviceTypes',
                itemId: 'used-by-field',
                fieldLabel: Uni.I18n.translate('general.usedBy', 'DLC', 'Used by'),
                items: [
                    {
                        xtype: 'container',
                        itemId: 'used-by',
                        items: [
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        if (me.isOverview && record.get('obsolete')) {
            me.insert(0, {
                xtype: 'container',
                layout: 'column',
                items: {
                    xtype: 'uni-form-empty-message',
                    itemId: 'empty-message',
                    text: Uni.I18n.translate('general.dlc.no.longer.be.used', 'DLC', 'The device life cycle was archived and can no longer be used')
                }
            });
        }

        me.callParent(arguments);
    }
});
