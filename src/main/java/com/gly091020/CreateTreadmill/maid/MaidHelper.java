package com.gly091020.CreateTreadmill.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.Entity;

public class MaidHelper {
    public static int getMaidLevel(Entity maid){
        return ((EntityMaid)(maid)).getFavorabilityManager().getLevel();
    }

    public static boolean isMaid(Entity entity){
        return entity instanceof EntityMaid;
    }
}
