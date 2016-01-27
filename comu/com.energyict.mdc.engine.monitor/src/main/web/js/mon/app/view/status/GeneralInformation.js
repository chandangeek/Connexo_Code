Ext.define('CSMonitor.view.status.GeneralInformation', {
    extend: 'Ext.panel.Panel',
    requires: ['Ext.ProgressBar', 'Ext.form.field.Checkbox', 'Ext.form.field.ComboBox'],
    xtype: 'generalInformation',
    border: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'container',
            margins: '0 0 0 10',
            height: 52,
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h2>General polling information</h2>'
                },
                {
                    xtype: 'component',
                    flex: 1
                },
                {
                    xtype: 'progressbar',
                    width: 150,
                    margins: '0 10 0 0',
                    animate: true,
                    itemId: 'waitTimeProgressBar'
                },
                {
                    xtype: 'checkbox',
                    margins: '0 10 0 0',
                    boxLabel: 'Auto refresh every',
                    itemId: 'autoRefreshChkBox'
                },
                {
                    xtype: 'combobox',
                    margins: '0 10 0 0',
                    store : {
                        fields: ['rate', 'seconds'],
                        data : [
                            {"rate": "1 minute", "seconds": 60},
                            {"rate": "2 minutes", "seconds": 120},
                            {"rate": "5 minutes", "seconds": 300},
                            {"rate": "10 minutes", "seconds": 600}
                        ]
                    },
                    displayField: 'rate',
                    valueField: 'seconds',
                    value: 300,
                    queryMode: 'local',
                    editable: false,
                    itemId: 'refreshRateCombo'
                },
                {
                    xtype: 'button',
                    margins: '0 10 0 0',
                    text: 'Refresh',
                    itemId: 'refreshBtn'
                }
            ]
        },
        {
            xtype: 'form',
            border: false,
            margins: '2 0 2 20',
            defaultType: 'textfield',
            items: [{
                fieldLabel: 'Changes inter poll delay',
                name: 'changeDetectionFrequency',
                labelWidth: 200,
                width: 600,
                fieldCls: 'no-border-field'
            },
            {
                fieldLabel: 'Scheduling inter poll delay',
                name: 'pollingFrequency',
                labelWidth: 200,
                width: 600,
                fieldCls: 'no-border-field'
            }]
        }
    ],

    setGeneralInformation: function(generalInfo) {
        var form = this.down('form'),
            nextRunText = "Next run",
            changeDetectionFrequency = generalInfo.get('changeDetectionFrequency'),
            nextRunDate = generalInfo.get('changeDetectionNextRun'),
            pollingFrequency =  generalInfo.get('pollingFrequency'),
            changeDetectionExpression = changeDetectionFrequency['count'] + ' ' + changeDetectionFrequency['time-unit']
        if (nextRunDate){
            changeDetectionExpression =  changeDetectionExpression  + ' (' + nextRunText + ': ' + nextRunDate + ')'
        }
        form.getForm().findField('changeDetectionFrequency').setValue(changeDetectionExpression);
        form.getForm().findField('pollingFrequency').setValue(
              pollingFrequency['count'] + ' ' + pollingFrequency['time-unit']
        );
    },

    getRefreshRateInSeconds: function() {
        return this.down('#refreshRateCombo').getSubmitValue();
    },

    setWaitInfo: function(secondsToWait, refreshRateInSeconds, text) {
        if (secondsToWait === 0 && refreshRateInSeconds === 0) {
            this.down('#waitTimeProgressBar').setVisible(false);
        } else {
            this.down('#waitTimeProgressBar').setVisible(true);
            this.down('#waitTimeProgressBar').updateProgress(secondsToWait / refreshRateInSeconds, text);
        }
    }
});