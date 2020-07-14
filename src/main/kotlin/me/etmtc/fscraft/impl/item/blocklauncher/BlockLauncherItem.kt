package me.etmtc.fscraft.impl.item.blocklauncher

import me.etmtc.fscraft.Registries
import me.etmtc.fscraft.impl.FSFallingBlockEntity
import me.etmtc.fscraft.impl.FSGroup
import me.etmtc.fscraft.maybePut
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.*
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
fun Item.getBlockState(player: PlayerEntity? = null):BlockState?{
    val block = Block.getBlockFromItem(this)
    return if(block == Blocks.AIR) null else try {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        block.getStateForPlacement(BlockItemUseContext(
                ItemUseContext(player, Hand.MAIN_HAND, RayTrace.nullResult()))
        ) ?: error("")
    } catch (t: Throwable){
        block.defaultState
    }
}
object BlockLauncherItem : Item(Properties().maxStackSize(1).group(FSGroup)), INamedContainerProvider {
    override fun onItemRightClick(worldIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {
        if (handIn == Hand.MAIN_HAND && !worldIn.isRemote) {
            if (playerIn.isSneaking){
                NetworkHooks.openGui(playerIn as ServerPlayerEntity, this)
            } else {
                playerIn.getHeldItem(Hand.MAIN_HAND)
                        .orCreateTag
                        .maybePut("BlockLauncher") { CompoundNBT() }
                        .getCompound("LauncherInventory")
                        .getList("Items", 10).also { listTag ->
                            listTag.firstOrNull()?.let { tag ->
                                if (tag is CompoundNBT) {
                                    val stack = ItemStack.read(tag)
                                    val item = stack.item
                                    if (item != Items.AIR && !stack.isEmpty) {
                                        stack.shrink(1)
                                        stack.item.getBlockState(playerIn)?.let {
                                            val vec = playerIn.positionVec
                                            val fbe = FSFallingBlockEntity(worldIn, vec.x, vec.y + 1.62, vec.z, it)
                                            fbe.motion = playerIn.lookVec.mul(2.0, 2.0, 2.0)
                                            worldIn.addEntity(fbe)
                                        }
                                        if (stack.isEmpty)
                                            listTag.remove(tag)
                                        else stack.write(tag)
                                    }
                                }
                            }
                        }
            }
        }
        return ActionResult(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn))
    }

    fun getInventory(entity: PlayerEntity): BlockLauncherInventory {
        val stack = entity.getHeldItem(Hand.MAIN_HAND)
        val nbt = stack.orCreateTag
        val tag = nbt.maybePut("BlockLauncher") { CompoundNBT() }
        return BlockLauncherInventory(tag, Hand.MAIN_HAND)
    }

    override fun createMenu(i: Int, playerInventory: PlayerInventory, playerEntity: PlayerEntity): net.minecraft.inventory.container.Container? {
        return BlockLauncherContainer(Registries.blockLauncherContainerType, i, playerInventory, getInventory(playerEntity))
    }

    override fun getDisplayName(): ITextComponent = TranslationTextComponent("container.fscraft.block_launcher")
}
