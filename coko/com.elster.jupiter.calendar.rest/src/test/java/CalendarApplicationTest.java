import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.impl.CalendarApplication;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.NlsService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

public class CalendarApplicationTest extends FelixRestApplicationJerseyTest {
    @Mock
    public CalendarService calendarService;


    @Override
    protected Application getApplication() {
        CalendarApplication application = new CalendarApplication();
        application.setCalendarService(calendarService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        return application;
    }
}
