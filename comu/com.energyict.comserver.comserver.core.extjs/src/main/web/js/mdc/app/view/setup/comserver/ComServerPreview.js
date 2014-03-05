Ext.define('Mdc.view.setup.comserver.ComServerPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServerPreview',
    requires: [
        'Mdc.store.ComServers'
    ],
    controllers: [
        'Mdc.controller.setup.ComServers'
    ],
    itemId: 'comserverpreview',
    layout: 'fit',
    initComponent: function () {
        this.items = [
            {
                xtype: 'panel',
                border: 1,
                width: '100%',
                layout: 'vbox',
                collapsible: true,
                title: 'Selected communication server preview',
                itemId: 'previewpanel',
                collapsed: true,
                defaults: {
                    labelWidth: 200,
                    padding: 10,
                    border: 0
                },
                items: [
                    {
                        xtype: 'panel',
                        width: '100%',
                        layout: 'hbox',
                        items:[
                            {
                                xtype: 'component',
                                itemId: 'comserverName',
                                flex: 3
                            },
                            {
                                xtype: 'component',
                                itemId: 'comserverActive',
                                tpl: '<h3><tpl if="active==true"{active}><span style="color:lightgreen">active</span>' +
                                    '<tpl elseif="active==false"><span style="color:#ff0000">not active</span>' +
                                    '<tpl else><span style="color:#ff0000"></span></tpl></h3></h3>',
                                flex: 1
                            },
                            {
                                xtype: 'button',
                                text: 'Start/Stop',
                                action: 'startStop',
                                margins: '0 10 0 0'
                            },
                            {
                                xtype: 'button',
                                text: 'Edit',
                                action: 'edit'
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        width: '100%',
                        layout: {
                            type: 'hbox',
                            align: 'stretchmax'
                        },
                        defaults: {
                            border: false,
                            xtype: 'panel',
                            flex: 1,
                            layout: 'anchor'
                        },
                        fieldDefaults: {
                            labelAlign: 'right',
                            labelSeparator: ':'
                        },
                        items: [
                                {"xtype": 'outboundComPorts'},
                                {"xtype": 'inboundComPorts'}
                        ]
                    }
                ]
            }
        ]
        this.callParent();
    }
});
