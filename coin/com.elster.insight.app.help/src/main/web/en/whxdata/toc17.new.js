/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

(function () {
    var toc = [{"type": "item", "name": "Confirm data", "url": "SCX-AG-MREN-000103-Insight/000107_UMNL_Data_editing/Confirm_data.htm"}, {
        "type": "item",
        "name": "Edit data",
        "url": "SCX-AG-MREN-000103-Insight/000107_UMNL_Data_editing/Edit_data.htm"
    }, {"type": "item", "name": "Estimate channel readings", "url": "SCX-AG-MREN-000103-Insight/000107_UMNL_Data_editing/Estimate_channel_readings.htm"}, {
        "type": "item",
        "name": "Reset data",
        "url": "SCX-AG-MREN-000103-Insight/000107_UMNL_Data_editing/Reset_data.htm"
    }];
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), toc, {sync: true});
})();