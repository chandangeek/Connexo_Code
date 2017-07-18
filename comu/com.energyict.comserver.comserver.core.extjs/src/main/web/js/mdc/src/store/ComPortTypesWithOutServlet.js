/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ComPortTypesWithOutServlet', {
    extend: 'Mdc.store.ComPortTypes',
    listeners: {
        load: function () {
            var index = this.find('id', 'TYPE_SERVLET');

            if (index !== -1) {
                this.remove(this.getAt(index));
            }
        }
    }
});