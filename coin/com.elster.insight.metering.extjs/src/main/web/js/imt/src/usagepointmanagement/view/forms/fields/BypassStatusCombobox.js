/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.BypassStatusCombobox', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.techinfo-bypassstatuscombobox',
    name: 'bypassStatus',
    fieldLabel: Uni.I18n.translate('general.label.bypassStatus', 'IMT', 'Bypass status'),
    store: 'Imt.usagepointmanagement.store.BypassStatuses',
    displayField: 'displayValue',
    valueField: 'id',
    queryMode: 'local',
    forceSelection: true,
    emptyText: Uni.I18n.translate('usagepoint.add.emptyText.bypassStatus', 'IMT', 'Bypass status...'),
    listeners: {
        change: {
            fn: function (field, newValue) {
                if (Ext.isEmpty(newValue)) {
                    field.reset();
                }
            }
        }
    }
});