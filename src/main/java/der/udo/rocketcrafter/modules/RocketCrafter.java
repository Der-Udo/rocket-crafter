package der.udo.rocketcrafter.modules;

import der.udo.rocketcrafter.RocketCraft;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.settings.*;

import meteordevelopment.meteorclient.systems.modules.Modules;





public class RocketCrafter extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Integer> autoCraftInitDelay = sgGeneral.add(new IntSetting.Builder()
        .name("initial-delay")
        .description("The initial delay before crafting in milliseconds. 0 to use normal delay instead.")
        .defaultValue(50)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> autoCraftDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The minimum delay between crafting the next stack in milliseconds.")
        .defaultValue(25)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> autoCraftRandomDelay = sgGeneral.add(new IntSetting.Builder()
        .name("random")
        .description("Randomly adds a delay of up to the specified time in milliseconds.")
        .min(0)
        .sliderMax(1000)
        .defaultValue(50)
        .build()
    );


    public RocketCrafter() {
        super(RocketCraft.CATEGORY, "rocket-crafter", "Craft D3 rockets from crafting table menu.");
    }

    private int getSleepTime() {
        return autoCraftDelay.get() + (autoCraftRandomDelay.get() > 0 ? ThreadLocalRandom.current().nextInt(0, autoCraftRandomDelay.get()) : 0);
    }



    public void craft(ScreenHandler handler) {
        int playerInvOffset = SlotUtils.indexToId(SlotUtils.MAIN_START);
        MeteorExecutor.execute(() -> craftSlots(handler, playerInvOffset, playerInvOffset + 4 * 9, false));
    }


    private void craftSlots(ScreenHandler handler, int start, int end, boolean steal) {
        boolean initial = autoCraftInitDelay.get() != 0;


        
        ArrayList<Integer> reedIndexes = new ArrayList<>();
        //find reeds
        for (int i = start; i < end; i++) {
            if (!handler.getSlot(i).hasStack()) continue;
            
            // Exit if user closes screen or exit world
            if (mc.currentScreen == null || !Utils.canUpdate()) break;

            Item item = handler.getSlot(i).getStack().getItem();

            if(item == Items.SUGAR_CANE){
                reedIndexes.add(i);
                //ChatUtils.info("adding reed inventory slot " + String.valueOf(i));
            }
        }

        //craft paper with delay
        int reedsInCraftingTable = 0;
        while(reedIndexes.size()>=3 || reedsInCraftingTable > 0){
            int sleep;
            if (initial) {
                sleep = autoCraftInitDelay.get();
                initial = false;
            } else sleep = getSleepTime();
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(reedsInCraftingTable == 3){
                InvUtils.shiftClick().slotId(0);
                reedsInCraftingTable = 0;
            } else {
                //ChatUtils.info("placing in grid from slot " + String.valueOf(reedIndexes.get(0)));
                InvUtils.shiftClick().slotId(reedIndexes.get(0));
                reedIndexes.remove(0);
                reedsInCraftingTable = reedsInCraftingTable + 1;
                //reedsIndex = reedsIndex + 1;
            }
            


        }

        //find paper and rockets
        ArrayList<Integer> paperIndexes = new ArrayList<>();
        ArrayList<Integer> gunpowderIndexes = new ArrayList<>();
        for (int i = start; i < end; i++) {
            if (!handler.getSlot(i).hasStack()) continue;
            
            // Exit if user closes screen or exit world
            if (mc.currentScreen == null || !Utils.canUpdate()) break;

            Item item = handler.getSlot(i).getStack().getItem();

            if(item == Items.GUNPOWDER){
                gunpowderIndexes.add(i);
                //ChatUtils.info("adding gp inventory slot " + String.valueOf(i));
            }
            if(item == Items.PAPER){
                paperIndexes.add(i);
                //ChatUtils.info("adding paper inventory slot " + String.valueOf(i));
            }
        }

        //craft rockets with delay
        int paperInCraftingTable = 0;
        int gunpowderInCraftingTable = 0;
        while((paperIndexes.size()>=1 && gunpowderIndexes.size() >= 3) || paperInCraftingTable > 0 || gunpowderInCraftingTable > 0){
            int sleep;
            if (initial) {
                sleep = autoCraftInitDelay.get();
                initial = false;
            } else sleep = getSleepTime();
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(gunpowderInCraftingTable == 3 && paperInCraftingTable == 1){
                //ChatUtils.info("Click Crafting slot");
                InvUtils.shiftClick().slotId(0);
                gunpowderInCraftingTable = 0;
                paperInCraftingTable = 0;
            } else {
                if (paperInCraftingTable == 1){
                    //ChatUtils.info("placing gp in grid from slot " + String.valueOf(gunpowderIndexes.get(0)));
                    InvUtils.shiftClick().slotId(gunpowderIndexes.get(0));
                    gunpowderIndexes.remove(0);
                    gunpowderInCraftingTable = gunpowderInCraftingTable + 1;
                } else {
                    //ChatUtils.info("placing paper in grid from slot " + String.valueOf(gunpowderIndexes.get(0)));
                    InvUtils.shiftClick().slotId(paperIndexes.get(0));
                    paperIndexes.remove(0);
                    paperInCraftingTable = paperInCraftingTable + 1;
                }
                   
            }
            


        }
    
    }
   

    


}
