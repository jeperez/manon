package manon.user.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import manon.user.document.User;
import manon.user.err.UserExistsException;
import manon.user.form.RegistrationForm;
import manon.user.form.UserPasswordUpdateForm;
import manon.user.form.UserUpdateForm;
import manon.user.model.UserPublicInfo;
import manon.util.Tools;
import manon.util.basetest.AbstractIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static io.restassured.http.ContentType.JSON;
import static manon.app.err.AbstractControllerAdvice.FIELD_ERRORS;
import static manon.user.model.RegistrationState.ACTIVE;
import static manon.user.model.RegistrationState.DELETED;
import static manon.user.model.UserAuthority.ROLE_PLAYER;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;

public class UserWSIntegrationTest extends AbstractIntegrationTest {
    
    @Override
    public int getNumberOfUsers() {
        return 3;
    }
    
    public Object[][] dataProviderShouldRegister() {
        return new Object[][]{
            {"JOHN", "12300"},
            {"BOB DYLAN AND FRIENDS", "PASSWORD"},
            {"A B CX_Y-Z", "secret  p4ssword!"}
        };
    }
    
    @ParameterizedTest
    @MethodSource("dataProviderShouldRegister")
    public void shouldRegister(String name, String pwd) throws Exception {
        whenAnonymous().getRequestSpecification()
            .body(RegistrationForm.builder().username(name).password(pwd).build())
            .contentType(JSON)
            .post(API_USER)
            .then()
            .statusCode(SC_CREATED);
        User user = userService.readByUsername(name);
        assertThat(user.getUsername()).isEqualTo(name);
        assertThat(user.getAuthorities()).isEqualTo(ROLE_PLAYER.name());
        assertThat(user.getVersion()).isGreaterThanOrEqualTo(0L);
        assertThat(user.getRegistrationState()).isEqualTo(ACTIVE);
        assertThat(pwd).as("don't store raw passwords!")
            .isNotEqualTo(user.getPassword())
            .doesNotContain(user.getPassword());
    }
    
    @Test
    public void shouldNotRegisterTwice() {
        whenAnonymous().getRequestSpecification()
            .body(RegistrationForm.builder().username("DUPLICATE").password("12300").build())
            .contentType(JSON)
            .post(API_USER)
            .then()
            .statusCode(SC_CREATED);
        whenAnonymous().getRequestSpecification()
            .body(RegistrationForm.builder().username("DUPLICATE").password("456789").build())
            .contentType(JSON)
            .post(API_USER)
            .then()
            .statusCode(SC_CONFLICT).contentType(JSON)
            .body(FIELD_ERRORS, Matchers.equalTo(UserExistsException.class.getSimpleName()));
    }
    
    @Test
    public void shouldDeleteAndLooseAuthorisations() throws Exception {
        whenP1().getRequestSpecification()
            .get(API_USER).then()
            .statusCode(SC_OK);
        whenP1().getRequestSpecification()
            .delete(API_USER).then()
            .statusCode(SC_OK);
        assertThat(userService.readOne(userId(1)).getRegistrationState()).isEqualTo(DELETED);
        whenP1().getRequestSpecification()
            .get(API_USER).then()
            .statusCode(SC_UNAUTHORIZED);
    }
    
    @Test
    public void shouldNotDeleteTwice() throws Exception {
        whenP1().getRequestSpecification()
            .delete(API_USER).then()
            .statusCode(SC_OK);
        assertThat(userService.readOne(userId(1)).getRegistrationState()).isEqualTo(DELETED);
        whenP1().getRequestSpecification()
            .delete(API_USER).then()
            .statusCode(SC_UNAUTHORIZED);
    }
    
    @Test
    public void shouldRead() throws Exception {
        Response res = whenP1().getRequestSpecification()
            .get(API_USER);
        res.then()
            .statusCode(SC_OK);
        User webUser = readValue(res, User.class);
        User dbUser = userService.readOne(userId(1)).toBuilder().password(null).build();
        assertThat(webUser).isEqualTo(dbUser);
    }
    
    @Test
    public void shouldReadVersion() throws Exception {
        Response res = whenP1().getRequestSpecification()
            .get(API_USER + "/version");
        res.then()
            .statusCode(SC_OK);
        Long webUserVersion = readValue(res, Long.class);
        Long dbUserVersion = userService.readOne(userId(1)).getVersion();
        assertThat(webUserVersion).isEqualTo(dbUserVersion);
    }
    
    public Object[][] dataProviderShouldUpdate() {
        return new Object[][]{
            {"", ""},
            {"nickname", "test.foo@bar.com"}
        };
    }
    
    @ParameterizedTest
    @MethodSource("dataProviderShouldUpdate")
    public void shouldUpdate(String nickname, String email) throws Exception {
        User userBefore = userService.readOne(userId(1));
        whenP1().getRequestSpecification()
            .body(UserUpdateForm.builder().nickname(nickname).email(email).build())
            .contentType(JSON)
            .put(API_USER + "/field")
            .then()
            .statusCode(SC_OK);
        User userAfter = userService.readOne(userId(1));
        User userExpected = userBefore.toBuilder()
            .email(email)
            .nickname(nickname)
            .version(userBefore.getVersion() + 1)
            .build();
        assertThat(userAfter).isEqualTo(userExpected);
    }
    
    @Test
    public void shouldUpdatePassword() {
        whenP1().getRequestSpecification()
            .body(UserPasswordUpdateForm.builder().oldPassword(pwd(1)).newPassword("a new password").build())
            .contentType(JSON)
            .put(API_USER + "/password")
            .then()
            .statusCode(SC_OK);
        RestAssured.given().auth().basic(name(1), "a new password")
            .get(API_USER)
            .then().statusCode(SC_OK);
    }
    
    @Test
    public void shouldUpdateWithLongestPassword() {
        String newPassword = Tools.fill("anewpassword", User.Validation.PASSWORD_MAX_LENGTH);
        whenP1().getRequestSpecification()
            .body(UserPasswordUpdateForm.builder().oldPassword(pwd(1)).newPassword(newPassword).build())
            .contentType(JSON)
            .put(API_USER + "/password")
            .then()
            .statusCode(SC_OK);
        RestAssured.given().auth().basic(name(1), newPassword)
            .get(API_USER)
            .then().statusCode(SC_OK);
    }
    
    @Test
    public void shouldUpdatePasswordWithDataLongerThanBCryptMaxLength() {
        // BCrypt truncates too long password. See https://security.stackexchange.com/questions/39849/does-bcrypt-have-a-maximum-password-length
        String newPassword = Tools.fill("anewpassword", User.Validation.PASSWORD_MAX_LENGTH);
        whenP1().getRequestSpecification()
            .body(UserPasswordUpdateForm.builder().oldPassword(pwd(1)).newPassword(newPassword).build())
            .contentType(JSON)
            .put(API_USER + "/password")
            .then()
            .statusCode(SC_OK);
        RestAssured.given().auth().basic(name(1), newPassword.substring(0, 80))
            .get(API_USER)
            .then().statusCode(SC_OK);
    }
    
    @Test
    public void shouldUpdatePasswordWithDataShorterThanBCryptMaxLength() {
        // BCrypt truncates too long passwords. See https://security.stackexchange.com/questions/39849/does-bcrypt-have-a-maximum-password-length
        String newPassword = Tools.fill("anewpassword", User.Validation.PASSWORD_MAX_LENGTH);
        whenP1().getRequestSpecification()
            .body(UserPasswordUpdateForm.builder().oldPassword(pwd(1)).newPassword(newPassword).build())
            .contentType(JSON)
            .put(API_USER + "/password")
            .then()
            .statusCode(SC_OK);
        RestAssured.given().auth().basic(name(1), newPassword.substring(0, 20))
            .get(API_USER)
            .then().statusCode(SC_UNAUTHORIZED);
    }
    
    @Test
    public void shouldNotUpdateBadPassword() {
        whenP1().getRequestSpecification()
            .body(UserPasswordUpdateForm.builder().oldPassword("wrongpassword").newPassword("a new password").build())
            .contentType(JSON)
            .put(API_USER + "/password")
            .then()
            .statusCode(SC_BAD_REQUEST);
        RestAssured.given().auth().basic(name(1), "a new password")
            .get(API_USER)
            .then().statusCode(SC_UNAUTHORIZED);
        RestAssured.given().auth().basic(name(1), pwd(1))
            .get(API_USER)
            .then().statusCode(SC_OK);
    }
    
    @Test
    public void shouldGetZeroFriends() {
        //GIVEN a user with no friend
        
        //WHEN he gets friends
        Response res = whenP1().getRequestSpecification()
            .get(API_USER + "/friends");
        res.then()
            .statusCode(SC_OK);
        List<UserPublicInfo> friends = Arrays.asList(res.getBody().as(UserPublicInfo[].class));
        
        //THEN he gets nothing
        assertThat(friends).isEmpty();
    }
}
