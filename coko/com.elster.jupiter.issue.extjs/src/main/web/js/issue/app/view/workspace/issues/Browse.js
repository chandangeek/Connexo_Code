Ext.define('Isu.view.workspace.issues.Browse', {
    extend: 'Ext.container.Container',
    requires: [
        'Isu.view.workspace.issues.FilteringToolbar',
        'Isu.view.workspace.issues.GroupingToolbar',
        'Isu.view.workspace.issues.SortingToolbar',
        'Isu.view.workspace.issues.List',
        'Isu.view.workspace.issues.Item'
    ],
    alias: 'widget.issues-browse',
    cls: Uni.About.baseCssPrefix + 'content-padded',

    items: [
        {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Issues</h1>',
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'filtering-toolbar'
                },
                {
                    xtype: 'component',
                    html: '<hr/>',
                    margin: '10 0'
                },
                {
                    xtype: 'isu-grouping-toolbar'
                },
                {
                    xtype: 'component',
                    html: '<hr/>',
                    margin: '10 0'
                },
                {
                    xtype: 'sorting-toolbar',
                    margin: '0 0 20 0'
                },
                {
                    name: 'noIssues',
                    html: '<h3>No issue found</h3><p>No data collection issues have been created yet.</p>',
//                    html: '<h3>No issues found</h3><p>The filter is too narrow</p>',
                    bodyPadding: 10,
                    margin: '0 0 20 0',
                    border: false,
                    hidden: true
                },
                {
                    xtype: 'issues-list',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'issues-item'
                }
            ]
        }
    ]
});