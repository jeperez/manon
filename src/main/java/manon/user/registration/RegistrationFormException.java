package manon.user.registration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.ObjectError;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RegistrationFormException extends Exception {
    
    private List<ObjectError> errors;
}
