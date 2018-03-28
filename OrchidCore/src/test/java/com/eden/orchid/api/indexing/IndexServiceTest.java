package com.eden.orchid.api.indexing;

import com.caseyjbrooks.clog.Clog;
import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.OrchidService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

@Test(groups={"services", "unit"})
public final class IndexServiceTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Clog.getInstance().setMinPriority(Clog.Priority.FATAL);
    }

    private OrchidContext context;
    private IndexService underTest;
    private IndexServiceImpl service;

    @BeforeMethod
    public void testSetup() {

        // test the service directly
        context = mock(OrchidContext.class);
        service = new IndexServiceImpl();
        service.initialize(context);

        // test that the default implementation is identical to the real implementation
        underTest = new IndexService() {
            public void initialize(OrchidContext context) { }
            public <T extends OrchidService> T getService(Class<T> serviceClass) { return (T) service; }
        };
    }

}
