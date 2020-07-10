
package me.etmtc.fscraft.config

import net.minecraftforge.common.ForgeConfigSpec
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun ForgeConfigSpec.Builder.section(str:String, op:ForgeConfigSpec.Builder.() -> Unit) {
    contract { callsInPlace(op, InvocationKind.EXACTLY_ONCE) }
    push(str)
    op()
    pop()
}
@OptIn(ExperimentalContracts::class)
inline fun ForgeConfigSpec.Builder.configure(path:String,boolean: Boolean, op: DefiningContext.() -> Unit):ForgeConfigSpec.BooleanValue{
    contract { callsInPlace(op, InvocationKind.EXACTLY_ONCE) }
    val ctx = DefiningContext().apply(op)
    ctx.comment?.let { comment(*it) }
    ctx.translation?.let { translation(it) }
    return define(path,boolean)
}
class DefiningContext @PublishedApi internal constructor() {
    var comment :Array<String>? = null
    var translation: String? = null
}
inline operator fun ForgeConfigSpec.BooleanValue.invoke(): Boolean = get()