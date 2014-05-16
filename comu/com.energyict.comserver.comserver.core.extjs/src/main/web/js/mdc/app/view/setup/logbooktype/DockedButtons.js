Ext.define('Mdc.view.setup.logbooktype.DockedButtons', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.logbook-docked-buttons',
    aling: 'left',

    items: [
        {
            xtype: 'container',
            name: 'LogBookCount',
            flex: 1
        },
        {
            xtype: 'button',
            text: 'Create logbook type',
            action: 'createlogbookaction',
            hrefTarget: '',
            href: '#/administration/logbooktypes/create'
        }
    ]

});
