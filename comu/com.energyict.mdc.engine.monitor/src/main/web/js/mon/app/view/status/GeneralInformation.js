Ext.define('CSMonitor.view.status.GeneralInformation', {
    extend: 'Ext.panel.Panel',
    requires: ['Ext.ProgressBar', 'Ext.form.field.Checkbox', 'Ext.form.field.ComboBox'],
    xtype: 'generalInformation',
    border: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    config: {
        oneMinuteText : "1 minute",
        twoMinutesText : "2 minutes",
        fiveMinutesText : "5 minutes",
        tenMinutesText : "10 minutes"
    },

    items: [
        {
            xtype: 'container',
            margins: '0 0 0 10',
            html: '<h2>General information</h2>',

            height: 52,
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h2>General information</h2>'
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
                        fields: ['rate'],
                        data : [
                            {"rate": "1 minute"},
                            {"rate": "2 minutes"},
                            {"rate": "5 minutes"},
                            {"rate": "10 minutes"}
                        ]
                    },
                    value: "5 minutes",
                    queryMode: 'local',
                    displayField: 'rate',
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
            xtype: 'component',
            itemId: 'changeDetectionFrequency',
            margins: '2 0 2 20',
            html: 'Change detection frequency:'
        },
        {
            xtype: 'component',
            itemId: 'pollingFrequency',
            margins: '2 0 2 20',
            html: 'Polling frequency:'
        }
    ],

    setGeneralInformation: function(generalInfo) {
        var changeDetectionFrequencyText = "Changes inter poll delay",
            pollingFrequencyText = "Scheduling inter poll delay",
            nextRunText = "Next run",
            changeDetectionFrequency = generalInfo.get('changeDetectionFrequency'),
            nextRunDate = generalInfo.get('changeDetectionNextRun'),
            pollingFrequency =  generalInfo.get('pollingFrequency');

        this.down('#changeDetectionFrequency').update(
            changeDetectionFrequencyText + ': <b>' + changeDetectionFrequency['count'] + ' ' + changeDetectionFrequency['time-unit']
                + '</b> (' + nextRunText + ': ' + nextRunDate + ')'
        );
        this.down('#pollingFrequency').update(
            pollingFrequencyText + ': <b>' + pollingFrequency['count'] + ' ' + pollingFrequency['time-unit'] + '</b>'
        );
    },

    getRefreshRateInSeconds: function() {
        var currentRateText = this.down('#refreshRateCombo').getValue();
        switch (currentRateText) {
        case this.getOneMinuteText():
            return 60;
        case this.getTwoMinutesText():
            return 120;
        case this.getFiveMinutesText():
            return 300;
        case this.getTenMinutesText():
        default:
            return 600;
        }
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