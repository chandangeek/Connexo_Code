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
            fieldLabel: Uni.I18n.translate('comtask.load.profile.type','MDC','Load profile type'),
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
                    fieldLabel: Uni.I18n.translate('comtask.mark.intervals.as.bad.time','MDC','Mark intervals as bad time'),
                    afterLabelTextTpl: '<img src="../sky/build/resources/images/shared/icon-info-small.png">',
                    labelWidth: 350,
                    width: 400,
                    defaults: {
                        name: 'intervals',
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
                                    fieldLabel: Uni.I18n.translate('comtask.minimum.clock.difference','MDC','Minimum clock difference'),
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
                            text: Uni.I18n.translate('comtask.minimum.clock.difference','MDC','Minimum clock difference')
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioEvents',
            fieldLabel: Uni.I18n.translate('comtask.meter.events.from.status.flag','MDC','Meter events from status flag'),
            afterLabelTextTpl: '<img src="../sky/build/resources/images/shared/icon-info-small.png">',
            labelWidth: 350,
            width: 400,
            defaults: {
                name: 'events',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: Uni.I18n.translate('general.yes','MDC','Yes'), inputValue: 'true'},
                {boxLabel: Uni.I18n.translate('general.no','MDC','No'), inputValue: 'false', checked: true}
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioFail',
            fieldLabel: Uni.I18n.translate('comtask.fail.profile.configuration.doesnt.match','MDC',"Fail if profile configuration doesn't match"),
            afterLabelTextTpl: '<img src="../sky/build/resources/images/shared/icon-info-small.png">',
            labelWidth: 350,
            width: 400,
            defaults: {
                name: 'fail',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: Uni.I18n.translate('general.yes','MDC','Yes'), inputValue: 'true'},
                {boxLabel: Uni.I18n.translate('general.no','MDC','No'), inputValue: 'false', checked: true}
            ]
        }
    ]
});

