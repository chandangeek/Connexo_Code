Ext.define('Isu.view.administration.Menu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.workspace-menu',

    defaults: {
        hrefTarget: '_self'
    },
    floating: false,
    items: [
        {
            text: 'Data collection',
            href: '#/issue-administration/datacollection'
        },
        {
            text: 'Issue assignment rules',
            href: '#/issue-administration/datacollection/issueassignmentrules'
        },
        {
            text: 'Issue creation rules',
            href: '#/issue-administration/issuecreationrules'
        }
    ]
});