package app.morphe.patches.tomato.cast

// ATENÇÃO: o esqueleto (imports, compatibleWith, ligação à extensão) tem de ser copiado
// do patch de exemplo que vem no morphe-patches-template. O miolo do `execute` é o que importa.

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION = "Lapp/morphe/extension/tomato/VlcCastPatch;"
private const val CAST_BUTTON_FACTORY = "Lcom/google/android/gms/cast/framework/CastButtonFactory;"

@Suppress("unused")
val vlcCastPatch = bytecodePatch(
    name = "Cast via VLC",
    description = "Replaces the Google Cast button action with opening the stream in VLC.",
) {
    // TODO: compatibleWith("com.tomatos.clientapp"("1.5.1")) — forma exacta conforme o template
    // TODO: dependsOn(<patch da extensão do template>)

    execute {
        val method = PlayerActivityOnCreateFingerprint.method
        val instructions = method.implementation!!.instructions

        // Procura TODAS as chamadas CastButtonFactory.setUpMediaRouteButton(Context, MediaRouteButton)
        // e, logo a seguir a cada uma, pendura o nosso hook no botão (2.º argumento).
        val indices = instructions.withIndex().filter { (_, ins) ->
            ins.opcode == Opcode.INVOKE_STATIC &&
                ((ins as ReferenceInstruction).reference as? MethodReference)?.let {
                    it.definingClass == CAST_BUTTON_FACTORY && it.name == "setUpMediaRouteButton"
                } == true
        }.map { it.index }

        check(indices.isNotEmpty()) { "setUpMediaRouteButton not found in PlayerActivity.onCreate" }

        // De trás para a frente para não deslocar os índices
        indices.asReversed().forEach { index ->
            val button = method.getInstruction<FiveRegisterInstruction>(index).registerD
            method.addInstructions(
                index + 1,
                "invoke-static { v$button }, $EXTENSION->hook(Landroid/view/View;)V",
            )
        }
    }
}
