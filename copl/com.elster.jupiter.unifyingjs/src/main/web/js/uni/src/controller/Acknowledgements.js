/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.controller.Acknowledgements
 *
 * Acknowledgements controller that is responsible for displaying acknowledgements
 * and removing them from the screen when required.
 */
Ext.define('Uni.controller.Acknowledgements', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.window.Acknowledgement'
    ],

    init: function () {
        this.getApplication().on('acknowledge', this.showAcknowledgement);
    },

    /**
     *
     * @param {String} message Message to show as acknowledgement.
     */
    showAcknowledgement: function (message) {
        var msgWindow = Ext.widget('acknowledgement-window'),
            task = new Ext.util.DelayedTask(function () {
                msgWindow.close();
            });

        msgWindow.setMessage(message);
        msgWindow.center();
        msgWindow.setPosition(msgWindow.x, 116, false);
        msgWindow.doLayout();
        task.delay(5000);
    }
});