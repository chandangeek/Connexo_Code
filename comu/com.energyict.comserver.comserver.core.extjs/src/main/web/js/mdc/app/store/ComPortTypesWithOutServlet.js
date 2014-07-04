Ext.define('Mdc.store.ComPortTypesWithOutServlet', {
    extend: 'Mdc.store.ComPortTypes',
    listeners: {
        load: function () {
            var index = this.find('comPortType', 'SERVLET');

            if (index !== -1) {
                this.remove(this.getAt(index));
            }
        }
    }
});