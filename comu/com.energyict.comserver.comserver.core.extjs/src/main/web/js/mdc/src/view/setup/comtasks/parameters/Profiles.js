Ext.define('Mdc.view.setup.comtasks.parameters.Profiles', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo',
        'Mdc.util.ComboSelectedCount'
    ],
    alias: 'widget.communication-tasks-profilescombo',
    name: 'profiles',
    items: [
        {
            xtype: 'combobox',
            fieldLabel: 'Load profile type',
            plugins: ['selectedCount'],
            queryMode: 'local',
            multiSelect: true,
            itemId: 'checkProfileTypes',
            labelWidth: 200,
            required: true,
            width: 400,
            store: 'Mdc.store.LoadProfileTypes',
            displayField: 'name',
            valueField: 'id',
            allowBlank: false,
            editable: false,
            validateOnBlur: false,
            validateOnChange: false
        },
        {
            xtype: 'container',
            layout: 'column',
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'radioIntervals',
                    fieldLabel: 'Mark intervals as bad time ',
                    afterLabelTextTpl: '<img src="../ext/packages/uni-theme-skyline/resources/images/shared/icon-info-small.png">',
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
                            text: 'Minimum clock difference'
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioEvents',
            fieldLabel: 'Meter events from status flag ',
            afterLabelTextTpl: '<img src="../ext/packages/uni-theme-skyline/resources/images/shared/icon-info-small.png">',
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
            afterLabelTextTpl: '<img src="../ext/packages/uni-theme-skyline/resources/images/shared/icon-info-small.png">',
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

