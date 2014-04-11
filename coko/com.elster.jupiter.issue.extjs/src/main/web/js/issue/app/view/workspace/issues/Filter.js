Ext.define('Isu.view.workspace.issues.Filter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Ext.form.Label'
    ],
    alias: "widget.issues-filter",
    store: 'Isu.store.Issues',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        // Filter
        {
            xtype: 'panel',
            layout: 'hbox',
            title: 'Filters',
            name: 'filter',
            emptyText: 'None',
            items: [],
            dockedItems: {
                xtype: 'button',
                action: 'clearFilter',
                text: 'Clear all',
                disabled: true,
                dock: 'right'
            }
        },
        { xtype: 'menuseparator' },
        // Group
        {
            xtype: 'panel',
            layout: 'hbox',
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
            xtype: 'panel',
            layout: 'hbox',
            title: 'Sort',
            name: 'sortitemspanel',
            emptyText: 'None',
            items: [],
            dockedItems: [
                {
                    xtype: 'button',
                    action: 'addSort',
                    text: 'Add sort',
                    menu: {
                        name: 'addsortitemmenu'
                    },
                    dock: 'left'
                },
                {
                    xtype: 'button',
                    action: 'clearSort',
                    text: 'Clear all',
                    disabled: true,
                    dock: 'right'
                }
            ]
        }
    ]
});