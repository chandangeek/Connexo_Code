Ext.define('Mdc.view.setup.logbooktype.EmptyListMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.logbook-empty-list-message',
    hidden: true,
    items: [
        {
            itemId: 'noLogbookTypes',
            xtype: 'panel',
            html: "<h3>No logbook types found</h3><br>\
          There are no logbook types. This could be because:<br>\
          &nbsp;&nbsp; - No logbook types have been defined yet.<br>\
          &nbsp;&nbsp; - No logbook types comply to the filter.<br><br>\
          Possible steps:<br><br>"
        },
        {
            itemId: 'createlogbook',
            xtype: 'button',
            text: 'Add logbook type',
            action: 'createlogbookaction',
            hrefTarget: '',
            href: '#/administration/logbooktypes/create'
        }
    ]
});
