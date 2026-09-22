package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The generated {@code *Api} interfaces' method signatures are fixed by the OpenAPI
 * contract (no room for an extra {@code @AuthenticationPrincipal} parameter without
 * breaking {@code @Override}), and the Thymeleaf web controllers have the same shape
 * of need, so both pull "who's logged in" from the security context via this helper
 * instead of a controller-method parameter.
 */
public final class CurrentUserSupport {

    private CurrentUserSupport() {
    }

    public static UserAccount currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppPrincipal appPrincipal)) {
            throw new AccessDeniedException("Authentication required");
        }
        return appPrincipal.getUserAccount();
    }

    public static UserAccount currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppPrincipal appPrincipal) {
            return appPrincipal.getUserAccount();
        }
        return null;
    }
}
