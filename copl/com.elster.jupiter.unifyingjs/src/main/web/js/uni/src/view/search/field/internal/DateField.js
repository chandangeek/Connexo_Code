/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.DateField', {
    extend: 'Ext.form.field.Date',
    alias: 'widget.uni-search-internal-datefield',
    removable: false,

    getValue: function () {
        var value = this.callParent(arguments);

        return value ? moment(value).format('YYYY-MM-DD') : value;
    }
});