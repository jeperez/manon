package manon.user.registration;

import manon.user.document.User;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static manon.util.Tools.isBlank;

public class RegistrationValidator implements Validator {
    
    @Override
    public boolean supports(Class<?> clazz) {
        return RegistrationForm.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        RegistrationForm user = (RegistrationForm) target;
        
        String username = user.getUsername();
        if (isBlank(username)) {
            errors.reject("USERNAME_EMPTY");
        } else if (username.length() < User.Validation.USERNAME_MIN_LENGTH) {
            errors.reject("USERNAME_TOO_SHORT");
        } else if (username.length() > User.Validation.USERNAME_MAX_LENGTH) {
            errors.reject("USERNAME_TOO_LONG");
        } else if (username.contains("<") || username.contains(">")) {
            errors.reject("USERNAME_FORBIDDEN_CHARACTERS");
        } else if (!User.Validation.USERNAME_PATTERN.matcher(username).find()) {
            errors.reject("USERNAME_BAD_FORMAT");
        }
        
        String password = user.getPassword();
        if (isBlank(password)) {
            errors.reject("PASSWORD_EMPTY");
        } else if (password.length() < User.Validation.PASSWORD_MIN_LENGTH) {
            errors.reject("PASSWORD_TOO_SHORT");
        } else if (password.length() > User.Validation.PASSWORD_MAX_LENGTH) {
            errors.reject("PASSWORD_TOO_LONG");
        }
    }
}
