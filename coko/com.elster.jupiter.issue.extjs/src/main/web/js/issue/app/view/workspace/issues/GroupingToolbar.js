Ext.define('Isu.view.workspace.issues.GroupingToolbar', {
    extend: 'Ext.container.Container',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.isu-grouping-toolbar',
    items: [
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'component',
                    html: 'Group',
                    cls: 'isu-toolbar-label',
                    width: 55
                },
                {
                    xtype: 'combobox',
                    name: 'groupingcombo',
                    store: 'Isu.store.IssueGrouping',
                    editable: false,
                    emptyText: 'None',
                    queryMode: 'local',
                    displayField: 'display',
                    valueField: 'value'
                }
            ]
        },
        {
            xtype: 'gridpanel',
            name: 'groupinggrid',
            store: 'Isu.store.IssuesGroups',
            enableColumnHide: false,
            minHeight: 125,
            margin: '10 0 0 0',
            hidden: true,
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
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
                ]
            },
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'pagingtoolbartop',
                    store: 'Isu.store.IssuesGroups',
                    displayMsg: '{0} - {1} of {2} reasons',
                    displayMoreMsg: '{0} - {1} of more than {2} reasons',
                    emptyMsg: '0 reasons'
                },
                {
                    dock: 'bottom',
                    xtype: 'pagingtoolbarbottom',
                    store: 'Isu.store.IssuesGroups'
                }
            ]
        },
        {
            xtype: 'container',
            name: 'groupinginformation',
            margin: '20 0 0',
            hidden: true,
            items: [
                {
                    xtype: 'component',
                    html: '<hr/>'
                },
                {
                    xtype: 'component',
                    name: 'informationtext',
                    margin: '20 0 0'
                }
            ]
        }
    ]
});