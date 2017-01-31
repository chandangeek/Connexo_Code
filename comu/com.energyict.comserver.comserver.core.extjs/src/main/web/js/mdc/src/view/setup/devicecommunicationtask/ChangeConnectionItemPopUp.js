/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtask.ChangeConnectionItemPopUp', {
    extend: 'Ext.window.Window',
    alias: 'widget.changeConnectionItemPopUp',
    requires: [
        'Mdc.widget.ScheduleField'
    ],
    width: 500,
    height: 150,
    closable: false,
    autoShow: true,
    modal: true,
    floating: true,
    itemId: 'changeConnectionItemPopUp',
    shadow: true,
    items: [
        {

            xtype: 'form',
            border: false,
            itemId: 'changeConnectionItemForm',
            layout: {
                type: 'vbox',
                align: 'stretch',
                margin: '10 0 0 0px'
            },
            defaults: {
                labelWidth: 150
            },
            items: []
        }
    ],

    bbar: [

        {
            xtype: 'container',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
            xtype: 'button',
            ui: 'action',
            itemId: 'changeButton',
            flex: 0
        },
        {
            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
            xtype: 'button',
            ui: 'link',
            itemId: 'cancelLink',
            handler: function (button) {
                button.up('.window').close();
            },
            flex: 0
        }
    ],
    initComponent: function () {
        var me = this;
        this.callParent(arguments);
        this.down('#changeButton').action = this.action;
        Ext.suspendLayouts();
        switch (this.action) {
            case 'changeConnectionMethodOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethodOf', 'MDC', "Change connection method of '{0}'", [this.comTaskName]));
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
            case 'changeProtocolDialectOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeProtocolDialectOfDeviceComTask', 'MDC', "Change protocol dialect of {0}", [this.comTaskName]));
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
            case 'changeUrgencyOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeUrgencyOfDeviceComTask', 'MDC', 'Change urgency of {0}', [this.comTaskName]));
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
        Ext.resumeLayouts();
    }
});

