Ext.define('Mdc.view.setup.dataloggerslaves.DataLoggerSlavesLinkWizardStep1', {
    extend: 'Ext.container.Container',
    alias: 'widget.datalogger-slave-link-wizard-step1',
    requires: [
        'Mdc.store.AvailableDataLoggerSlaves',
        'Mdc.view.setup.dataloggerslaves.DataLoggerSlaveDeviceAdd',
        'Mdc.view.setup.dataloggerslaves.MultiElementSlaveDeviceAdd'
    ],
    layout: {
        type: 'vbox'
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'radiogroup',
                        itemId: 'mdc-step1-radiogroup',
                        columns: 1,
                        fieldLabel: '',
                        vertical: true,
                        defaults: {
                            margin: '0 15 0 0'
                        },
                        items: [
                            {
                                itemId: 'mdc-existing-dataloggerslave-option',
                                boxLabel: Uni.I18n.translate('linkwizard.step1.option.existing', 'MDC', 'Existing data logger slave'),
                                name: 'useExisting',
                                inputValue: true,
                                checked: true,
                                margin: '20 15 0 0'
                            },
                            {
                                itemId: 'mdc-new-dataloggerslave-option',
                                boxLabel: Uni.I18n.translate('linkwizard.step1.option.new', 'MDC', 'New slave device'),
                                name: 'useExisting',
                                inputValue: false
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'combobox',
                                width: 386,
                                itemId: 'mdc-step1-slave-combo',
                                store: 'Mdc.store.AvailableDataLoggerSlaves',
                                forceSelection: true,
                                displayField: 'name',
                                valueField: 'name',
                                emptyText: Uni.I18n.translate('general.selectADataLoggerSlave', 'MDC', 'Start typing to select a data logger slave...'),
                                msgTarget: 'under',
                                margin: '20 0 40 0',
                                queryMode: 'remote',
                                queryParam: 'like',
                                queryDelay: 500,
                                queryCaching: false,
                                minChars: 1,
                                editable: true,
                                typeAhead: true
                            }
                        ]
                    }
                ]
            },
            {
               xtype: 'datalogger-slave-device-add',
               deviceTypeStore: 'Mdc.store.AvailableSlaveDeviceTypes',
               hidden: true
            }
        ];
        me.callParent(arguments);
    },
    setDeviceTypePurpose: function(deviceTypePurpose){
        switch (deviceTypePurpose) {
            case Mdc.LinkPurpose.LINK_DATALOGGER_SLAVE.value:
                this.items.items[1].hidden = false;
                this.items.items[2].hidden = true;
                break;
            case Mdc.LinkPurpose.LINK_MULTI_ELEMENT_SLAVE.value:
                this.items.items[1].hidden = true;
                this.items.items[2].hidden = false;
                break;
        }
    }
})
;
