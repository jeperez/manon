package manon.profile.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import manon.app.security.PasswordEncoderService;
import manon.app.security.UserSimpleDetails;
import manon.profile.ProfileNotFoundException;
import manon.profile.ProfileUpdateForm;
import manon.profile.ProfileUpdateFormException;
import manon.profile.ProfileUpdateValidator;
import manon.profile.document.Profile;
import manon.profile.service.ProfileService;
import manon.user.UserExistsException;
import manon.user.UserNotFoundException;
import manon.user.UserPasswordUpdateForm;
import manon.user.UserPasswordUpdateFormException;
import manon.user.UserPasswordUpdateValidator;
import manon.user.document.User;
import manon.user.registration.RegistrationForm;
import manon.user.registration.RegistrationFormException;
import manon.user.registration.RegistrationValidator;
import manon.user.registration.service.RegistrationService;
import manon.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static manon.app.config.API.API_PROFILE;
import static manon.app.config.API.API_V1;
import static manon.util.Tools.MEDIA_JSON;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/** Profile API. */
@RestController
@RequestMapping(value = API_V1 + API_PROFILE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ProfileWS {
    
    private final RegistrationService registrationService;
    private final ProfileService profileService;
    private final UserService userService;
    private final PasswordEncoderService passwordEncoderService;
    
    /** Register a new profile.
     * Create {@link User} and associated {@link Profile}. */
    @RequestMapping(method = POST, consumes = MEDIA_JSON, produces = MEDIA_JSON)
    // TODO move to UserWS API since it works at user level
    @ResponseStatus(CREATED)
    public User register(@RequestBody RegistrationForm registrationForm, BindingResult bindingResult)
            throws UserExistsException, RegistrationFormException, ProfileNotFoundException {
        log.info("user registration with {}", registrationForm);
        ValidationUtils.invokeValidator(new RegistrationValidator(), registrationForm, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new RegistrationFormException(bindingResult.getAllErrors());
        }
        return registrationService.registerPlayer(registrationForm.getUsername(), registrationForm.getPassword());
    }
    
    /** Unregister a profile. */
    @RequestMapping(method = DELETE) // TODO move to UserWS API since it works at user level
    public void delete(@AuthenticationPrincipal UserSimpleDetails user)
            throws UserNotFoundException {
        log.info("user {} deletes himself", user.getIdentity());
        registrationService.delete(user.getUserId());
    }
    
    /** Get user's profile. */
    @RequestMapping(method = GET)
    public Profile read(@AuthenticationPrincipal UserSimpleDetails user)
            throws ProfileNotFoundException {
        log.info("user {} reads his profile", user.getIdentity());
        return profileService.readOne(user.getProfileId());
    }
    
    /** Update one user's profile field. */
    @RequestMapping(value = "/field", method = PUT, consumes = MEDIA_JSON)
    public void updateField(@AuthenticationPrincipal UserSimpleDetails user,
                            @RequestBody ProfileUpdateForm profileUpdateForm,
                            BindingResult bindingResult)
            throws ProfileNotFoundException, ProfileUpdateFormException {
        log.info("user {} updates his profile with {}", user.getIdentity(), profileUpdateForm);
        ValidationUtils.invokeValidator(new ProfileUpdateValidator(), profileUpdateForm, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ProfileUpdateFormException(bindingResult.getAllErrors());
        }
        profileService.update(user.getProfileId(), profileUpdateForm);
    }
    
    /** Update current user's password. */
    @RequestMapping(value = "/password", method = PUT, consumes = MEDIA_JSON)
    // TODO move to UserWS API since it works at user level
    public void updatePassword(@AuthenticationPrincipal UserSimpleDetails user,
                               @RequestBody UserPasswordUpdateForm userPasswordUpdateForm,
                               BindingResult bindingResult)
            throws UserNotFoundException, UserPasswordUpdateFormException {
        log.info("user {} updates his password", user.getIdentity());
        ValidationUtils.invokeValidator(new UserPasswordUpdateValidator(), userPasswordUpdateForm, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new UserPasswordUpdateFormException(bindingResult.getAllErrors());
        }
        userService.setPassword(user.getUser().getId(),
                passwordEncoderService.encode(userPasswordUpdateForm.getNewPassword()));
    }
}
