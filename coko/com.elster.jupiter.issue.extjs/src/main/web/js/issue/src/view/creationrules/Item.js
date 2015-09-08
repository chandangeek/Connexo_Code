Ext.define('Isu.view.creationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.creationrules.ActionMenu',
        'Isu.privileges.Issue'
    ],
    alias: 'widget.issue-creation-rules-item',
    title: ' ',
    itemId: 'issue-creation-rules-item',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
            privileges: Isu.privileges.Issue.createRule,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'creation-rule-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                        name: 'template_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
                        name: 'issueType_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.issueReason', 'ISU', 'Issue reason'),
                        name: 'reason_name'
                    },

                    {
                        fieldLabel: Uni.I18n.translate('general.title.dueIn', 'ISU', 'Due in'),
                        name: 'dueIn',
                        renderer: function (value) {
                            var result = '';

                            if (value && value.number) {
                                switch (value.type) {
                                    case 'days':
                                        result =   Uni.I18n.translatePlural('general.timeUnit.days', value.number, 'ISU', '{0} days', '{0} day', '{0} days');
                                        break;
                                    case 'weeks':
                                        result =   Uni.I18n.translatePlural('general.timeUnit.weeks', value.number, 'ISU', '{0} weeks', '{0} week', '{0} weeks');
                                        break;
                                    case 'months':
                                        result =   Uni.I18n.translatePlural('general.timeUnit.months', value.number, 'ISU', '{0} months', '{0} month', '{0} months');
                                        break;
                                }
                            }

                            return result;
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.title.created', 'ISU', 'Created'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.lastModified', 'ISU', 'Last modified'),
                        name: 'modificationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ]
    }
});