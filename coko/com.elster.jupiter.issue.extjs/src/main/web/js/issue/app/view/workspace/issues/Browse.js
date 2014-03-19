Ext.define('Isu.view.workspace.issues.Browse', {
    extend: 'Ext.container.Container',
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
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    items: [
                        {
                            xtype: 'component',
                            html: '<h1>Issues</h1>',
                            flex: 1
                        },
//                        {
//                            xtype: 'component',
//                            html: '<a href="#/workspace/datacollection/assignmentrules">View assignment rules</a>',
//                            width: 150
//                        }
                    ]
                },
                {
                    xtype: 'issues-filter'
                },
                {
                    xtype: 'issue-no-group'
                },
                {
                    xtype: 'issues-list'
                },
                {
                    xtype: 'issues-item'
                }
            ]
        }
    ]
});