/**
 * @class Imt.controller.Main
 */
Ext.define('Imt.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Imt.usagepointmanagement.controller.View',
        'Imt.usagepointmanagement.controller.Edit',
        'Imt.devicemanagement.controller.Device'
    ],

    privileges: [],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.devicemanagement.model.Device'
    ],
    controllers: [
		'Imt.usagepointmanagement.controller.View',
		'Imt.usagepointmanagement.controller.Edit',
		'Imt.devicemanagement.controller.Device'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ]
});
