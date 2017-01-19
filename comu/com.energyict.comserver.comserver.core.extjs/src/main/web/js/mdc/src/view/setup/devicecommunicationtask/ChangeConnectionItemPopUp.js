Ext.define('Mdc.view.setup.devicecommunicationtask.ChangeConnectionItemPopUp', {
    extend: 'Ext.window.Window',
    alias: 'widget.changeConnectionItemPopUp',
    autoShow: true,
    modal: true,
    floating: true,
    frame: true,
    itemId: 'changeConnectionItemPopUp',
    items: [
        {
            xtype: 'form',
            itemId: 'changeConnectionItemForm',
            defaults: {
                width: 400,
                labelWidth: 170
            },
            items: [
                {
                    xtype: 'label',
                    itemId: 'mdc-padding-label',
                    margin: '20 0 20 0'
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        this.callParent(arguments);
        Ext.suspendLayouts();
        switch (this.action) {
            case 'changeConnectionMethodOfDeviceComTask': {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethodOf', 'MDC', "Change connection method of '{0}'", this.comTaskName));
                this.down('#changeConnectionItemForm').add(
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTask.connectionMethod', 'MDC', 'Connection method'),
                        name: 'name',
                        itemId: 'connectionMethodCombo',
                        displayField: 'name',
                        store: this.store,
                        queryMode: 'local',
                        value: this.init
                    });
                break;
            }
            case 'changeProtocolDialectOfDeviceComTask': {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeProtocolDialectOfDeviceComTask', 'MDC', "Change protocol dialect of {0}", this.comTaskName));
                this.down('#changeConnectionItemForm').add(
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTask.protocolDialect', 'MDC', 'Protocol dialect'),
                        name: 'name',
                        itemId: 'protocolDialectCombo',
                        displayField: 'name',
                        store: this.store,
                        queryMode: 'local',
                        value: this.init
                    });
                break;
            }
            case 'changeUrgencyOfDeviceComTask': {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeUrgencyOfDeviceComTask', 'MDC', 'Change urgency of {0}', this.comTaskName));
                this.down('#changeConnectionItemForm').add({
                    xtype: 'numberfield',
                    itemId: 'urgencyCombo',
                    name: 'urgency',
                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.urgency', 'MDC', 'Urgency'),
                    value: this.init
                });
                break;
            }
        }
        this.down('#changeConnectionItemForm').add(
            {
                xtype: 'fieldcontainer',
                fieldLabel: '&nbsp;',
                margin: '20 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'changeButton',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                        ui: 'action',
                        action: me.action
                    },
                    {
                        xtype: 'button',
                        itemId: 'cancelLink',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link',
                        handler: function () {
                            me.close();
                        }
                    }
                ]
            }
        );
        Ext.resumeLayouts();
    }
});

