Ext.define('Dsh.view.CommunicationOverview', {
    extend: 'Ext.container.Container',
    alias: 'widget.communication-overview',
    itemId: 'communication-overview',
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
    initComponent: function () {
        this.callParent(arguments);
    }
});