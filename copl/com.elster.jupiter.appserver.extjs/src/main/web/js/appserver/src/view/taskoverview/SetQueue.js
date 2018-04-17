/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.SetQueue', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.set-queue',
    columns: 1,
    defaults: {
        name: 'setQueue'
    },
    requires: [
        'Apr.store.QueuesType',
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                width: 500,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'queue-type',
                        name: 'queueType',
                        fieldLabel: Uni.I18n.translate('general.selectqueue', 'APR', 'Select queue'),
                        labelWidth: 150,
                        store: 'Apr.store.QueuesType',
                        emptyText: Uni.I18n.translate('general.selectqueue', 'APR', 'Select queue ...'),
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'value',
                        width: 400
                    }

                ],
                action: 'applyAction'
            }
        ];

        me.callParent(arguments);
    }
});