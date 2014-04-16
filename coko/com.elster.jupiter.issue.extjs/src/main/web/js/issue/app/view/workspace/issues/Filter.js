Ext.define('Isu.view.workspace.issues.Filter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Ext.form.Label',
        'Skyline.panel.FilterToolbar'
    ],
    alias: "widget.issues-filter",
    store: 'Isu.store.Issues',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            title: 'Filters',
            xtype: 'filter-toolbar',
            name: 'filter',
            emptyText: 'None'
        },
        // Filter
        { xtype: 'menuseparator' },
        // Group
        {
            xtype: 'filter-toolbar',
            title: 'Group',
            name: 'group',
            items: {
                xtype: 'combobox',
                name: 'groupnames',
                editable: false,
                emptyText: 'None',
                queryMode: 'local',
                displayField: 'display',
                valueField: 'value',
                labelAlign: 'left'
            }
        },
        {
            xtype: 'gridpanel',
            name: 'groupgrid',
            hidden: true,
            store: 'Isu.store.IssuesGroups',
            border: true,
            columns: [
                {
                    text: 'Reason',
                    dataIndex: 'reason',
                    flex: 5
                },
                {
                    text: 'Issues',
                    dataIndex: 'number',
                    flex: 1
                }
            ],
            tbar: {
                xtype: 'panel',
                name: 'groupitemsshown',
                hidden: true,
                border: false
            },
            bbar: {
                xtype: 'pagingtoolbarbottom',
                store: 'Isu.store.IssuesGroups',
                dock: 'bottom'
            }
        },
        { xtype: 'menuseparator' },
        // Sort
        {
            xtype: 'filter-toolbar',
            title: 'Sort',
            name: 'sortitemspanel',
            emptyText: 'None',
            tools: [
                {
                    xtype: 'button',
                    action: 'addSort',
                    text: 'Add sort',
                    menu: {
                        name: 'addsortitemmenu'
                    },
                    dock: 'left'
                }
            ]
        }
    ]
});