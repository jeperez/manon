package manon.app.info.api;

import manon.util.basetest.MockBeforeClass;
import manon.util.basetest.Rs;
import org.testng.annotations.Test;

public class InfoWSCtrlTest extends MockBeforeClass {
    
    @Test(dataProvider = DP_ALLOW_ADMIN)
    public void shouldVerifyGetAppVersion(Rs rs, Integer status) {
        rs.getRequestSpecification()
                .get(API_SYS + "/info/app-version")
                .then()
                .statusCode(status);
        verify(infoWS, status).getAppVersion();
    }
}
