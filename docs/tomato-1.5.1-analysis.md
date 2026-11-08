# Tomato Animes 1.5.1 — análise do cast

- Package: `com.tomatos.clientapp`, versionName 1.5.1, versionCode 101, minSdk 24, targetSdk 34
- App em React Native (Hermes) + parte nativa Android com Media3/ExoPlayer
- 8 ficheiros dex; classes relevantes não estão ofuscadas

## Onde está o cast

Tudo no `com.tomatos.clientapp.PlayerActivity` (+ inner classes):

- Campos: `mCastContext`, `mSessionManager`, `mSessionManagerListener`, `mCastSession`,
  `mRemoteMediaClient`, `isCastingActive`, `mCastAllowed`, `castOverlay`, `castButtonHolder`,
  `overlayMediaRouteButton` (`androidx.mediarouter.app.MediaRouteButton`)
- `defaultURL` (String): URL do stream, preenchida depois de a API do episódio responder
- `static ExoPlayer player`
- Métodos: `onCreate` (faz `CastContext.getSharedInstance` e `CastButtonFactory.setUpMediaRouteButton`
  duas vezes), `canStartCastSession`, `isCurrentCastMedia`, `initializePlayer`
- Inner: `PlayerActivity$SessionManagerListenerImpl` (onSessionStarted/Ended/...), `PlayerActivity$2` (Handler)
- Manifest: `OPTIONS_PROVIDER_CLASS_NAME = androidx.media3.cast.DefaultCastOptionsProvider`,
  `ReconnectionService`, `MediaIntentReceiver`
- Layouts com `castButton`: `ExoPlayerControlViewBinding`, `CardPlayerSettingsBinding`

## Ainda por descobrir

- Os headers HTTP do stream: `initializePlayer` chama
  `DefaultHttpDataSource.Factory.setDefaultRequestProperties(Map)`. Se o servidor exigir
  Referer/User-Agent, o VLC precisa deles (extra `headers` do intent).
- Se há outros pontos de entrada de cast (ex.: botão no `CardPlayerSettings`).
