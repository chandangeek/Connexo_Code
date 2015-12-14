Ext.define('Mdc.view.setup.comport.OutboundComPortEdit', {
    extend: 'Ext.window.Window',
    alias: 'widget.outboundComPortEdit',
    autoScroll: true,
    title: Uni.I18n.translate('comserver.comServer','MDC','ComServer'),
    width: '80%',
    height: '90%',
    modal: true,
    constrain: true,
    autoShow: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,


    initComponent: function () {
        var comporttypes = Ext.create('Mdc.store.ComPortTypes');
        var comportpools = Ext.create('Mdc.store.ComPortPools');
        comportpools.filter('direction','outbound');
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
                                fieldLabel: 'Communication port type',
                                store: comporttypes,
                                queryMode: 'local',
                                displayField: 'localizedValue',
                                valueField: 'id'
                            },
                            {
                                xtype: 'textfield',
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('general.description','MDC','Description')
                            },
                            {
                                xtype: 'numberfield',
                                name: 'numberOfSimultaneousConnections',
                                fieldLabel: 'numberOfSimultaneousConnections'
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

