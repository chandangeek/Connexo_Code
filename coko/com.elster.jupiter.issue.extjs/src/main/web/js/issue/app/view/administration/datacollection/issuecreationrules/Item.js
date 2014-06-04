Ext.define('Isu.view.administration.datacollection.issuecreationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.administration.datacollection.issuecreationrules.ActionMenu'
    ],
    alias: 'widget.issue-creation-rules-item',
    items: {
        xtype: 'panel',
        title: 'Details',
        frame: true,
        tools: [
            {
                xtype: 'item-action',
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
                        xtype: 'displayfield'
                    },
                    items: [
                        {
                            fieldLabel: 'Name',
                            name: 'name'
                        },
                        {
                            fieldLabel: 'Rule template',
                            name: 'template_name'
                        },
                        {
                            fieldLabel: 'Issue type',
                            name: 'issueType_name'
                        },
                        {
                            fieldLabel: 'Issue reason',
                            name: 'reason_name'
                        },

                        {
                            fieldLabel: 'Due in',
                            name: 'due_in'
                        }
                    ]
                },
                {
                    defaults: {
                        xtype: 'displayfield'
                    },
                    items: [
                        {
                            fieldLabel: 'Created',
                            name: 'creationDate',
                            renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                        },
                        {
                            fieldLabel: 'Last modified',
                            name: 'modificationDate',
                            renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                        }
                    ]
                }
            ]
        }
    }
});