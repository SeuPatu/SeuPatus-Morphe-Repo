package app.template.patches.tomato.cast

import app.morphe.patcher.Fingerprint

/**
 * PlayerActivity.onCreate: é aqui que os botões de cast (MediaRouteButton) são ligados
 * ao Google Cast através de CastButtonFactory.setUpMediaRouteButton().
 * Em Tomato 1.5.1 esta classe não está ofuscada.
 */
object PlayerActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/tomatos/clientapp/PlayerActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
