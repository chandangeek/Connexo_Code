Ext.define('Itk.view.creationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Itk.view.creationrules.ActionMenu',
    ],
    alias: 'widget.issue-creation-rules-item',
    title: ' ',
    itemId: 'issue-creation-rules-item',
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Itk.privileges.Issue.createIssueRule,
            menu: {
                xtype: 'issue-creation-rule-action-menu'
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
                        fieldLabel: Uni.I18n.translate('general.title.name', 'ITK', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.Template', 'ITK', 'Template'),
                        name: 'template_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.issueReason', 'ITK', 'Issue reason'),
                        name: 'reason_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ITK', 'Status'),
                        name: 'active',
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('administration.issueCreationRules.active', 'ITK', 'Active')
                                : Uni.I18n.translate('administration.issueCreationRules.inactive', 'ITK', 'Inactive');
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
                        fieldLabel: Uni.I18n.translate('general.title.created', 'ITK', 'Created'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.lastModified', 'ITK', 'Last modified'),
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