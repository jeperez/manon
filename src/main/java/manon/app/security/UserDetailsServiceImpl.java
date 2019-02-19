package manon.app.security;

import lombok.RequiredArgsConstructor;
import manon.user.document.User;
import manon.user.err.UserNotFoundException;
import manon.user.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static manon.user.model.RegistrationState.ACTIVE;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserService userService;
    
    @Override
    public UserSimpleDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        User user;
        try {
            user = userService.readByUsername(username);
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException(username);
        }
        
        return new UserSimpleDetails(
            user.getUsername(),
            user.getPassword(),
            true, true, true,
            ACTIVE == user.getRegistrationState(),
            Stream.of(user.getAuthorities().split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toSet()),
            user);
    }
}