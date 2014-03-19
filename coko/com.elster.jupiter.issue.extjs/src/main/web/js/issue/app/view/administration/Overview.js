Ext.define('Isu.view.administration.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.administration-overview',

    content: [
        {
            xtype: 'panel',
            html: '<h1>Administration</h1>'+
                '<h3>Navigation:</h3>'+
                '<ul>'+
                '<li>'+
                '<a href="#/administration/datacollection">Data collection</a>'+
                '<ul>'+
                '<li>'+
                '<a href="#/administration/datacollection/issueassignmentrules">Issue assignment rules</a>'+
                '</li>'+
                '</ul>'+
                '</li>' +
                '</ul>',
            flex: 1
        }
    ]
});