Ext.define('Isu.view.workspace.issues.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step1',
    title: 'Select issues',
    border: false,
    requires: [
        'Isu.view.workspace.issues.List'
    ],

    items: [
        {
            name: 'step1-errors',
            layout: 'hbox',
            hidden: true,
            defaults: {
                xtype: 'container',
                cls: 'isu-error-panel'
            },
            items: [
                {html: '<b>There are errors on this page that require your attention.</b>'}
            ]
        },
        {xtype: 'issues-filter'},
        {xtype: 'issue-no-group'},
        {
            xtype: 'radiogroup',
            name: 'AllOrSelectedIssues',
            columns: 1,
            vertical: true,
            submitValue: false,
            defaults: {
                padding: '30 0'
            },
            items: [
                {
                    boxLabel: '<b>All issues</b><br/>' +
                        '<span style="color: grey;">Select all issues (related to filters and grouping on the issues screen)</span>',
                    name: 'issuesRange',
                    inputValue: 'ALL'
                },
                {
                    boxLabel: '<b>Selected issues</b><br/><span style="color: grey;">Select issues in table</span>',
                    name: 'issuesRange',
                    inputValue: 'SELECTED'
                }
            ]
        },
        {
            xtype: 'container',
            name: 'selected-issues-txt-holder',
            layout: 'hbox',
            padding: '0 0 10 0',
            items: [
                {
                    xtype: 'label',
                    name: 'issues-qty-txt',
                    width: 120
                },
                {
                    xtype: 'button',
                    name: 'uncheck-all-btn',
                    text: 'Uncheck all',
                    margin: '0 0 0 20'
                }
            ]
        },
        {
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
                        header: 'Title',
                        xtype: 'templatecolumn',
                        tpl: '{reason}<tpl if="device"> to {device.name} {device.serialNumber}</tpl>',
                        flex: 2
                    },
                    {
                        header: 'Due date',
                        dataIndex: 'dueDate',
                        xtype: 'datecolumn',
                        format: 'M d Y',
                        width: 140
                    },
                    {
                        header: 'Status',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="status">{status.name}</tpl>',
                        width: 100
                    },
                    {
                        header: 'Assignee',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type}"></span></tpl> {assignee.name}',
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