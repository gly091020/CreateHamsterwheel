package com.gly091020.CreateTreadmill.maid;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

public class MaidEventHandler {
    @SubscribeEvent
    public static void onInteractMaid(InteractMaidEvent event){
        var maid = event.getMaid();
        var player = event.getPlayer();
        var stack = event.getStack();
        if(maid.getOwner() != null && player.is(maid.getOwner()) && stack.is(Items.LEAD)){
            maid.setLeashedTo(player, true);
            stack.shrink(1);
            event.setCanceled(true);
        }
    }
}
