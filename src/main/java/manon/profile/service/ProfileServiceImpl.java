package manon.profile.service;

import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import manon.profile.ProfileNotFoundException;
import manon.profile.ProfileUpdateForm;
import manon.profile.document.Profile;
import manon.profile.friendship.FriendshipExistsException;
import manon.profile.friendship.FriendshipRequestExistsException;
import manon.profile.friendship.FriendshipRequestNotFoundException;
import manon.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static manon.profile.document.Profile.Validation.MAX_EVENTS;

@Service("ProfileService")
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    
    private final ProfileRepository profileRepository;
    
    @Override
    public long count() {
        return profileRepository.count();
    }
    
    @Override
    public void save(Profile profile) {
        profileRepository.save(profile);
    }
    
    @Override
    public void ensureExist(String... ids) throws ProfileNotFoundException {
        for (String id : ids) {
            readOne(id);
        }
    }
    
    @Override
    public Profile readOne(String id) throws ProfileNotFoundException {
        return profileRepository.findById(id).orElseThrow(() -> new ProfileNotFoundException(id));
    }
    
    @Override
    public Profile create() {
        Profile profile = Profile.builder().build();
        profileRepository.save(profile);
        return profile;
    }
    
    @Override
    public void update(String profileId, ProfileUpdateForm profileUpdateForm)
            throws ProfileNotFoundException, DuplicateKeyException {
        profileRepository.updateField(profileId, profileUpdateForm.getField(), profileUpdateForm.getValue());
    }
    
    @Override
    public void askFriendship(String profileIdFrom, String profileIdTo)
            throws ProfileNotFoundException, FriendshipExistsException, FriendshipRequestExistsException {
        Profile from = readOne(profileIdFrom);
        if (from.getFriends().contains(profileIdTo)) {
            throw new FriendshipExistsException(profileIdFrom, profileIdTo);
        }
        if (from.getFriendshipRequestsFrom().contains(profileIdTo)) {
            throw new FriendshipRequestExistsException(profileIdTo, profileIdFrom);
        }
        if (from.getFriendshipRequestsTo().contains(profileIdTo)) {
            throw new FriendshipRequestExistsException(profileIdFrom, profileIdTo);
        }
        profileRepository.askFriendship(profileIdFrom, profileIdTo);
        keepEvents(profileIdFrom);
        keepEvents(profileIdTo);
    }
    
    @Override
    public void acceptFriendshipRequest(String profileIdFrom, String profileIdTo)
            throws ProfileNotFoundException, FriendshipRequestNotFoundException {
        if (!readOne(profileIdTo).getFriendshipRequestsFrom().contains(profileIdFrom)) {
            throw new FriendshipRequestNotFoundException(profileIdFrom, profileIdTo);
        }
        profileRepository.acceptFriendshipRequest(profileIdFrom, profileIdTo);
        keepEvents(profileIdFrom);
        keepEvents(profileIdTo);
    }
    
    @Override
    public void rejectFriendshipRequest(String profileIdFrom, String profileIdTo)
            throws ProfileNotFoundException {
        profileRepository.rejectFriendshipRequest(profileIdFrom, profileIdTo);
        keepEvents(profileIdFrom);
        keepEvents(profileIdTo);
    }
    
    @Override
    public void cancelFriendshipRequest(String profileIdFrom, String profileIdTo)
            throws ProfileNotFoundException {
        profileRepository.cancelFriendshipRequest(profileIdFrom, profileIdTo);
        keepEvents(profileIdFrom);
        keepEvents(profileIdTo);
    }
    
    @Override
    public void revokeFriendship(String profileIdFrom, String profileIdTo)
            throws ProfileNotFoundException {
        profileRepository.revokeFriendship(profileIdFrom, profileIdTo);
        keepEvents(profileIdFrom);
        keepEvents(profileIdTo);
    }
    
    @Override
    public void keepEvents(String id) throws ProfileNotFoundException {
        profileRepository.keepEvents(id, MAX_EVENTS);
    }
    
    @Override
    public long getSkill(String id) {
        return profileRepository.getSkill(id);
    }
    
    @Override
    public long sumSkill(Collection<String> ids) {
        return profileRepository.sumSkill(ids);
    }
}
