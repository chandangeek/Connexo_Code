Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    stores: [
        'ComServerInfos'
    ],
    views: [
        'Dsh.view.widget.CommunicationServers'
    ],
    refs: [
        {ref: 'communicationServers', selector: '#communication-servers'},
        {ref: 'communicationServersTooltip', selector: '#comservers-tooltip'}
    ],
    init: function () {
        this.control({
            '#communication-servers': {
                afterrender: this.onCommunicationServersAfterRender,
                itemmouseenter: this.onCommunicationServersItemMouseOver
            }
        });
        this.callParent(arguments);
    },
    showOverview: function () {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('communication-servers'));
    },
    onCommunicationServersAfterRender: function (view) {
        view.store.load();
        Ext.create('Ext.tip.ToolTip', {
            itemId: 'comservers-tooltip',
            target: view.el,
            delegate: view.itemSelector,
            trackMouse: true,
            showDelay: 50,
            hideDelay: 0
        });
    },
    onCommunicationServersItemMouseOver: function (view, record, item, index, e, eOpts) {
        this.getCommunicationServersTooltip().update(
            '<table>' +
                '<tr>' +
                    '<td style="text-align: right; padding-right: 10px; white-space: nowrap">Communication server</td>' +
                    '<td>' + record.get('comServerName') + '</td></tr>' +
                '<tr>' +
                    '<td style="text-align: right; padding-right: 10px; white-space: nowrap">Online/remote</td>' +
                    '<td>' + record.get('comServerType').charAt(0).toUpperCase() + record.get('comServerType').slice(1).toLocaleLowerCase() + '</td>' +
                '</tr>' +
                '<tr>' +
                    '<td style="text-align: right; padding-right: 10px; white-space: nowrap">Down since</td>' +
                    '<td>' + Ext.util.Format.date(new Date(), 'D M j, Y G:i') + '</td>' +
                '</tr>' +
            '</table>'
        );
    }
});