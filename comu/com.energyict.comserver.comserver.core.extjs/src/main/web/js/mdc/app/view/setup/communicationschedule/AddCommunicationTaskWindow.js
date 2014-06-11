Ext.define('Mdc.view.setup.communicationschedule.AddCommunicationTaskWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.addCommunicationTaskWindow',
    itemId: 'addCommunicationTaskWindow',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.CommunicationTasks'
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
            xtype: 'grid',
            itemId: 'communicationTaskGridFromSchedule',
            store: 'CommunicationTasks',
            selModel: {
                mode: 'MULTI'
            },
            selType: 'checkboxmodel',
            columns: [
                {
                    header: Uni.I18n.translate('communicationtask.name', 'MDC', 'Name'),
                    dataIndex: 'name',
                    sortable: false,
                    hideable: false,
                    fixed: true,
                    flex: 0.9
                }
            ]
        },
        {
            xtype: 'form',
            border: false,
            itemId: 'addCommunicationTaskButtonForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            width: '100%',
            defaults: {
                labelWidth: 250
            },
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp',
                    //width: 430,
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    width: '100%',
                    items: [
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            action: 'addAction',
                            itemId: 'addButton'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancelLink',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            handler: function(button){
                                button.up('.window').close();
                            }
                        }
                    ]
                }
            ]
        }
    ],


    initComponent: function () {
        me = this;
//        this.down('#comTaskGrid').
        me.callParent(arguments);
//        me.mon(Ext.getBody(), 'click', function(el, e){
//            me.close(me.closeAction);
//        }, me, { delegate: '.x-mask' });
    }

});

