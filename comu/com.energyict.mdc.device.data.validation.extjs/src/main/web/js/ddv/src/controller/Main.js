Ext.define('Ddv.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.Auth',
    ],

    controllers: [
        'Ddv.controller.ValidationOverview'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        this.callParent();
    },
    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
