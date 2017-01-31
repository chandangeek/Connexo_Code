/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.component.UserCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-user-combo',
    store: 'Isu.store.Users',
    displayField: 'name',
    valueField: 'id',
    triggerAction: 'query',
    queryMode: 'remote',
    queryParam: 'like',
    allQuery: '',
    lastQuery: '',
    queryDelay: 100,
    minChars: 0,
    disableKeyFilter: true,
    queryCaching: true,
    typeAhead: true,
    forceSelection: true,
    formBind: false,
    emptyText: Uni.I18n.translate('issues.selectUser','ISU','Select user'),
    listConfig: {
        getInnerTpl: function(displayField) {
            return '<img src="../../apps/isu/resources/images/icons/USER.png"/> {' + displayField + '}';
        }
    }
});