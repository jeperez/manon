package manon.user.err;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("serial")
@AllArgsConstructor
@Getter
public class PasswordNotMatchException extends Exception {
}
