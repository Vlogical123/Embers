package teamroots.embers.itemmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.RegistryManager;
import teamroots.embers.network.PacketHandler;
import teamroots.embers.network.message.MessagePlayerJetFX;
import teamroots.embers.network.message.MessageRemovePlayerEmber;
import teamroots.embers.network.message.MessageSpawnEmberProj;
import teamroots.embers.network.message.MessageSuperheatFX;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.util.EmberInventoryUtil;
import teamroots.embers.util.ItemModUtil;
import teamroots.embers.util.Misc;

public class ModifierResonatingBell extends ModifierBase {

	public ModifierResonatingBell() {
		super(EnumType.TOOL,"resonating_bell",5.0,true);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public int cooldown = 0;
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event){
		if (event.phase == TickEvent.Phase.START){
			if (cooldown > 0){
				cooldown --;
			}
		}
	}
	
	@SubscribeEvent
	public void onClick(PlayerInteractEvent.RightClickBlock event){
		ItemStack s = event.getItemStack();
		if (ItemModUtil.hasHeat(s) && cooldown == 0){
			int level = ItemModUtil.getModifierLevel(s, ItemModUtil.modifierRegistry.get(RegistryManager.resonating_bell).name);
			if (event.getWorld().isRemote && level > 0 && EmberInventoryUtil.getEmberTotal(event.getEntityPlayer()) > cost){
				cooldown = 80;
				IBlockState state = event.getWorld().getBlockState(event.getPos());
				int count = 0;
				List<BlockPos> positions = new ArrayList<BlockPos>();
				for (int i = -(1+3*level); i < (2+3*level); i ++){
					for (int j = -(1+3*level); j < (2+3*level); j ++){
						for (int k = -(1+3*level); k < (2+3*level); k ++){
							if (event.getWorld().getBlockState(event.getPos().add(i, j, k)) == state){
								positions.add(event.getPos().add(i,j,k));
								count ++;
							}
						}
					}
				}
				if (count < 200*level){
					for (BlockPos p : positions){
						for (int i = 0; i < 3; i ++){
							ParticleUtil.spawnParticleGlowThroughBlocks(event.getWorld(), p.getX()+0.5f, p.getY()+0.5f, p.getZ()+0.5f, 0.0625f*(Misc.random.nextFloat()-0.5f), 0.0625f*(Misc.random.nextFloat()-0.5f), 0.0625f*(Misc.random.nextFloat()-0.5f), 255, 64, 16, 0.0625f*Misc.random.nextFloat()+0.0625f, 8.0f+16.0f*Misc.random.nextFloat(), 200);
						}
					}
				}
				PacketHandler.INSTANCE.sendToServer(new MessageRemovePlayerEmber(event.getEntityPlayer().getUniqueID(),cost));
			}
		}
	}
}
