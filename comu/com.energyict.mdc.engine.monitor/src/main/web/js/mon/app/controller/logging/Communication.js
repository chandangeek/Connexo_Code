/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.logging.Communication', {
    extend: 'Ext.app.Controller',

    views: ['logging.Communication', 'logging.Criteria' ],
    requires: [Ext.String],

    config: {
        deviceName: '',
        comportName: ''
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
            'criteria textfield[name="deviceName"]': {
                change: this.onChangeDeviceName,
                specialkey: this.onSpecialKey
            },
            'criteria textfield[name="comportName"]': {
                change: this.onChangeComportName,
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
        if (!this.getDeviceName() && !this.getComportName()) {
            // No criteria defined, so warn/guide the user
            this.getViewPanel().warnForEmptyCriteria();
            this.getCriteriaPanel().warnForEmptyCriteria();
        } else {
            this.getViewPanel().openWindow(this.constructUrl());
        }
    },

    constructUrl: function(mode) {
        var url = '#logging/comm/',
            appendAnd = false;

        if (this.getDeviceName()) {
            url += ('devid=' + Ext.String.htmlEncode(this.getDeviceName()));
            appendAnd = true;
        }
        if (this.getComportName()) {
            if (appendAnd) {
                url += '&';
            }
            url += ('portid=' +  Ext.String.htmlEncode(this.getComportName()));
        }
        return url;
    },

    onChangeDeviceName: function (field, newValue, oldValue) {
        newValue = newValue.replace(/\s*,\s*/g, ",");
        this.setDeviceName(newValue.trim());

    },

    onChangeComportName: function(field, newValue, oldValue) {
        newValue = newValue.replace(/\s*,\s*/g, ",");
        this.setComportName(newValue.trim());
    },

    onSpecialKey: function(field, event) {
        if (event.getKey() === event.ENTER) {
            this.onCommunicationClicked();
        }
    }

    //stripInvalidCharacters: function(originalValue) {
    //    var pattern = new RegExp('[^0-9,]', 'gi');
    //    if (pattern.test(originalValue)) {
    //        return originalValue.replace(pattern, '');
    //    }
    //    return originalValue;
    //}

});
