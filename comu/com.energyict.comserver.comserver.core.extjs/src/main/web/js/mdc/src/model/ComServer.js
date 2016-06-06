Ext.define('Mdc.model.ComServer', {
    extend: 'Uni.model.Version',
    uses: [
        'Mdc.util.UriParser'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'comServerType', type: 'string', useNull: true},
        {name: 'displayComServerType', type: 'string', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'active', type: 'boolean', useNull: true},
        {name: 'serverLogLevel', type: 'string', useNull: true},
        {name: 'communicationLogLevel', type: 'string', useNull: true},
        {name: 'queryAPIPostUri', type: 'string', useNull: true},
        {name: 'eventRegistrationUri', type: 'string', useNull: true},
        {name: 'statusUri', type: 'string', useNull: true},
        {name: 'storeTaskQueueSize', type: 'int', useNull: true},
        {name: 'numberOfStoreTaskThreads', type: 'int', useNull: true},
        {name: 'storeTaskThreadPriority', type: 'int', useNull: true},
        'inboundComPorts',
        'outboundComPorts',
        'changesInterPollDelay',
        'schedulingInterPollDelay',
        {name: 'onlineComServerId', type: 'int', useNull: true},
        {
            name: 'comportslink',
            persist: false,
            mapping: function (data) {
                var inboundComPorts = data.inboundComPorts ? data.inboundComPorts.length : 0,
                    outboundComPorts = data.outboundComPorts ? data.outboundComPorts.length : 0,
                    comports = inboundComPorts + outboundComPorts;
                return '<a href="#/administration/comservers/' + data.id + '/comports">'
                    + Uni.I18n.translatePlural('comserver.preview.communicationPorts', parseInt(comports), 'MDC',
                        'No communication ports', '{0} communication port', '{0} communication ports')
                    + '</a>';
            }
        },
        {
            name: 'serverName',
            persist: false,
            mapping: function (data) {
                if (data.eventRegistrationUri){
                    return Ext.create('Mdc.util.UriParser').parse(data.statusUri).hostname;
                }
                return '';
            }
        },
        {
            name: 'monitorPort',
            persist: false,
            mapping: function (data) {
                if (data.eventRegistrationUri){
                    return Ext.create('Mdc.util.UriParser').parse(data.statusUri).port;
                }
                return '';
            }
        },
        {
            name: 'eventRegistrationPort',
            persist: false,
            mapping: function (data) {
                if (data.eventRegistrationUri){
                    return Ext.create('Mdc.util.UriParser').parse(data.eventRegistrationUri).port;
                }
                return '';
            }
        }
    ],
    associations: [
        {name: 'changesInterPollDelay', type: 'hasOne', model: 'Mdc.model.field.TimeInfo', associationKey: 'changesInterPollDelay'},
        {name: 'schedulingInterPollDelay', type: 'hasOne', model: 'Mdc.model.field.TimeInfo', associationKey: 'schedulingInterPollDelay'},
        {name: 'outboundComPorts', type: 'hasMany', model: 'Mdc.model.ComPort', foreignKey: 'comserver_id', associationKey: 'outboundComPorts',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.OutboundComPort';
            }
        },
        {name: 'inboundComPorts', type: 'hasMany', model: 'Mdc.model.InboundComPort', foreignKey: 'comserver_id', associationKey: 'inboundComPorts',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.InboundComPort';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/mdc/comservers',
        reader: {
            type: 'json'
        }
    },
    updateHostNameOfUrisIfNeeded: function (){
        var currentStatusUri =  Ext.create('Mdc.util.UriParser').parse(this.get('statusUri')),
            currentEventRegistrationUri =  Ext.create('Mdc.util.UriParser').parse(this.get('eventRegistrationUri'));
        var serverName = this.get('serverName');
        if (serverName !== currentStatusUri.hostname) {
            if (!serverName) {
                serverName = 'servername_missing';  // Will be recognized by backend as server name missing
            }

            this.set('statusUri', currentStatusUri.withHostName(serverName).buildUrl());
            this.set('eventRegistrationUri', currentEventRegistrationUri.withHostName(serverName).buildUrl());

        }
    },
    updateMonitorAndStatusPortIfNeeded: function (){
        var currentStatusUri =  Ext.create('Mdc.util.UriParser').parse(this.get('statusUri'));
        var monitorPort = this.get('monitorPort');
        if (monitorPort !== currentStatusUri.port) {
            if (!monitorPort) {
                monitorPort = '0'; // Will be recognized by backend as port missing
            }
            this.set('statusUri', currentStatusUri.withPort(monitorPort).buildUrl());
        }
    },
    updateEventRegistrationPortIfNeeded: function (){
        var currentEventRegistrationUri =  Ext.create('Mdc.util.UriParser').parse(this.get('eventRegistrationUri'));
        var eventRegistrationPort = this.get('eventRegistrationPort');
        if (eventRegistrationPort != currentEventRegistrationUri.port) {
            if (!eventRegistrationPort) {
                eventRegistrationPort = '0'; // Will be recognized by backend as port missing
            }
            this.set('eventRegistrationUri', currentEventRegistrationUri.withPort(eventRegistrationPort).buildUrl());
        }
    }

});
