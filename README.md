# Bass Strings Database

A subscription community for bass players: browse a catalog of bass guitar models and
bass string products, post experience reviews rated by playing style and musical genre,
comment, and find strings that suit how you actually play.

- **Backend:** Spring Boot 4.1 (Java 25), MongoDB (Spring Data), Spring Security with
  OAuth2/OIDC login (Google, Facebook, Apple), role-based authorization (ADMIN / MONITOR
  / USER).
- **API:** contract-first. `openapi/bassstrings-api.yaml` is the source of truth; the
  `openapi-generator-maven-plugin` (configured in `pom.xml`) generates the `*Api`
  interfaces and DTOs into `target/generated-sources/openapi` at build time
  (`interfaceOnly=true`), and hand-written `@RestController` classes under
  `com.bassstrings.hub.api.controller` implement those interfaces.
- **Frontend:** server-rendered Spring MVC + Thymeleaf (`com.bassstrings.hub.web`,
  `src/main/resources/templates`).

## ⚠️ About this build: not yet compiled

This project was generated in a sandboxed environment whose network policy blocks Maven
Central (`repo.maven.apache.org`) entirely, so **`mvn compile` could not be run here** -
neither to download dependencies nor to execute the OpenAPI code generator that the REST
controllers are written against. Everything below was written carefully by hand against
the well-documented, stable behavior of Spring Boot, Spring Security, Spring Data
MongoDB, and the `openapi-generator` "spring" generator, but it has **not been verified
to compile**. The first thing to do on a machine with normal internet access is:

```bash
mvn clean compile
```

If that surfaces a mismatch between a hand-written controller in
`com.bassstrings.hub.api.controller` and the interface generated from
`openapi/bassstrings-api.yaml` (under `target/generated-sources/openapi/...` after the
build), open the generated interface and adjust the controller method's parameter list
to match - this is the normal contract-first workflow and should only need small
tweaks, if any. Please treat this as a solid, complete first draft rather than a
finished, tested deliverable.

## Getting started

### Prerequisites

- JDK 25
- Maven 3.9+
- A MongoDB instance (local, Docker, or Atlas)

### Run MongoDB locally (Docker)

```bash
docker run -d --name bassstrings-mongo -p 27017:27017 mongo:7
```

### Build and run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. The catalog pages (`/`, `/strings`,
`/models`) work immediately with an empty database - you'll just see empty listings
until you add some data (see "Seeding data" below). Signing in requires at least one
OAuth2 provider to be configured (see next section); until then the "Sign in" button
still renders but shows no provider buttons.

### Configuring sign-in providers

Each provider is wired up explicitly in `OAuth2ClientRegistrationConfig` and only
registered (its login button only appears on `/login`) once its client id is set, so you
can configure one, two, or all three.

Set these as environment variables (or in a `.env` file your process manager loads -
`application.yml` reads them via `${VAR_NAME:}`):

| Variable | Provider | Where to get it |
|---|---|---|
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google | [Google Cloud Console](https://console.cloud.google.com/apis/credentials) → OAuth client ID (Web application). Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google` |
| `FACEBOOK_CLIENT_ID`, `FACEBOOK_CLIENT_SECRET` | Facebook | [Meta for Developers](https://developers.facebook.com/) → your app → Facebook Login → Settings. Valid OAuth redirect URI: `http://localhost:8080/login/oauth2/code/facebook` |
| `APPLE_CLIENT_ID` | Apple | The **Services ID** you register under "Sign in with Apple" in the [Apple Developer portal](https://developer.apple.com/account/resources/identifiers/list/serviceId) |
| `APPLE_TEAM_ID` | Apple | Your 10-character Team ID, top-right of the developer portal |
| `APPLE_KEY_ID` | Apple | The Key ID of the "Sign in with Apple" key you create under Keys |
| `APPLE_PRIVATE_KEY_PATH` | Apple | Filesystem path to the downloaded `AuthKey_<keyId>.p8` file |

Apple has no static client secret - `AppleClientSecretGenerator` signs a short-lived
JWT with your `.p8` private key at startup (and re-signs it before it would expire; see
that class's Javadoc). Apple's redirect URI to register is
`http://localhost:8080/login/oauth2/code/apple`, and it must use `https://` in
production (Apple doesn't allow plain `http://` redirect URIs except for `localhost`
during development, so you'll need a real domain + TLS before Apple sign-in works
outside your machine).

In production, use `https://your-domain/login/oauth2/code/{provider}` for all three and
put the secrets in your platform's secret manager rather than plain environment
variables where avoidable.

### Signing in locally with mock-oauth2-server

For local runs and integration tests you don't need real provider credentials:
`docker-compose.yml` starts [mock-oauth2-server](https://github.com/navikt/mock-oauth2-server)
on `http://localhost:8090` and turns on a **Mock login (local only)** button on `/login`.

```bash
mvn clean package            # the Dockerfile copies target/*.jar
docker compose up --build
```

Open `http://localhost:8080/login`, click **Continue with Mock login**, and type any
username (it becomes your account's provider id, so a new name gives you a new user).
The optional claims box takes JSON such as `{"email":"me@example.com","name":"Me"}`.

To run the app from your IDE or `mvn spring-boot:run` instead, start only the
dependencies and point the app at the mock server on localhost:

```bash
docker compose up -d mock-oauth2-server mongodb
MOCK_OAUTH2_ENABLED=true \
SPRING_MONGODB_URI='mongodb://rootuser:rootpass@localhost:27017/myapp?authSource=admin' \
mvn spring-boot:run
```

| Variable | Default | Purpose |
|---|---|---|
| `MOCK_OAUTH2_ENABLED` | `false` | Registers the mock provider. Never set this in production. |
| `MOCK_OAUTH2_ISSUER_URI` | `http://localhost:8090/default` | Issuer the app calls for token, userinfo and JWKS. |
| `MOCK_OAUTH2_AUTHORIZATION_URI` | `<issuer>/authorize` | Where the browser is sent to sign in. Only needed when the app and your browser reach the mock server by different host names, as in docker-compose. |

### Seeding data

There's no admin UI yet (see "What's not built" below) - the fastest way to get a bass
model and a bass string product into the catalog is the REST API, as an ADMIN:

1. Sign in once with any configured provider so your `UserAccount` document exists in
   MongoDB.
2. In the `users` collection, add `"ADMIN"` to your document's `roles` array (there's no
   bootstrap-admin flow yet - this one step is manual).
3. `POST /api/v1/bass-models` and `POST /api/v1/bass-strings` with your session cookie
   and the CSRF token from the `XSRF-TOKEN` cookie (send it back as an `X-XSRF-TOKEN`
   header) - see `openapi/bassstrings-api.yaml` for the request shapes.

### Running tests

```bash
mvn test
```

The included test (`ReviewFormDataTest`) is a plain unit test with no external
dependencies. A full `@SpringBootTest` slice wasn't added because it would need a real
or embedded MongoDB wired into CI - a reasonable next addition once you're building
this for real (Testcontainers' MongoDB module is the usual choice).

## Project layout

```
openapi/bassstrings-api.yaml        Source-of-truth API contract
pom.xml                             Build + openapi-generator-maven-plugin config
src/main/java/com/bassstrings/hub/
  api/controller/                   Hand-written REST controllers implementing the
                                     generated *Api interfaces, + the API exception handler
  config/                           SecurityConfig, MongoDB auditing
  domain/                           MongoDB documents (persistence layer)
  exception/                        ResourceNotFoundException
  mapper/                           Generated-DTO <-> domain-entity conversion
  repository/                       Spring Data MongoDB repositories
  security/                         OAuth2/OIDC wiring, principal types, Apple JWT signing
  service/                          Business logic, authorization checks, rating aggregation
  web/                              Thymeleaf MVC controllers + form-backing beans
src/main/resources/
  application.yml
  templates/                        Thymeleaf pages + fragments/common.html (nav/footer)
  static/css/style.css
```

## Design notes worth knowing before you extend this

- **Enums are shared, not duplicated.** `Role`, `PlayingStyle`, `MusicGenre`,
  `StringMaterial`, `StringConstruction`, `ScaleLength`, `ReviewStatus`, and
  `CommentStatus` are generated once from the OpenAPI spec and reused directly as the
  MongoDB-persisted enum types on the domain entities, rather than defining a parallel
  domain enum plus a mapper for each. Every other domain type (`UserAccount`,
  `BassGuitarModel`, `BassStringProduct`, `StringReview`, `ReviewComment`) is its own
  persistence class, independent of the generated DTOs, and converted via the classes in
  `mapper/`.
- **Rating aggregates are denormalized onto `BassStringProduct`** (`averageOverallRating`,
  `reviewCount`, `suitablePlayingStyles`, `taggedGenres`) and recomputed by
  `ReviewService` on every review create/update/delete, so the catalog can filter and
  sort by rating/suitability with a plain field query instead of an aggregation join on
  every page view. A style is added to `suitablePlayingStyles` once its average rating
  across that product's reviews reaches 4/5.
- **Authorization** is enforced in three layers: URL-pattern rules in `SecurityConfig`
  for what a role can do in general; `@PreAuthorize` on admin-only service methods; and
  explicit author-or-moderator checks inside `ReviewService`/`CommentService` for
  operations that need the resource loaded first to know who owns it (throwing Spring
  Security's `AccessDeniedException` so the 403 response shape stays consistent for both
  the API and the web UI - see `RestAwareAccessDeniedHandler`).
- **Apple's one-time name field isn't captured.** Apple only ever sends the user's name
  once, in a one-time form field on the very first authorization - never in the
  `id_token` claims Spring Security reads by default. `AccountProvisioningOidcUserService`
  falls back to the email address for the display name on Apple sign-in; capturing that
  one-time name would mean intercepting the raw authorization response before Spring
  Security parses it.

## What's not built (by design, for this MVP)

The clarifying questions at the start of this project scoped the first build to the
core catalog + reviews experience. Explicitly out of scope for now:
- An admin dashboard/UI for managing the catalog or moderating content (all of that is
  reachable today via the REST API by an ADMIN/MONITOR, per the OpenAPI spec).
- Subscription/paywall logic (e.g. Stripe-gated content).
- Search/filter refinements beyond what's in `BassStringSearchCriteria` today.
