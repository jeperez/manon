package manon.api.user;

import io.restassured.response.Response;
import manon.document.user.Friendship;
import manon.document.user.FriendshipEvent;
import manon.document.user.FriendshipRequest;
import manon.document.user.User;
import manon.err.user.FriendshipNotFoundException;
import manon.err.user.FriendshipRequestExistsException;
import manon.err.user.FriendshipRequestNotFoundException;
import manon.err.user.UserNotFoundException;
import manon.model.user.UserPublicInfo;
import manon.util.TestTools;
import manon.util.basetest.AbstractIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static manon.model.user.FriendshipEventCode.TARGET_ACCEPTED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.TARGET_CANCELED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.TARGET_REJECTED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.TARGET_REVOKED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.TARGET_SENT_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.YOU_ACCEPTED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.YOU_CANCELED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.YOU_REJECTED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.YOU_REVOKED_FRIEND_REQUEST;
import static manon.model.user.FriendshipEventCode.YOU_SENT_FRIEND_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class FriendshipWSIntegrationTest extends AbstractIntegrationTest {
    
    @Override
    public int getNumberOfUsers() {
        return 3;
    }
    
    @AfterEach
    public void reinitFriendships() {
        friendshipEventRepository.deleteAll();
        friendshipRequestRepository.deleteAll();
        friendshipRepository.deleteAll();
    }
    
    //
    // askFriendship
    //
    
    /** Ask 2 friendship requests. */
    @Test
    public void shouldAskFriendshipToManyUsers() throws Exception {
        //GIVEN 3 users with no friends nor friendship requests
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        User p3 = userService.readOne(userId(3));
        
        //WHEN P1 sends friendship requests to P2 and P3
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(3))
            .then()
            .statusCode(SC_OK);
        
        //THEN P1 has 2 requests
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).isNotNull().isEmpty();
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).hasSize(2);
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1), userId(1));
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3), userId(2));
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(2);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 has 1 request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).isNotNull().isEmpty();
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).hasSize(1);
        assertThat(p2FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p2FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(1);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        
        //THEN P3 has 1 request
        List<Friendship> p3Friends = friendshipRepository.findAllFor(userId(3));
        assertThat(p3Friends).isNotNull().isEmpty();
        
        List<FriendshipRequest> p3FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p3FriendshipRequests).hasSize(1);
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipEvent> p3FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(3));
        assertThat(p3FriendshipEvents).hasSize(1);
        assertThat(p3FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    /** Ensure we don't store the same friendship request twice. */
    @Test
    public void shouldNotAskFriendshipTwice() throws Exception {
        //GIVEN 2 users with no friends nor friendship requests
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        
        //WHEN P1 asks friendship to P2 twice
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_CONFLICT)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestExistsException.class.getSimpleName()));
        
        //THEN P1 has 0 friend and 1 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).isEmpty();
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).hasSize(1);
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(1);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 has 0 friend and 1 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).isEmpty();
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).hasSize(1);
        assertThat(p2FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p2FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(1);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    @Test
    public void shouldNotAskFriendshipToFriend() throws Exception {
        //GIVEN 2 users that are friends
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        friendshipService.askFriendship(userId(1), userId(2));
        TestTools.temporize();
        friendshipService.acceptFriendshipRequest(userId(1), userId(2));
        TestTools.temporize();
        
        //WHEN P1 asks friendship to P2 again
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_CONFLICT);
        
        //THEN P1 still has 1 friend and 0 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).hasSize(1);
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(2);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_ACCEPTED_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 still has 1 friend and 0 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).hasSize(1);
        assertThat(p2Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p2Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(2);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_ACCEPTED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p2FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    /** Ensure we two users send crossed friendship request. */
    @Test
    public void shouldNotAskCrossedFriendship() throws Exception {
        //GIVEN 2 users with no friends nor friendship requests
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        
        //WHEN P1 asks friendship then P2 asks friendship to P1
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP2().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_CONFLICT)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestExistsException.class.getSimpleName()));
        
        //THEN P1 has 0 friend and 1 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).isEmpty();
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).hasSize(1);
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(1);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 has 0 friend and 1 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).isEmpty();
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).hasSize(1);
        assertThat(p2FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p2FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(1);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    @Test
    public void shouldNotAskFriendshipToUnknownUser() {
        //GIVEN 1 users with no friends nor friendship requests
        //WHEN P1 tries to ask friendship to unknown user
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + UNKNOWN_ID)
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(UserNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
    }
    
    //
    // acceptFriendshipRequest
    //
    
    /** Ask 2 friendship requests and 1 target accept it. */
    @Test
    public void shouldAcceptPartOfFriendship() throws Exception {
        //GIVEN 3 users with no friends nor friendship requests. P1 asked friendship to P2 and P3
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        User p3 = userService.readOne(userId(3));
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(3))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        
        //WHEN P2 accepts
        whenP2().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_OK);
        
        //THEN P1 has 1 friend and 1 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).hasSize(1);
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).hasSize(1);
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(3);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_ACCEPTED_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(2)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 has 1 friend and 0 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).hasSize(1);
        assertThat(p2Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p2Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(2);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_ACCEPTED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p2FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        
        //THEN P3 has 0 friend and 1 remaining request
        List<Friendship> p3Friends = friendshipRepository.findAllFor(userId(3));
        assertThat(p3Friends).isEmpty();
        
        List<FriendshipRequest> p3FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(3));
        assertThat(p3FriendshipRequests).hasSize(1);
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipEvent> p3FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(3));
        assertThat(p3FriendshipEvents).hasSize(1);
        assertThat(p3FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    /** Ask 2 friendship requests and 2 targets accept it. */
    @Test
    public void shouldAcceptAllFriendship() throws Exception {
        //GIVEN 3 users with no friends nor friendship requests. P1 asked friendship to P2 and P3
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        User p3 = userService.readOne(userId(3));
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(3))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP2().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        
        //WHEN P3 accepts
        whenP3().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_OK);
        
        //THEN P1 has 2 friends and 0 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).hasSize(2);
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1), userId(1));
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3), userId(2));
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(4);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_ACCEPTED_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_ACCEPTED_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(2)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(3)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 still has 1 friend and 0 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).hasSize(1);
        assertThat(p2Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p2Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(2);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_ACCEPTED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p2FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        
        //THEN P3 has 1 friend and 0 remaining request
        List<Friendship> p3Friends = friendshipRepository.findAllFor(userId(3));
        assertThat(p3Friends).hasSize(1);
        assertThat(p3Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p3Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipRequest> p3FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(3));
        assertThat(p3FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p3FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(3));
        assertThat(p3FriendshipEvents).hasSize(2);
        assertThat(p3FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_ACCEPTED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p3FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    @Test
    public void shouldNotAcceptSelfFriendshipRequest() throws Exception {
        //GIVEN 2 users. P1 asks friendship to P2
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        
        //WHEN P1 tries to accept his own friendship request
        whenP1().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).isEmpty();
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).hasSize(1);
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(2));
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(1);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN nothing changed for P2
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).isEmpty();
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(2));
        assertThat(p2FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(1);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    @Test
    public void shouldNotAcceptUnknownFriendshipRequest() {
        //GIVEN 2 users with no friends nor friendship requests
        //WHEN P1 tries to accept an unknown friendship request
        whenP1().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + userId(2))
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
        
        //THEN nothing changed for P2
        shouldHaveNoFriendNorFriendshipRequestNorEvent(2);
    }
    
    @Test
    public void shouldNotAcceptUnknownUserFriendshipRequest() {
        //GIVEN 1 user with no friends nor friendship requests
        //WHEN P1 tries to accept an unknown user friendship request
        whenP1().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + UNKNOWN_ID)
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
    }
    
    //
    // rejectFriendshipRequest
    //
    
    /** Ask many friendship requests and targets reject them. */
    @Test
    public void shouldRejectFriendship() throws Exception {
        //GIVEN 3 users with no friends nor friendship requests. P1 asked friendship to P2 and P3
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        User p3 = userService.readOne(userId(3));
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(3))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        
        //WHEN P2 rejects P1 friendship request
        whenP2().getRequestSpecification()
            .post(API_USER + "/rejectfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_OK);
        
        //THEN P1 has 0 friend and 1 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).isEmpty();
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).hasSize(1);
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(3);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_REJECTED_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(2)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 has 0 friend and 0 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).isEmpty();
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(2);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_REJECTED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p2FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        
        //THEN P3 has 0 friend and 1 remaining request
        List<Friendship> p3Friends = friendshipRepository.findAllFor(userId(3));
        assertThat(p3Friends).isEmpty();
        
        List<FriendshipRequest> p3FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(3));
        assertThat(p3FriendshipRequests).hasSize(1);
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipEvent> p3FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(3));
        assertThat(p3FriendshipEvents).hasSize(1);
        assertThat(p3FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    @Test
    public void shouldNotRejectUnknownFriendshipRequest() {
        //GIVEN 2 users with no friends nor friendship requests
        //WHEN P1 tries to reject an unknown friendship request
        whenP1().getRequestSpecification()
            .post(API_USER + "/rejectfriendship/user/" + userId(2))
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
        
        //THEN nothing changed for P2
        shouldHaveNoFriendNorFriendshipRequestNorEvent(2);
    }
    
    @Test
    public void shouldNotRejectUnknownUserFriendshipRequest() {
        //GIVEN 1 user with no friends nor friendship requests
        //WHEN P1 tries to reject an unknown user friendship request
        whenP1().getRequestSpecification()
            .post(API_USER + "/rejectfriendship/user/" + UNKNOWN_ID)
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
    }
    
    //
    // cancelFriendshipRequest
    //
    
    /** Create friendship requests, then cancel one of them. */
    @Test
    public void shouldCancelFriendshipRequest() throws Exception {
        //GIVEN 3 users with no friends nor friendship requests. P1 asked friendship to P2 and P3
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        User p3 = userService.readOne(userId(3));
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(3))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        
        //WHEN P1 cancels friendship request to P2
        whenP1().getRequestSpecification()
            .post(API_USER + "/cancelfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        
        //THEN P1 has 0 friend and 1 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).isEmpty();
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).hasSize(1);
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(3);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_CANCELED_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(2)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 has 0 friend and 0 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).isEmpty();
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(2));
        assertThat(p2FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(2);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_CANCELED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p2FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        
        //THEN P3 has 0 friend and 1 remaining request
        List<Friendship> p3Friends = friendshipRepository.findAllFor(userId(3));
        assertThat(p3Friends).isEmpty();
        
        List<FriendshipRequest> p3FriendshipRequests = friendshipRequestRepository.findAllByRequestTo(userId(3));
        assertThat(p3FriendshipRequests).hasSize(1);
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p3FriendshipRequests).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipEvent> p3FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(3));
        assertThat(p3FriendshipEvents).hasSize(1);
        assertThat(p3FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    @Test
    public void shouldNotCancelUnknownFriendshipRequest() {
        //GIVEN 2 users with no friends nor friendship requests
        //WHEN P1 tries to cancel an unknown friendship request
        whenP1().getRequestSpecification()
            .post(API_USER + "/cancelfriendship/user/" + userId(2))
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
        
        //THEN nothing changed for P2
        shouldHaveNoFriendNorFriendshipRequestNorEvent(2);
    }
    
    @Test
    public void shouldNotCancelUnknownUserFriendshipRequest() {
        //GIVEN 1 user with no friends nor friendship requests
        //WHEN P1 tries to cancel an unknown user friendship request
        whenP1().getRequestSpecification()
            .post(API_USER + "/cancelfriendship/user/" + UNKNOWN_ID)
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipRequestNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
    }
    
    //
    // revokeFriendship
    //
    
    /** Create friendship requests, accept them, then revoke one of them. */
    @Test
    public void shouldRevokeFriendship() throws Exception {
        //GIVEN 3 users. P1 is friend with P2 and P3
        User p1 = userService.readOne(userId(1));
        User p2 = userService.readOne(userId(2));
        User p3 = userService.readOne(userId(3));
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP1().getRequestSpecification()
            .post(API_USER + "/askfriendship/user/" + userId(3))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP2().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        whenP3().getRequestSpecification()
            .post(API_USER + "/acceptfriendship/user/" + userId(1))
            .then()
            .statusCode(SC_OK);
        TestTools.temporize();
        
        //WHEN P1 revokes friendship with P2
        whenP1().getRequestSpecification()
            .post(API_USER + "/revokefriendship/user/" + userId(2))
            .then()
            .statusCode(SC_OK);
        
        //THEN P1 has 1 friend and 0 remaining request
        List<Friendship> p1Friends = friendshipRepository.findAllFor(userId(1));
        assertThat(p1Friends).hasSize(1);
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p1Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipRequest> p1FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(1));
        assertThat(p1FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p1FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(1));
        assertThat(p1FriendshipEvents).hasSize(5);
        assertThat(p1FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_REVOKED_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_ACCEPTED_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(2)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_ACCEPTED_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(3)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p3).build(), "code", "friend");
        assertThat(p1FriendshipEvents.get(4)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_SENT_FRIEND_REQUEST).friend(p2).build(), "code", "friend");
        
        //THEN P2 has 1 friend and 0 remaining request
        List<Friendship> p2Friends = friendshipRepository.findAllFor(userId(2));
        assertThat(p2Friends).isEmpty();
        
        List<FriendshipRequest> p2FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(2));
        assertThat(p2FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p2FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(2));
        assertThat(p2FriendshipEvents).hasSize(3);
        assertThat(p2FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_REVOKED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p2FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_ACCEPTED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p2FriendshipEvents.get(2)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        
        //THEN P3 has 1 friend and 0 remaining request
        List<Friendship> p3Friends = friendshipRepository.findAllFor(userId(3));
        assertThat(p3Friends).hasSize(1);
        assertThat(p3Friends).extracting(friendshipRequest -> friendshipRequest.getRequestFrom().getId()).contains(userId(1));
        assertThat(p3Friends).extracting(friendshipRequest -> friendshipRequest.getRequestTo().getId()).contains(userId(3));
        
        List<FriendshipRequest> p3FriendshipRequests = friendshipRequestRepository.findAllByRequestFrom(userId(3));
        assertThat(p3FriendshipRequests).isEmpty();
        
        List<FriendshipEvent> p3FriendshipEvents = friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(3));
        assertThat(p3FriendshipEvents).hasSize(2);
        assertThat(p3FriendshipEvents.get(0)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(YOU_ACCEPTED_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
        assertThat(p3FriendshipEvents.get(1)).isEqualToComparingOnlyGivenFields(
            FriendshipEvent.builder().code(TARGET_SENT_FRIEND_REQUEST).friend(p1).build(), "code", "friend");
    }
    
    @Test
    public void shouldNotRevokeUnknownFriendshipRequest() {
        //GIVEN 2 users with no friends nor friendship requests
        //WHEN P1 tries to revoke an unknown friendship
        whenP1().getRequestSpecification()
            .post(API_USER + "/revokefriendship/user/" + userId(2))
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
        
        //THEN nothing changed for P2
        shouldHaveNoFriendNorFriendshipRequestNorEvent(2);
    }
    
    @Test
    public void shouldNotRevokeUnknownUserFriendshipRequest() {
        //GIVEN 1 user with no friends nor friendship requests
        //WHEN P1 tries to revoke an unknown user friendship
        whenP1().getRequestSpecification()
            .post(API_USER + "/revokefriendship/user/" + UNKNOWN_ID)
            .then().statusCode(SC_NOT_FOUND)
            .body(MANAGED_ERROR_TYPE, Matchers.equalTo(FriendshipNotFoundException.class.getSimpleName()));
        
        //THEN nothing changed for P1
        shouldHaveNoFriendNorFriendshipRequestNorEvent(1);
    }
    
    //
    // others
    //
    
    /** Only {@link FriendshipEvent.Validation#MAX_EVENTS_PER_USER} most recent user's friendshipEvents should be kept. */
    @Test
    public void shouldCheckEventsMaintenance() {
        //GIVEN two users with no event
        assertThat(friendshipEventRepository.countAllByUser(userId(1))).isEqualTo(0);
        assertThat(friendshipEventRepository.countAllByUser(userId(2))).isEqualTo(0);
        
        //WHEN P1 generates more than MAX_EVENTS_PER_USER events for P1 and P2
        for (int i = 0; i < FriendshipEvent.Validation.MAX_EVENTS_PER_USER + 5; i++) {
            whenP1().getRequestSpecification()
                .post(API_USER + "/askfriendship/user/" + userId(2))
                .then()
                .statusCode(SC_OK);
            TestTools.temporize();
            whenP1().getRequestSpecification()
                .post(API_USER + "/cancelfriendship/user/" + userId(2))
                .then()
                .statusCode(SC_OK);
            TestTools.temporize();
        }
        
        //THEN only last MAX_EVENTS_PER_USER events stay in database for each user
        assertThat(friendshipEventRepository.countAllByUser(userId(1))).isEqualTo(FriendshipEvent.Validation.MAX_EVENTS_PER_USER);
        assertThat(friendshipEventRepository.countAllByUser(userId(2))).isEqualTo(FriendshipEvent.Validation.MAX_EVENTS_PER_USER);
    }
    
    @Test
    public void shouldGetFriends() throws Exception {
        //GIVEN a P1 user with 2 friends P2 and P3: P1 initiated friendship with P2 and P3
        friendshipService.askFriendship(userId(1), userId(2));
        TestTools.temporize();
        friendshipService.askFriendship(userId(1), userId(3));
        TestTools.temporize();
        friendshipService.acceptFriendshipRequest(userId(1), userId(2));
        TestTools.temporize();
        friendshipService.acceptFriendshipRequest(userId(1), userId(3));
        UserPublicInfo upi1 = UserPublicInfo.from(userService.readOne(userId(1)));
        UserPublicInfo upi2 = UserPublicInfo.from(userService.readOne(userId(2)));
        UserPublicInfo upi3 = UserPublicInfo.from(userService.readOne(userId(3)));
        
        //WHEN P1 gets friends
        Response p1Res = whenP1().getRequestSpecification()
            .get(API_USER + "/friends");
        p1Res.then()
            .statusCode(SC_OK);
        List<UserPublicInfo> p1Friends = Arrays.asList(p1Res.getBody().as(UserPublicInfo[].class));
        
        //THEN P1 has friends
        assertThat(p1Friends)
            .hasSize(2)
            .containsExactly(upi3, upi2);
        
        //WHEN P2 gets friends
        Response p2Res = whenP2().getRequestSpecification()
            .get(API_USER + "/friends");
        p2Res.then()
            .statusCode(SC_OK);
        List<UserPublicInfo> p2Friends = Arrays.asList(p2Res.getBody().as(UserPublicInfo[].class));
        
        //THEN P2 has a friend
        assertThat(p2Friends)
            .hasSize(1)
            .containsExactly(upi1);
        
        //WHEN P3 get friends
        Response p3Res = whenP3().getRequestSpecification()
            .get(API_USER + "/friends");
        p3Res.then()
            .statusCode(SC_OK);
        List<UserPublicInfo> p3Friends = Arrays.asList(p3Res.getBody().as(UserPublicInfo[].class));
        
        //THEN P3 has a friend
        assertThat(p3Friends)
            .hasSize(1)
            .containsExactly(upi1);
    }
    
    @Test
    public void shouldGetFriendsMixed() throws Exception {
        //GIVEN a P1 user with 2 friends P2 and P3: P2 and P3 initiated friendship with P1
        friendshipService.askFriendship(userId(2), userId(1));
        TestTools.temporize();
        friendshipService.askFriendship(userId(3), userId(1));
        TestTools.temporize();
        friendshipService.acceptFriendshipRequest(userId(2), userId(1));
        TestTools.temporize();
        friendshipService.acceptFriendshipRequest(userId(3), userId(1));
        UserPublicInfo upi1 = UserPublicInfo.from(userService.readOne(userId(1)));
        UserPublicInfo upi2 = UserPublicInfo.from(userService.readOne(userId(2)));
        UserPublicInfo upi3 = UserPublicInfo.from(userService.readOne(userId(3)));
        
        //WHEN P1 gets friends
        Response p1Res = whenP1().getRequestSpecification()
            .get(API_USER + "/friends");
        p1Res.then()
            .statusCode(SC_OK);
        List<UserPublicInfo> p1Friends = Arrays.asList(p1Res.getBody().as(UserPublicInfo[].class));
        
        //THEN P1 has friends
        assertThat(p1Friends)
            .hasSize(2)
            .containsExactly(upi3, upi2);
        
        //WHEN P2 gets friends
        Response p2Res = whenP2().getRequestSpecification()
            .get(API_USER + "/friends");
        p2Res.then()
            .statusCode(SC_OK);
        List<UserPublicInfo> p2Friends = Arrays.asList(p2Res.getBody().as(UserPublicInfo[].class));
        
        //THEN P2 has a friend
        assertThat(p2Friends)
            .hasSize(1)
            .containsExactly(upi1);
        
        //WHEN P3 get friends
        Response p3Res = whenP3().getRequestSpecification()
            .get(API_USER + "/friends");
        p3Res.then()
            .statusCode(SC_OK);
        List<UserPublicInfo> p3Friends = Arrays.asList(p3Res.getBody().as(UserPublicInfo[].class));
        
        //THEN P3 has a friend
        assertThat(p3Friends)
            .hasSize(1)
            .containsExactly(upi1);
    }
    
    private void shouldHaveNoFriendNorFriendshipRequestNorEvent(int humanId) {
        assertThat(friendshipRepository.findAllFor(userId(humanId))).isEmpty();
        assertThat(friendshipRequestRepository.findAllByRequestFrom(userId(humanId))).isEmpty();
        assertThat(friendshipEventRepository.findAllByUserOrderByCreationDateDesc(userId(humanId))).isEmpty();
    }
}
