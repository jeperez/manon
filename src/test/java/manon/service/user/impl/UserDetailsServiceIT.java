package manon.service.user.impl;

import manon.err.user.UserNotFoundException;
import manon.model.user.UserSimpleDetails;
import manon.util.basetest.AbstractIT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDetailsServiceIT extends AbstractIT {

    @Override
    public int getNumberOfUsers() {
        return 1;
    }

    @Test
    void shouldLoadUserByUsername() {
        String username = name(1);
        UserSimpleDetails userSimpleDetailsFound = ((UserDetailsServiceImpl) userDetailsService).loadUserByUsername(username);
        assertThat(userSimpleDetailsFound).isNotNull();
        assertThat(userSimpleDetailsFound.getUsername()).isEqualTo(username);
        assertThat(userSimpleDetailsFound.getUser().getUsername()).isEqualTo(username);
    }

    @Test
    void shouldFailLoadUserByUnknownUsername() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(UNKNOWN_USER_NAME))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldFailLoadUserByNullUsername() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
            .isInstanceOf(UserNotFoundException.class);
    }
}
