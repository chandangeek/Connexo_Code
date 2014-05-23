Ext.define('Isu.view.administration.communicationtasks.parameters.Profiles', {
    extend: 'Ext.form.Panel',
    requires: [
        'Isu.view.administration.communicationtasks.parameters.TimeCombo',
        'Isu.util.CheckCombo',
        'Isu.util.ComboSelectedCount'
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
            labelSeparator: '*',
            store: 'Isu.store.CommunicationTasksProfiles',
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
            margin: '10 0 0 150',
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'radioIntervals',
                    fieldLabel: 'Mark intervals as bad time ',
                    afterLabelTextTpl: '<img src="../../apps/issue/resources/images/icons/info.png">',
                    labelWidth: 220,
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
                                    width: 30,
                                    margin: '0 10 0 0',
                                    value: 60
                                },
                                {
                                    xtype: 'communication-tasks-parameters-timecombo',
                                    width: 100,
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
            margin: '0 0 0 131',
            fieldLabel: 'Meter events from status flag ',
            afterLabelTextTpl: '<img src="../../apps/issue/resources/images/icons/info.png">',
            labelWidth: 240,
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
            margin: '0 0 0 52',
            fieldLabel: "Fail if profile configuration doesn't match ",
            afterLabelTextTpl: '<img src="../../apps/issue/resources/images/icons/info.png">',
            labelWidth: 320,
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

