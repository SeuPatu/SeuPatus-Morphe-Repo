package app.template.patches.tomato.cast

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.template.patches.shared.Constants.COMPATIBILITY_TOMATO
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS = "Lapp/template/extension/tomato/VlcCastPatch;"
private const val CAST_BUTTON_FACTORY = "Lcom/google/android/gms/cast/framework/CastButtonFactory;"

@Suppress("unused")
val vlcCastPatch = patch(
    name = "Cast via VLC",
    description = "Replaces the Google Cast button action with opening the current stream in VLC.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TOMATO)

    extendWith("extensions/tomato.mpe")

    execute {
        val method = PlayerActivityOnCreateFingerprint.method

        // Snapshot dos pontos de inserção: (índice da chamada, registo do botão, é range?)
        data class Hook(val index: Int, val buttonRegister: Int)

        val hooks = method.implementation!!.instructions.withIndex().mapNotNull { (index, ins) ->
            val isStaticCall = ins.opcode == Opcode.INVOKE_STATIC || ins.opcode == Opcode.INVOKE_STATIC_RANGE
            if (!isStaticCall) return@mapNotNull null

            val ref = (ins as ReferenceInstruction).reference as? MethodReference ?: return@mapNotNull null
            if (ref.definingClass != CAST_BUTTON_FACTORY || ref.name != "setUpMediaRouteButton") {
                return@mapNotNull null
            }

            // setUpMediaRouteButton(Context, MediaRouteButton): o botão é o 2.º argumento.
            val buttonRegister = when (ins) {
                is FiveRegisterInstruction -> ins.registerD
                is RegisterRangeInstruction -> ins.startRegister + 1
                else -> return@mapNotNull null
            }
            Hook(index, buttonRegister)
        }.toList()

        check(hooks.isNotEmpty()) {
            "CastButtonFactory.setUpMediaRouteButton not found in PlayerActivity.onCreate"
        }

        // De trás para a frente para não deslocar os índices.
        hooks.asReversed().forEach { (index, reg) ->
            val call = if (reg <= 15) {
                "invoke-static { v$reg }, $EXTENSION_CLASS->hook(Landroid/view/View;)V"
            } else {
                "invoke-static/range { v$reg .. v$reg }, $EXTENSION_CLASS->hook(Landroid/view/View;)V"
            }
            method.addInstructions(index + 1, call)
        }
    }
}
