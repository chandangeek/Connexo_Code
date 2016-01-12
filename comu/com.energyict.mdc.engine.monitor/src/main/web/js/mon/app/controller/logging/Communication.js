Ext.define('CSMonitor.controller.logging.Communication', {
    extend: 'Ext.app.Controller',

    views: ['logging.Communication', 'logging.Criteria'],

    config: {
        deviceId : '',
        comportId : '',
        connectionId: '',
        notDefined : ''
    },

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'communication'
        },
        {
            ref: 'criteriaPanel',
            selector: 'criteria'
        }
    ],

    init: function() {
        this.control({
            'communication': {
                afterrender: this.onAfterRender
            },
            'criteria textfield[name="deviceId"]': {
                change: this.onChangeDeviceId,
                specialkey: this.onSpecialKey
            },
            'criteria textfield[name="comportId"]': {
                change: this.onChangeComportId,
                specialkey: this.onSpecialKey
            },
            'criteria textfield[name="connectionId"]': {
                change: this.onChangeConnectionId,
                specialkey: this.onSpecialKey
            }
        });
    },

    onAfterRender: function() {
        var me = this;
        Ext.ComponentQuery.query('#communicationLoggingContainer')[0].el.addListener('click', function(event, element) {
            me.onCommunicationClicked(event, element);
        });
    },

    onCommunicationClicked: function(event, element) {
        if (this.getDeviceId() === this.getNotDefined() &&
                this.getComportId() === this.getNotDefined() &&
                this.getConnectionId() === this.getNotDefined()) {
            // No criteria defined, so warn/guide the user
            this.getViewPanel().warnForEmptyCriteria();
            this.getCriteriaPanel().warnForEmptyCriteria();
        } else {
            this.getViewPanel().openWindow(this.constructUrl());
        }
    },

    constructUrl: function(mode) {
        var url = '#logging/comm',
            appendAnd = false,
            deviceId = this.getNotDefined(),
            portId = this.getNotDefined(),
            connectionId = this.getNotDefined();

        url += '/';

        if (this.getDeviceId() !== this.getNotDefined()) {
            deviceId = this.getDeviceId();
            url += ('devid=' + deviceId.toString());
            appendAnd = true;
        }
        if (this.getComportId() !== this.getNotDefined()) {
            portId = this.getComportId();
            if (appendAnd) {
                url += '&';
            }
            url += ('portid=' + portId.toString());
            appendAnd = true;
        }
        if (this.getConnectionId() !== this.getNotDefined()) {
            connectionId = this.getConnectionId();
            if (appendAnd) {
                url += '&';
            }
            url += ('connid=' + connectionId.toString());
        }
        return url;
    },

    onChangeDeviceId: function(field, newValue, oldValue) {
//        console.log('Device id: old = ' + oldValue + ' - new = ' + newValue);
        var strippedValue = this.stripInvalidCharacters(newValue);
        this.setDeviceId(strippedValue);
        field.value = strippedValue;
    },

    onChangeComportId: function(field, newValue, oldValue) {
//        console.log('Comport id: old = ' + oldValue + ' - new = ' + newValue);
        var strippedValue = this.stripInvalidCharacters(newValue);
        this.setComportId(strippedValue);
        field.value = strippedValue;
    },

    onChangeConnectionId: function(field, newValue, oldValue) {
//        console.log('Connection id: old = ' + oldValue + ' - new = ' + newValue);
        var strippedValue = this.stripInvalidCharacters(newValue);
        this.setConnectionId(strippedValue);
        field.value = strippedValue;
    },

    onSpecialKey: function(field, event) {
        if (event.getKey() === event.ENTER) {
            this.onCommunicationClicked();
        }
    },

    stripInvalidCharacters: function(originalValue) {
        var pattern = new RegExp('[^0-9,]', 'gi');
        if (pattern.test(originalValue)) {
            return originalValue.replace(pattern, '');
        }
        return originalValue;
    }

});
