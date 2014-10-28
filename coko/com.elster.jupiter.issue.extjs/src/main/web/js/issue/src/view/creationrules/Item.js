Ext.define('Isu.view.creationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.creationrules.ActionMenu'
    ],
    alias: 'widget.issue-creation-rules-item',
    title: 'Details',
    itemId: 'issue-creation-rules-item',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
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
                        name: 'due_in'
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
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.lastModified', 'ISU', 'Last modified'),
                        name: 'modificationDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    }
                ]
            }
        ]
    }
});