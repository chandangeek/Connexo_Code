Ext.define('Mdc.view.setup.comserver.ComServers', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupComServers',

    requires: [
        'Mdc.store.ComServers'
    ],
    overflowY: 'auto',
    layout: 'fit',
    itemId: 'comservergrid',

    store: Ext.create('Mdc.store.ComServers'),
    requires: ['Ext.ux.PreviewPlugin'],
    initComponent: function () {
        this.columns = [
            {
                text: 'ComServers',
                xtype: 'templatecolumn',
                tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                    '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                        '{name} - {comServerDescriptor} - <tpl if="active==true"{active}><span style="color:lightgreen">active</span><tpl else><span style="color:#ff0000">not active</span></tpl>' +
                    '</caption>' +
                    '<tr>' +
                        '<td>serverLogLevel: </td>' +
                        '<td>{serverLogLevel}</td>' +
                        '<td>changesInterPollDelay: </td>' +
                        '<td>{changesInterPollDelay.count} {changesInterPollDelay.timeUnit}</td>' +
                    '</tr>'+
                    '<tr>' +
                        '<td>communicationLogLevel: </td>' +
                        '<td>{communicationLogLevel}</td>' +
                        '<td>schedulingInterPollDelay: </td>' +
                        '<td>{schedulingInterPollDelay.count} {schedulingInterPollDelay.timeUnit}</td>' +
                    '</tr>'+
                    '<tr>' +
                    '<td>storeTaskQueueSize: </td>' +
                    '<td>{storeTaskQueueSize}</td>' +
                    '</tr>'+
                    '<tr>' +
                    '<td>numberOfStoreTaskThreads: </td>' +
                    '<td>{numberOfStoreTaskThreads}</td>' +
                    '</tr>'+
                    '<tr>' +
                    '<td>storeTaskThreadPriority: </td>' +
                    '<td>{storeTaskThreadPriority}</td>' +
                    '</tr>'+
                    '</table>',
                flex:1
            }
        ];

        this.buttons = [
            {
                text: 'Add',
                action: 'add'
            },
            {
                text: 'Delete',
                action: 'delete'
            }
        ];

        this.callParent();
    }
});