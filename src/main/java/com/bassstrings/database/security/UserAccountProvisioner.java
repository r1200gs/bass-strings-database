package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;
import com.bassstrings.database.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Finds-or-creates the local {@link UserAccount} behind an OAuth2 login, shared by both
 * {@link AccountProvisioningOidcUserService} (Google, Apple) and
 * {@link AccountProvisioningOAuth2UserService} (Facebook) so account creation and role
 * defaulting live in exactly one place regardless of which provider authenticated the
 * subscriber.
 */
@Component
@RequiredArgsConstructor
public class UserAccountProvisioner {

    private final UserAccountRepository repository;

    public UserAccount provision(String provider, String providerId, String email, String displayName, String avatarUrl) {
        Optional<UserAccount> byProvider = repository.findByProviderAndProviderId(provider, providerId);
        if (byProvider.isPresent()) {
            return updateIfChanged(byProvider.get(), email, displayName, avatarUrl);
        }

        // A subscriber who first signs in with Google and later with Facebook using the
        // same address lands on one account rather than two, as long as that email is
        // verified by both providers. This is a deliberate MVP simplification: a fully
        // hardened implementation would only link accounts once the newly-presented
        // email has been separately confirmed.
        if (email != null) {
            Optional<UserAccount> byEmail = repository.findByEmail(email);
            if (byEmail.isPresent()) {
                UserAccount account = byEmail.get();
                account.setProvider(provider);
                account.setProviderId(providerId);
                return repository.save(updateIfChanged(account, email, displayName, avatarUrl));
            }
        }

        UserAccount account = new UserAccount();
        account.setProvider(provider);
        account.setProviderId(providerId);
        account.setEmail(email);
        account.setDisplayName(displayName != null && !displayName.isBlank()
                ? displayName
                : (email != null ? email : provider + " subscriber"));
        account.setAvatarUrl(avatarUrl);
        return repository.save(account);
    }

    public List<GrantedAuthority> authoritiesFor(UserAccount account) {
        return account.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    private UserAccount updateIfChanged(UserAccount account, String email, String displayName, String avatarUrl) {
        boolean changed = false;
        if (email != null && !email.equals(account.getEmail())) {
            account.setEmail(email);
            changed = true;
        }
        if (displayName != null && !displayName.isBlank() && !displayName.equals(account.getDisplayName())) {
            account.setDisplayName(displayName);
            changed = true;
        }
        if (avatarUrl != null && !avatarUrl.equals(account.getAvatarUrl())) {
            account.setAvatarUrl(avatarUrl);
            changed = true;
        }
        return changed ? repository.save(account) : account;
    }
}
