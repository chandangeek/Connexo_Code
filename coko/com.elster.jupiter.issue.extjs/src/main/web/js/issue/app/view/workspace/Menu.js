Ext.define('Isu.view.workspace.Menu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.workspace-menu',

    defaults: {
        hrefTarget: '_self'
    },
    floating: false,
    items: [
        {
            text: 'Data collection',
            href: '#/workspace/datacollection'
        },
        {
            text: 'Issues',
            href: '#/workspace/datacollection/issues'
        }
    ]
});