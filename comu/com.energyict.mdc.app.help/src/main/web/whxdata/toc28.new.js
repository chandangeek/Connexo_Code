/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

(function () {
    var toc = [{
        "type": "book",
        "name": "Schedule communication",
        "key": "toc29",
        "url": "000017_EN_Connexo_1.0_User_guide/000017_UMNL_Collect_remote_data/Schedule_communication.htm"
    }, {"type": "book", "name": "Collect data on request", "key": "toc30", "url": "000017_EN_Connexo_1.0_User_guide/000017_UMNL_Collect_remote_data/Collect_data_on_request.htm"}, {
        "type": "book",
        "name": "Monitor collected data",
        "key": "toc31",
        "url": "000017_EN_Connexo_1.0_User_guide/000017_UMNL_Collect_remote_data/Monitor_collected_data.htm"
    }];
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), toc, {sync: true});
})();