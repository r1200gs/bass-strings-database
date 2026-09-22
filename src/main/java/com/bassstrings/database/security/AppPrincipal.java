package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;

/**
 * Implemented by both {@link AppUserPrincipal} (plain OAuth2 providers, e.g. Facebook)
 * and {@link AppOidcUserPrincipal} (OIDC providers, e.g. Google and Apple) so that
 * controllers and services can resolve "who's logged in" without caring which kind of
 * OAuth2 flow authenticated them:
 *
 * <pre>{@code
 * @GetMapping("/me")
 * public String me(@AuthenticationPrincipal AppPrincipal principal) {
 *     UserAccount account = principal.getUserAccount();
 *     ...
 * }
 * }</pre>
 */
public interface AppPrincipal {

    UserAccount getUserAccount();
}
