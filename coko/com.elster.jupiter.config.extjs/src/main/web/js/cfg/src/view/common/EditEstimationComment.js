/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.common.EditEstimationComment', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-edit-estimation-comment-window',
    title: Uni.I18n.translate('general.editEstimationComment', 'CFG', 'Edit estimation comment'),
    records: null,
    usagePoint: null,

    requires: [
        'Uni.view.readings.EstimationComment'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'reading-edit-comment-window-form',
            padding: 5,
            defaults: {
                width: 500,
                labelWidth: 170,
                margin: 20
            },
            items: [
                {
                    xtype: 'estimation-comment'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '10 10 0 0',
                    style: {
                        display: 'inline-block'
                    },
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'edit-comment-button',
                            readings: me.records,
                            margin: '0 0 0 40',
                            text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };
        if (!me.usagePoint) {
            me.items.items.unshift(
                {
                    xtype: 'radiogroup',
                    fieldLabel: Uni.I18n.translate('editComment.valueToEditComment', 'CFG', 'Value to edit comment'),
                    itemId: 'value-edit',
                    columns: 1,
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('editComment.value', 'CFG', 'Value (kWh)'),
                            inputValue: true,
                            name: 'valueEdit',
                            checked: true
                        },
                        {
                            boxLabel: Uni.I18n.translate('editComment.bulkValue', 'CFG', 'Bulk value (kWh)'),
                            inputValue: false,
                            name: 'valueEdit'
                        }
                    ]
                }
            );
        }

        me.callParent(arguments);
    }
});