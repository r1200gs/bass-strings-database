package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Handles the plain (non-OIDC) OAuth2 login flow, used here by Facebook. Google and
 * Apple are OIDC providers and go through {@link AccountProvisioningOidcUserService}
 * instead - see the comment on {@code SecurityConfig.oauth2Login} for why both hooks
 * are wired up side by side.
 */
@Service
@RequiredArgsConstructor
public class AccountProvisioningOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    private final UserAccountProvisioner provisioner;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = extractFacebookPictureUrl(attributes);

        UserAccount account = provisioner.provision(registrationId, providerId, email, name, avatarUrl);
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new AppUserPrincipal(account, attributes, provisioner.authoritiesFor(account), userNameAttributeName);
    }

    /** Facebook's Graph API nests the avatar as {@code picture.data.url}. */
    @SuppressWarnings("unchecked")
    private String extractFacebookPictureUrl(Map<String, Object> attributes) {
        Object picture = attributes.get("picture");
        if (picture instanceof Map<?, ?> pictureMap) {
            Object data = pictureMap.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object url = dataMap.get("url");
                return url != null ? url.toString() : null;
            }
        }
        return null;
    }
}
