Ext.define('Isu.view.administration.datacollection.issueassignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.datacollection.issueassignmentrules.List'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    side: [

    ],

    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    html: '<h1>Issue assignment rules</h1>',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'issues-assignment-rules-list',
                    margin: '0 0 20 0'
                }
            ]
        }
    ]
});