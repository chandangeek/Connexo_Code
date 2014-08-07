Ext.define('Dsh.view.ConnectionOverview', {
    extend: 'Ext.container.Container',
    alias: 'widget.connection-overview',
    itemId: 'connection-overview',
    layout: {
        type: 'table',
        columns: 4,
        tableAttrs: {
            style: {
                width: '100%',
                padding: '0 30px'
            }
        },
        tdAttrs: {
            style: {
                verticalAlign: 'top',
                paddingBottom: '30px'
            }
        }
    },

});