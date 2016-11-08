Ext.define('CSMonitor.controller.logging.communication.Text', {
    extend: 'CSMonitor.controller.logging.Text',

    requires: [
        'CSMonitor.controller.logging.Text'
    ],

    views: ['logging.communication.Text', 'logging.communication.BytesViewer'],
    config: {
        deviceNames: '',
        portNames: ''
    },

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'communicationLoggingText'
        },
        {
            ref: 'generalLoggingText button#hrefForDownload',
            selector: 'hrefForDownload'
        },
        {
            ref: 'bytesViewPanel',
            selector: 'bytesViewer'
        },
        {
            ref: 'mainContainer',
            selector: 'app-main'
        }
    ],

    init: function() {
        this.control({
            'communicationLoggingText': {
                afterrender: this.onAfterRender
            },
            'communicationLoggingText button#pauseLoggingBtn': {
                click: this.pauseLogging
            },
            'communicationLoggingText button#saveLoggingBtn': {
                click: this.saveLogging
            },
            'communicationLoggingText combobox#logLevelCombo': {
                select: this.onLogLevelChange
            },
            'bytesViewer': {
                afterrender: this.onAfterRenderBytesViewer
            }
        });
    },

    onOpen: function(evt) {
        this.callParent(arguments);
        this.doLogReadWriteMessage('Listening started', this.getMessageClass());
    },

    onAfterRender: function() {
        this.getViewPanel().setTitle('<h2>Communication logging</h2>');
        this.getViewPanel().addLogPanel('Protocol logging');
        this.getViewPanel().setUnselectable();
  //      this.getViewPanel().setSaveLogBtnVisible(document.createElement('a').hasOwnProperty('download'));

        this.setPingTask(Ext.TaskManager.newTask({
            interval: this.getSecondsForNextPing() * 1000,
            scope: this,
            run: this.doPing
        }));

        this.subscribe();

        var criteriaInfo = '',
            addDelimiter = false;

        if (this.getDeviceNames()) {
            criteriaInfo += ('Device name = ' + this.getDeviceNames());
            addDelimiter = true;
        }
        if (this.getPortNames().length > 0) {
            if (addDelimiter) {
                criteriaInfo += ' - ';
            }
            criteriaInfo += ('Communication port name = ' + this.getPortNames());
            addDelimiter = true;
        }

        this.getViewPanel().setSelectionCriteria(criteriaInfo);
    },

    onAfterRenderBytesViewer: function() {
        this.getBytesViewPanel().syncScrolling();
    },

    registerForLogging: function() {
        if (this.getDeviceNames().length > 0) {
            this.getWebSocket().send('register request for device: ' + this.getDeviceNames());
        }

        if (this.getPortNames().length > 0) {
            this.getWebSocket().send('register request for comPort: ' + this.parserFriendly(this.getPortNames()));
        }
    },

    registerForChangedLoggingLevel: function() {
        var msg = 'register request for ';
        msg += this.getLevelDictionarySnd()[this.getViewPanel().getLogLevel()[0]];
        msg += ': CONNECTION,COMTASK,LOGGING';
        this.getWebSocket().send(msg);
    },

    onTokens: function(tokens) {
        if (tokens[1] === 'comm') {

            var criteria = tokens[2].split("&"),
                index,
                nameValuePair;

            for (index = 0; index < criteria.length; index += 1) {
                nameValuePair = criteria[index].split('=');
                if (nameValuePair[0] === 'devid') {
                    this.setDeviceNames(nameValuePair[1]);
                } else if (nameValuePair[0] === 'portid') {
                    this.setPortNames(nameValuePair[1]);
                }
            }
            this.getMainContainer().addCenterPanel('communicationLoggingText');
            this.getMainContainer().addSouthPanel('bytesViewer', 'Communication tracing');
        }
    },

    saveLogging: function() {
        this.doSaveLogging("protocol_logging.txt");
    },

    parserFriendly: function (allnames){
        var parserfriendly = '';
        var names = allnames.split(",");
        for	(index = 0; index < names.length; index++) {
            parserfriendly += '\'' + names[index]+'\'';
            if (index < names.length -1){
                parserfriendly += ','
            }
        }
        return parserfriendly;
    }
});
