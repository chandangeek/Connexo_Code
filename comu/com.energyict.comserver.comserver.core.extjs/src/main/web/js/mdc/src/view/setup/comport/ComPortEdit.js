Ext.define('Mdc.view.setup.comport.ComPortEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comPortEdit',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,


    initComponent: function () {
        var comporttypes = Ext.create('Mdc.store.ComPortTypes');
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
                        title: Uni.I18n.translate('general.required','MDC','Required'),
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('general.name','MDC','Name')
                            },
                            {
                                xtype: 'combobox',
                                name: 'comPortType',
                                fieldLabel: Uni.I18n.translate('general.type','MDC','Type'),
                                store: comporttypes,
                                queryMode: 'local',
                                displayField: 'comPortType',
                                valueField: 'comPortType'
                            },
                            {
                                xtype: 'textfield',
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('general.description','MDC','Description')
                            },
                            {
                                xtype: 'numberfield',
                                name: 'numberOfSimultaneousConnections',
                                fieldLabel: Uni.I18n.translate('comServerComPorts.form.connectionCount', 'MDC', 'Simultaneous connections')
                            },
                            {
                                xtype: 'checkbox',
                                inputValue: true,
                                uncheckedValue: 'false',
                                name: 'active',
                                fieldLabel: Uni.I18n.translate('general.active','MDC','Active')
                            }
                        ]}
                ]
            }
        ];

        this.buttons = [
            {
                text: Uni.I18n.translate('general.save','MDC','Save'),
                action: 'save'
            },
            {
                text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});

