Ext.define('Mdc.view.setup.comtasks.parameters.BasicCheck', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo',
    ],
    alias: 'widget.communication-tasks-basiccheck',
    name: 'basiccheck',
    items: [
        {
            xtype: 'radiogroup',
            itemId: 'radioVerifySerialNumber',
            fieldLabel: Uni.I18n.translate('comtask.verify.serial.number', 'MDC', 'Verify serial number'),
            labelWidth: 300,
            width: 500,
            defaults: {
                name: 'verifyserialnumber',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: Uni.I18n.translate('general.yes','MDC','Yes'), inputValue: 'true'},
                {boxLabel: Uni.I18n.translate('general.no','MDC','No'), inputValue: 'false', checked: true}
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioReadclockdifference',
            fieldLabel: Uni.I18n.translate('comtask.read.clock.difference','MDC','Read clock difference'),
            labelWidth: 300,
            width: 500,
            defaults: {
                name: 'readclockdifference',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: Uni.I18n.translate('general.yes','MDC','Yes'), inputValue: 'true', id: 'radioYes'},
                {boxLabel: Uni.I18n.translate('general.no','MDC','No'), inputValue: 'false', checked: true, id: 'radioNo'}
            ],
            listeners: {
                change: function () {
                    var radioYes = Ext.getCmp('radioYes');
                    if (radioYes.getValue()) {
                        this.up().down('#mdc-maxClockDifferenceBasicCheck').setDisabled(false);
                    } else {
                        this.up().down('#mdc-maxClockDifferenceBasicCheck').setDisabled(true);
                    }
                }
            }
        },
        {
            xtype: 'fieldcontainer',
            layout: 'hbox',
            itemId: 'mdc-maxClockDifferenceBasicCheck',
            msgTarget: 'under',
            fieldLabel: Uni.I18n.translate('comtask.maximum.clock.difference','MDC','Maximum clock difference'),
            labelWidth: 300,
            width: 500,
            disabled: true,
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'disContNum',
                    name: 'maximumclockdifference',
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    flex: 1,
                    value: 60
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'disContTime',
                    value: 'seconds',
                    flex: 3
                }
            ]
        }
    ]
});

