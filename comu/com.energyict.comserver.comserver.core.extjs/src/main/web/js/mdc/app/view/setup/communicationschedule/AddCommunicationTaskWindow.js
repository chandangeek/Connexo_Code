Ext.define('Mdc.view.setup.communicationschedule.AddCommunicationTaskWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.addCommunicationTaskWindow',
    itemId: 'addCommunicationTaskWindow',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    closable: true,
    width: 700,
    height: 500,
    constrain: true,
    autoShow: true,
    modal:true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    closeAction : 'destroy',
    floating:true,
    cls: 'content-wrapper',
    //border: 0,
//    region: 'center',
    title: Uni.I18n.translate('communicationschedule.addCommunicationTasks', 'MDC', 'Add communication tasks'),
    items: [
        {
            xtype: 'gridpanel',
            columns: [
                {
                    header: Uni.I18n.translate('communicationtask.name', 'MDC', 'Name'),
                    dataIndex: 'schedulingStatus',
                    sortable: false,
                    hideable: false,
                    fixed: true,
                    flex: 0.1
                }
            ]
        }
    ],


    initComponent: function () {
        var me = this;
        me.callParent(arguments);
//        me.mon(Ext.getBody(), 'click', function(el, e){
//            me.close(me.closeAction);
//        }, me, { delegate: '.x-mask' });
    }

});

