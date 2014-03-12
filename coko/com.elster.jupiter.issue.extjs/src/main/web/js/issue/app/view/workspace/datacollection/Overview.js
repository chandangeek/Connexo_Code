Ext.define('Isu.view.workspace.datacollection.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.datacollection-overview',

    content: [
        {
            xtype: 'panel',
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