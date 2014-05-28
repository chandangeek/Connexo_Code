Ext.define('Isu.view.workspace.issues.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step1',
    title: 'Select issues',
    border: false,
    requires: [
        'Isu.view.workspace.issues.List',
        'Isu.util.FormErrorMessage'
    ],

    items: [
        {
            name: 'step1-errors',
            layout: 'hbox',
            hidden: true,
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message'
                }
            ]
        },
        {
            itemId: 'radiogroupStep1',
            xtype: 'radiogroup',
            name: 'AllOrSelectedIssues',
            columns: 1,
            vertical: true,
            submitValue: false,
            defaults: {
                padding: '0 0 30 0'
            },
            items: [
                {   itemId: 'issueRange1',
                    boxLabel: '<b>All issues</b><br/>' +
                        '<span style="color: grey;">Select all issues (related to filters and grouping on the issues screen)</span>',
                    name: 'issuesRange',
                    inputValue: 'ALL'
                },
                {   itemId: 'issueRange2',
                    boxLabel: '<b>Selected issues</b><br/><span style="color: grey;">Select issues in table</span>',
                    name: 'issuesRange',
                    inputValue: 'SELECTED'
                }
            ]
        },
        {
            itemId: 'selected-issues',
            xtype: 'container',
            name: 'selected-issues-txt-holder',
            layout: 'hbox',
            padding: '0 0 10 0',
            items: [
                {
                    itemId: 'issues-qty-txt',
                    xtype: 'label',
                    name: 'issues-qty-txt',
                    width: 120
                },
                {
                    itemId: 'uncheck-all',
                    xtype: 'button',
                    name: 'uncheck-all-btn',
                    text: 'Uncheck all',
                    margin: '0 0 0 20'
                }
            ]
        },
        {
            itemId: 'issues-list',
            xtype: 'issues-list',
            height: 285,
            selType: 'checkboxmodel',
            selModel: {
                checkOnly: true,
                enableKeyNav: false,
                showHeaderCheckbox: false
            },
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        itemId: 'Title',
                        header: 'Title',
                        xtype: 'templatecolumn',
                        tpl: '{reason.name}<tpl if="device"> to {device.serialNumber}</tpl>',
                        flex: 2
                    },
                    {
                        itemId: 'dueDate',
                        header: 'Due date',
                        dataIndex: 'dueDate',
                        xtype: 'datecolumn',
                        format: 'M d Y',
                        width: 140
                    },
                    {
                        itemId: 'status',
                        header: 'Status',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="status">{status.name}</tpl>',
                        width: 100
                    },
                    {
                        itemId: 'assignee',
                        header: 'Assignee',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
                        flex: 1
                    }
                ]
            },
            dockedItems: []
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});