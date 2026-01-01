package com.gly091020.CreateTreadmill.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.sch246.muhc.Config;

public class MaidUseHandCrankUtil {
    public static float getMaidStress(EntityMaid maid){
        var once = Config.BASE_STRESS.get() +
                maid.getFavorability() * Config.STRESS_PER_FAVORABILITY.get();
        var count = 1;
        if(maid.getFavorability() == 384){
            count = Config.TWO_HANDED_OPERATION.get() ? 4 : 2;
        }
        else if(maid.getFavorability() >= 384 / 2f){
            count = Config.TWO_HANDED_OPERATION.get() ? 2 : 1;
        }
        return once * count;
    }
}
