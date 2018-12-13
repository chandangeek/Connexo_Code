/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.LoadLimiterTypeField', {
    extend: 'Ext.form.field.Text',
    alias: 'widget.techinfo-loadlimitertypefield',
    name: 'loadLimiterType',
    fieldLabel: Uni.I18n.translate('general.label.loadLimiterType', 'IMT', 'Load limiter type')
});