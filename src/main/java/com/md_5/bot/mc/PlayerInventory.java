package com.md_5.bot.mc;

import java.util.Arrays;
import lombok.Data;
import net.minecraft.server.ItemStack;

@Data
public class PlayerInventory {

    private final ItemStack[] slots = new ItemStack[45];

    /**
     * Set the index of this inventory to an itemstack.
     *
     * @param index slot to put the stack
     * @param itemstack stack to put in the slot
     */
    public void setItem(int index, ItemStack itemstack) {
        slots[index] = itemstack;
    }

    /**
     * Get the itemstack currently in the specified slot.
     *
     * @param index the index of the slot in question
     * @return the itemstack in this slot
     */
    public ItemStack getItem(int index) {
        return slots[index];
    }

    /**
     * An array containing all the armor slots. First armor slot (helmet) is
     * index 0.
     *
     * @return the armor slots
     */
    public ItemStack[] getArmor() {
        return Arrays.copyOfRange(slots, 5, 8);
    }

    /**
     * An array containing all the hotbar slots. First slot (leftmost) is index
     * 0.
     *
     * @return the hotbar slots
     */
    public ItemStack[] getHotbar() {
        return Arrays.copyOfRange(slots, 36, 44);
    }

    /**
     * An array containing all the crafting slots. First slot (top right) is
     * index 0. The index works around the 4 slots in a clockwise direction.
     *
     * @return the crafting slots
     */
    public ItemStack[] getCrafting() {
        return Arrays.copyOfRange(slots, 1, 4);
    }

    /**
     * Returns the current output of the inbuilt crafting slot.
     *
     * @return current crafting output
     */
    public ItemStack getCraftOutput() {
        return slots[0];
    }

    /**
     * Gets the main storage area of this inventory.
     *
     * @return the main storage area
     */
    public ItemStack[] getMainInventory() {
        return Arrays.copyOfRange(slots, 9, 35);
    }
}
