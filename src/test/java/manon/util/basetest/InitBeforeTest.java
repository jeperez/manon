package manon.util.basetest;

import org.testng.annotations.BeforeMethod;

/**
 * Recreate data beforeMethod every test method.
 */
public abstract class InitBeforeTest extends InitBeforeClass {
    
    @Override
    @BeforeMethod
    public void beforeMethod() throws Exception {
        setInitialized(false);
        super.beforeMethod();
    }
}
