package manon.user.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import manon.app.security.UserSimpleDetails;
import manon.user.err.FriendshipExistsException;
import manon.user.err.FriendshipRequestExistsException;
import manon.user.err.FriendshipRequestNotFoundException;
import manon.user.err.UserNotFoundException;
import manon.user.service.FriendshipService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static manon.app.config.API.API_USER;

@RestController
@RequestMapping(value = API_USER)
@RequiredArgsConstructor
@Slf4j
public class FriendshipWS {
    
    private final FriendshipService friendshipService;
    
    /** Create a friendship request to another user. */
    @PostMapping(value = "/askfriendship/user/{id}")
    public void askFriendship(@AuthenticationPrincipal UserSimpleDetails user,
                              @PathVariable("id") String id)
            throws UserNotFoundException, FriendshipExistsException, FriendshipRequestExistsException {
        log.debug("user {} asks friendship request to user {}", user.getIdentity(), id);
        friendshipService.askFriendship(user.getUser().getId(), id);
    }
    
    /** Accept a friendship request from another user. */
    @PostMapping(value = "/acceptfriendship/user/{id}")
    public void acceptFriendshipRequest(@AuthenticationPrincipal UserSimpleDetails user,
                                        @PathVariable("id") String id)
            throws UserNotFoundException, FriendshipRequestNotFoundException {
        log.debug("user {} accepts friendship request from user {}", user.getIdentity(), id);
        friendshipService.acceptFriendshipRequest(id, user.getUser().getId());
    }
    
    /** Reject a friendship request from another user. */
    @PostMapping(value = "/rejectfriendship/user/{id}")
    public void rejectFriendshipRequest(@AuthenticationPrincipal UserSimpleDetails user,
                                        @PathVariable("id") String id) {
        log.debug("user {} rejects friendship request from user {}", user.getIdentity(), id);
        friendshipService.rejectFriendshipRequest(id, user.getUser().getId());
    }
    
    /** Cancel a friendship request to another user. */
    @PostMapping(value = "/cancelfriendship/user/{id}")
    public void cancelFriendshipRequest(@AuthenticationPrincipal UserSimpleDetails user,
                                        @PathVariable("id") String id) {
        log.debug("user {} cancels friendship request to user {}", user.getIdentity(), id);
        friendshipService.cancelFriendshipRequest(user.getUser().getId(), id);
    }
    
    /** Delete an existing friendship relation with another user. */
    @PostMapping(value = "/revokefriendship/user/{id}")
    public void revokeFriendship(@AuthenticationPrincipal UserSimpleDetails user,
                                 @PathVariable("id") String id) {
        log.debug("user {} deletes friendship with user {}", user.getIdentity(), id);
        friendshipService.revokeFriendship(user.getUser().getId(), id);
    }
}
