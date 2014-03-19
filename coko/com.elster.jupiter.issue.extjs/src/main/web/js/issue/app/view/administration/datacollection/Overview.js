Ext.define('Isu.view.administration.datacollection.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.administration-datacollection-overview',

    content: [
        {
            xtype: 'panel',
            html: '<h1>Data collection</h1>'+
                '<h3>Navigation:</h3>'+
                '<ul>'+
                '<li>'+
                '<a href="#/administration/datacollection/issueassignmentrules">Issue assignment rules</a>'+
                '</li>'+
                '</ul>',
            flex: 1
        }
    ]
});