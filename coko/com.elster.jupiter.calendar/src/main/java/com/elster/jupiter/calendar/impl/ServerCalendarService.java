package com.elster.jupiter.calendar.impl;

        import com.elster.jupiter.calendar.CalendarService;
        import com.elster.jupiter.calendar.Category;
        import com.elster.jupiter.nls.Thesaurus;
        import com.elster.jupiter.orm.DataModel;

        import java.util.Optional;

/**
 * Created by igh on 18/04/2016.
 */
public interface ServerCalendarService extends CalendarService {

    DataModel getDataModel();

    Thesaurus getThesaurus();

    Optional<Category> findTimeOfUseCategory();
}
