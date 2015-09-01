Ext.define('Mdc.view.setup.comtasks.parameters.Profiles', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo',
        'Mdc.view.setup.comtasks.parameters.ComboWithToolbar'
    ],
    alias: 'widget.communication-tasks-profilescombo',
    name: 'profiles',
    items: [
        {
            xtype: 'combo-with-toolbar',
            fieldLabel: 'Load profile type',
            itemId: 'checkProfileTypes',
            store: 'Mdc.store.LoadProfileTypes'
        },
        {
            xtype: 'container',
            layout: 'column',
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'radioIntervals',
                    fieldLabel: 'Mark intervals as bad time ',
                    afterLabelTextTpl: '<img src="../sky/build/resources/images/shared/icon-info-small.png">',
                    labelWidth: 350,
                    width: 400,
                    defaults: {
                        name: 'intervals',
                        margin: '0 10 0 0'
                    },
                    items: [
                        {boxLabel: 'Yes', inputValue: 'true', id: 'radioYes'},
                        {boxLabel: 'No', inputValue: 'false', checked: true, id: 'radioNo'}
                    ],
                    listeners: {
                        change: function () {
                            var radioYes = Ext.getCmp('radioYes');
                            if (radioYes.getValue()) {
                                this.up().down('#disCont').setDisabled(false);
                            } else {
                                this.up().down('#disCont').setDisabled(true);
                            }
                        }
                    }
                },
                {
                    xtype: 'container',
                    layout: 'vbox',
                    itemId: 'disCont',
                    margin: '0 0 0 70',
                    disabled: true,
                    items: [
                        {
                            xtype: 'container',
                            layout: 'column',
                            items: [
                                {
                                    xtype: 'textfield',
                                    name: 'minimumclockdifference',
                                    itemId: 'disContNum',
                                    fieldLabel: 'Minimum clock difference',
                                    hideLabel: true,
                                    maskRe: /[0-9]+/,
                                    margin: '0 10 0 0',
                                    width: 30,
                                    value: 60
                                },
                                {
                                    xtype: 'communication-tasks-parameters-timecombo',
                                    width: 110,
                                    itemId: 'disContTime',
                                    value: 'seconds'
                                }
                            ]
                        },
                        {
                            xtype: 'label',
                            text: Uni.I18n.translate('profiles.minimumClockDifference','MDC','Minimum clock difference')
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioEvents',
            fieldLabel: 'Meter events from status flag ',
            afterLabelTextTpl: '<img src="../sky/build/resources/images/shared/icon-info-small.png">',
            labelWidth: 350,
            width: 400,
            defaults: {
                name: 'events',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: 'Yes', inputValue: 'true'},
                {boxLabel: 'No', inputValue: 'false', checked: true}
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioFail',
            fieldLabel: "Fail if profile configuration doesn't match ",
            afterLabelTextTpl: '<img src="../sky/build/resources/images/shared/icon-info-small.png">',
            labelWidth: 350,
            width: 400,
            defaults: {
                name: 'fail',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: 'Yes', inputValue: 'true'},
                {boxLabel: 'No', inputValue: 'false', checked: true}
            ]
        }
    ]
});

