/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step2',
    title: Uni.I18n.translate('mdc.processstep2.bulk.selectAction','MDC','Select action'),

    requires: [
        'Ext.form.RadioGroup'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                border: false,
                items: [
                    {
                        itemId: 'radiogroupStep2',
                        xtype: 'radiogroup',
                        columns: 1,
                        vertical: true,
                        defaults: {
                            name: 'operation',
                            submitValue: false
                        },
                        items: [
                            {
                                itemId: 'Restart',
                                boxLabel: Uni.I18n.translate('mdc.processstep2.bulk.retryProcess','MDC','Retry processes'),
                                name: 'operation',
                                inputValue: 'retry',
                                checked: true,
                                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('mdc.processstep2.bulk.retryProcessMsg','MDC','This option is available only if instances of the same process (name and version) are selected') + '</span>'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});