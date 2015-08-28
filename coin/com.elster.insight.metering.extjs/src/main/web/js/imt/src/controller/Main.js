/**
 * @class Imt.controller.Main
 */
Ext.define('Imt.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Imt.usagepointmanagement.controller.View',
        'Imt.usagepointmanagement.controller.Edit',
        'Imt.devicemanagement.controller.Device',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.devicemanagement.model.Device',
        'Imt.channeldata.controller.View',
        'Imt.registerdata.controller.View'
    ],

    privileges: [],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.devicemanagement.model.Device',
        'Imt.channeldata.model.Channel',
        'Imt.registerdata.model.Register',
    ],
    controllers: [
		'Imt.usagepointmanagement.controller.View',
		'Imt.usagepointmanagement.controller.Edit',
		'Imt.devicemanagement.controller.Device',
        'Imt.channeldata.controller.View',
        'Imt.registerdata.controller.View'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ]
});
