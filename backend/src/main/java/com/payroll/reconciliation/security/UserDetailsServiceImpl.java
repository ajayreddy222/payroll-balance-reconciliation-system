package com.payroll.reconciliation.security;

import com.payroll.reconciliation.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of Spring Security's {@link UserDetailsService}.
 * Loads user details from the database by email address for authentication.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AppUserRepository users;

    /**
     * Constructs the service with the required user repository.
     *
     * @param users the user repository
     */
    public UserDetailsServiceImpl(AppUserRepository users) {
        this.users = users;
    }

    /**
     * Loads a user by their email address for Spring Security authentication.
     *
     * @param username the email address to look up
     * @return the Spring Security UserDetails object
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.findByEmail(username)
                .map(user -> new User(user.getEmail(), user.getPasswordHash(), List.of(new SimpleGrantedAuthority(user.getRole().name()))))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
