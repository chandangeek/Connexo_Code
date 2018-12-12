/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.BaseInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.base-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField',
        'Imt.usagepointmanagement.view.forms.fields.BypassField',
        'Imt.usagepointmanagement.view.forms.fields.BypassStatusCombobox',
        'Imt.usagepointmanagement.view.forms.fields.LimiterCombobox',
        'Imt.usagepointmanagement.view.forms.fields.LoadLimiterTypeField',
        'Imt.usagepointmanagement.view.forms.fields.LoadLimitField'
    ],

    predefinedRecord: null,

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        if (me.predefinedRecord) {
            me.loadRecord(me.predefinedRecord);
        }
    }
});