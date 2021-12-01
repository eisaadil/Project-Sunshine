package com.eisaadil.sunshine;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.TestSuite;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.eisaadil.sunshine", appContext.getPackageName());


    }
}

/*
package com.eisaadil.sunshine;

/**
 * Created by eisaadil on 24/01/17.
 */
/*
import android.test.suitebuilder.TestSuiteBuilder;

        import junit.framework.Test;
        import junit.framework.TestSuite;

public class FullTestSuite extends TestSuite {
    public static junit.framework.Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class).includeAllPackagesUnderHere().build();
    }

    public FullTestSuite() {
        super();
    }
}

 */