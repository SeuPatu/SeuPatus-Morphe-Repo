package app.morphe.patches.tomato.cast

// ATENÇÃO: confirma contra o exemplo de Fingerprint do template (imports / parâmetros
// do construtor podem diferir). A lógica abaixo vem da análise do APK 1.5.1.

import app.morphe.patcher.Fingerprint

/** PlayerActivity.onCreate — é aqui que o botão de cast é ligado ao Google Cast. */
internal object PlayerActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/tomatos/clientapp/PlayerActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
