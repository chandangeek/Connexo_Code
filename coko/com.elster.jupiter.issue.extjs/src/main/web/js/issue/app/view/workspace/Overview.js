Ext.define('Isu.view.workspace.Overview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.workspace-overview',
    overflowY: 'auto',
    cls: 'content-wrapper',
    items: [
        {
            html: '<h1>Workspace</h1>'+
                '<h3>Navigation:</h3>'+
                '<ul>'+
                '<li>'+
                '<a href="#/workspace/datacollection">Data collection</a>'+
                '<ul>'+
                '<li>'+
                '<a href="#/workspace/datacollection/issues">Issues</a>'+
                '</li>'+
                '</ul>'+
                '</li>' +
                '</ul>',
            flex: 1
        }
    ]
});