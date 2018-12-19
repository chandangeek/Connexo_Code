/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step2',
    title: Uni.I18n.translate('issue.selectAction','ISU','Select action'),

    requires: [
        'Ext.form.RadioGroup'
    ],

    initComponent: function () {
        var me = this,
            icon = '<span class="uni-icon-info-small" style="cursor: pointer;display: inline-block;width: 16px;height: 16px;margin-left: 5px;float: none;vertical-align: bottom;" data-qtip="' +
                Uni.I18n.translate('general.helpTooltip', 'ISU', 'Click for more information') + '"></span>';

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
                                boxLabel: 'Retry processes',//Uni.I18n.translate('issue.assignIssues','ISU','Assign issues'),
                                name: 'operation',
                                inputValue: 'retry',
                                checked: true,
                                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + "This option is available only if instances of the same process (name and version) are selected"/*Uni.I18n.translate('searchItems.bulk.removeScheduleMsg', 'MDC', 'Shared communication schedule will no longer be visible and used on the selected devices. A record is kept for tracking purposes. This action cannot be undone.') */+ '</span>',
                                privileges: Isu.privileges.Issue.assign
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
