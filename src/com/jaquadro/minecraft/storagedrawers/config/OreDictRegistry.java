package com.jaquadro.minecraft.storagedrawers.config;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OreDictRegistry
{
    private Set<String> blacklist = new HashSet<String>();
    private List<String> blacklistPrefix = new ArrayList<String>();

    private Set<String> blacklistCache = new HashSet<String>();
    private Set<String> graylistCache = new HashSet<String>();

    public OreDictRegistry () {
        register("logWood");
        register("plankWood");
        register("slabWood");
        register("stairWood");
        register("treeSapling");
        register("treeLeaves");
        register("blockGlass");
        register("paneGlass");
        register("record");
        register("stone");
        register("cobblestone");
        register("sand");
        register("sandstone");

        registerPrefix("list");
    }

    public boolean register (String entry) {
        if (entry == null)
            return false;

        blacklistCache.add(entry);
        graylistCache.remove(entry);

        return blacklist.add(entry);
    }

    public boolean registerPrefix (String entry) {
        if (entry == null)
            return false;

        if (blacklistPrefix.contains(entry))
            return false;

        graylistCache.clear();

        return blacklistPrefix.add(entry);
    }

    public boolean unregister (String entry) {
        blacklistCache.remove(entry);

        return blacklist.remove(entry);
    }

    public boolean unregisterPrefix (String entry) {
        return blacklistPrefix.remove(entry);
    }

    public boolean isEntryBlacklisted (String entry) {
        if (blacklistCache.contains(entry))
            return true;

        for (int i = 0, n = blacklistPrefix.size(); i < n; i++) {
            if (entry.startsWith(blacklistPrefix.get(i))) {
                blacklistCache.add(entry);
                return true;
            }
        }

        return false;
    }

    public boolean isEntryValid (String entry) {
        if (graylistCache.contains(entry))
            return true;

        if (isEntryBlacklisted(entry))
            return false;

        if (!isValidForEquiv(entry)) {
            blacklistCache.add(entry);
            return false;
        }

        graylistCache.add(entry);

        return true;
    }

    private boolean isValidForEquiv (String oreName) {
        List<ItemStack> oreList = OreDictionary.getOres(oreName);
        if (oreList.size() == 0)
            return false;

        // Fail entries that have any wildcard items registered to them.

        for (int i = 0, n = oreList.size(); i < n; i++) {
            if (oreList.get(i).getItemDamage() == OreDictionary.WILDCARD_VALUE)
                return false;
        }

        // Fail entries where the keys in at least one stack are not the super-set of all other stacks.
        // Can be determined by merging all keys and testing cardinality.

        HashSet<Integer> mergedIds = new HashSet<Integer>();
        int maxKeyCount = 0;

        for (int i = 0, n = oreList.size(); i < n; i++) {
            int[] ids = OreDictionary.getOreIDs(oreList.get(i));
            maxKeyCount = Math.max(maxKeyCount, ids.length);

            for (int id : ids)
                mergedIds.add(id);
        }

        if (maxKeyCount < mergedIds.size())
            return false;

        return true;
    }
}
