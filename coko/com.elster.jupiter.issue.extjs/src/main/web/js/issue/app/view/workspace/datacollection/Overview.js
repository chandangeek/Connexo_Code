Ext.define('Isu.view.workspace.datacollection.Overview', {
    extend: 'Ext.container.Container',
    requires: [
        'Uni.view.breadcrumb.Trail'
    ],
    alias: 'widget.datacollection-overview',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    overflowY: 'auto',
    cls: 'content-wrapper',
    items: [
        {
            xtype: 'breadcrumbTrail',
            padding: 6
        },
        {
            html: '<h1>Data collection</h1>'+
                '<h3>Navigation:</h3>'+
                '<ul>'+
                '<li>'+
                '<a href="#/workspace/datacollection/issues">Issues</a>'+
                '</li>'+
                '</ul>',
            flex: 1
        }
    ]
});