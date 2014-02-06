Ext.define('Mdc.view.setup.devicetype.DeviceTypeEdit', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceTypeEdit',
    itemId: 'deviceTypeEdit',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-wrapper',
    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [

                {
                    xtype: 'form',
                    border: false,
                    itemId: 'deviceTypeEditForm',
                    padding: '10 10 0 10',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
//                    tbar: [
//                        {
//                            xtype: 'component',
//                            html: '<h4>Overview</h4>',
//                            itemId: 'deviceTypePreviewTitle'
//                        }
//                    ],

                    items: [
                        {
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name:',
                            labelAlign: 'right',
                            labelWidth:	150
                        },
                        {
                            xtype: 'combobox',
                            name: 'communicationProtocolName',
                            fieldLabel: 'Device Communication protocol:',
                            labelAlign: 'right',
                            labelWidth:	150
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth:	150,
                            //width: 430,
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    text: 'Create',
                                    xtype: 'button',
                                    action: 'createEditAction',
                                    itemId: 'createEditButton'
                                },
                                {
                                    xtype: 'box',
                                    padding: '2 0 0 10',
                                    autoEl: {
                                        tag: 'a',
                                        href: 'javascript:history.back()',
                                        html: 'Cancel'
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});
