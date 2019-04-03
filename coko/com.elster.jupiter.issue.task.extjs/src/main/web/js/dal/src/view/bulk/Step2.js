Ext.define('Itk.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-bulk-step2',
    title: Uni.I18n.translate('issue.selectAction','ITK','Select action'),

    requires: [
        'Ext.form.RadioGroup'
    ],

    initComponent: function () {
        var me = this,
            icon = '<span class="uni-icon-info-small" style="cursor: pointer;display: inline-block;width: 16px;height: 16px;margin-left: 5px;float: none;vertical-align: bottom;" data-qtip="' +
                Uni.I18n.translate('general.helpTooltip', 'ITK', 'Click for more information') + '"></span>';

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
                                itemId: 'Assign',
                                boxLabel: Uni.I18n.translate('issues.assignIssues','ITK','Assign issues'),
                                name: 'operation',
                                inputValue: 'assign',
                                checked: true,
                                privileges: Itk.privileges.Issue.assign
                            },
                            {
                                itemId: 'Close',
                                boxLabel: Uni.I18n.translate('issues.closeIssues', 'ITK', 'Close issues'),
                                name: 'operation',
                                inputValue: 'close',
                                privileges: Itk.privileges.Issue.close
                            },
                            {
                                itemId: 'PrioritySet',
                                boxLabel: Uni.I18n.translate('issues.PrioritySet', 'ITK', 'Set priority'),
                                name: 'operation',
                                inputValue: 'setpriority',
                                privileges: Itk.privileges.Issue.action
                            },
                            {
                                itemId: 'Snooze',
                                boxLabel: Uni.I18n.translate('issues.snooze', 'ITK', 'Snooze'),
                                name: 'operation',
                                inputValue: 'snooze',
                                privileges: Itk.privileges.Issue.action
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

});