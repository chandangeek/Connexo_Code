Ext.define('Mdc.view.setup.comportpool.InboundComPortPoolEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.inboundComPortPoolEdit',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,


    initComponent: function () {
      //  var comporttypes = Ext.create('Mdc.store.ComPortTypes');
        this.items = [
            {
                xtype: 'form',
                shrinkWrap: 1,
                padding: 10,
                border: 0,
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'fieldset',
                        title: 'Required',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: 'Name'
                            },
                            {
                                xtype: 'textfield',
                                name: 'decription',
                                fieldLabel: 'Description'
                            },
                            {
                                xtype: 'textfield',
                                name: 'type',
                                fieldLabel: 'Type'
                            },
                            {
                                xtype: 'checkbox',
                                name: 'active',
                                inputValue: true,
                                uncheckedValue: 'false',
                                fieldLabel: 'active'
                            }
                        ]}
                ]
            }
        ];

        this.buttons = [
            {
                text: 'Save',
                action: 'save'
            },
            {
                text: 'Cancel',
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});


