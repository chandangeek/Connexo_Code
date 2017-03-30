/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comserver.FieldContainerHelpMessage', {
    extend: 'Ext.Component',
    alias: 'widget.fieldContainerHelpMessage',
    text: null,
    listenedFieldCount: 0,
    caughtErrors: 0,
    style: 'color: #686868; font-style: italic',

    initComponent: function () {
        this.text && (this.html = this.text);
        this.callParent(arguments);
        this.on('afterrender', this.setErrorListeners, this, {single: true});
    },

    setText: function (text) {
        this.text = text;
        this.update(text);
    },

    getText: function () {
        return this.text;
    },

    setErrorListeners: function () {
        var me = this,
            parent = me.up('fieldcontainer'),
            formFields = parent.query('[isFormField=true]');

        me.listenedFieldCount = formFields.length;

        Ext.Array.each(formFields, function (field) {
            field.on('errorchange', me.onError, me);
            field.un('destroy', me.onError, me);
        });
    },

    onError: function (field, error) {
        error ? this.caughtErrors++ : this.caughtErrors--;
        this.caughtErrors ? this.hide() : this.show();
    }
});
