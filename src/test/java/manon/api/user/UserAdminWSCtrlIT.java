package manon.api.user;

import manon.util.basetest.AbstractMockIT;
import manon.util.web.Rs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class UserAdminWSCtrlIT extends AbstractMockIT {

    @ParameterizedTest
    @MethodSource(DP_ALLOW_ADMIN)
    void shouldVerifyFindAll(Rs rs, Integer status) {
        rs.getSpec()
            .get(API_USER_ADMIN + "/all")
            .then()
            .statusCode(status);
        verify(userAdminWS, status).findAll(any(), any());
    }

    @ParameterizedTest
    @MethodSource(DP_ALLOW_ADMIN)
    void shouldVerifyActivate(Rs rs, Integer status) {
        rs.getSpec()
            .pathParam("userId", UNKNOWN_ID)
            .post(API_USER_ADMIN + "/{userId}/activate")
            .then()
            .statusCode(status);
        verify(userAdminWS, status).activate(any(), eq(UNKNOWN_ID));
    }

    @ParameterizedTest
    @MethodSource(DP_ALLOW_ADMIN)
    void shouldVerifyBan(Rs rs, Integer status) {
        rs.getSpec()
            .pathParam("userId", UNKNOWN_ID)
            .post(API_USER_ADMIN + "/{userId}/ban")
            .then()
            .statusCode(status);
        verify(userAdminWS, status).ban(any(), eq(UNKNOWN_ID));
    }

    @ParameterizedTest
    @MethodSource(DP_ALLOW_ADMIN)
    void shouldVerifySuspend(Rs rs, Integer status) {
        rs.getSpec()
            .pathParam("userId", UNKNOWN_ID)
            .post(API_USER_ADMIN + "/{userId}/suspend")
            .then()
            .statusCode(status);
        verify(userAdminWS, status).suspend(any(), eq(UNKNOWN_ID));
    }

    @ParameterizedTest
    @MethodSource(DP_ALLOW_ADMIN)
    void shouldVerifySearch(Rs rs, Integer status) {
        rs.getSpec()
            .post(API_USER_ADMIN + "/search")
            .then()
            .statusCode(status);
        verify(userAdminWS, status).search(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource(DP_ALLOW_ADMIN)
    void shouldVerifySearchByIdentity(Rs rs, Integer status) {
        rs.getSpec()
            .post(API_USER_ADMIN + "/search/identity")
            .then()
            .statusCode(status);
        verify(userAdminWS, status).searchByIdentity(any(), any(), any(), any(), any());
    }
}
