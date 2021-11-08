package youyihj.tinkersoredictcache;

import com.google.common.collect.Iterables;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mod(
        modid = TinkersOreDictCache.MOD_ID,
        name = TinkersOreDictCache.MOD_NAME,
        version = TinkersOreDictCache.VERSION,
        dependencies = TinkersOreDictCache.DEPENDENCIES
)
public class TinkersOreDictCache {

    public static final String MOD_ID = "tinkersoredictcache";
    public static final String MOD_NAME = "TinkersOreDictCache";
    public static final String VERSION = "1.0";
    public static final String DEPENDENCIES = "required-after:tconstruct";
    public Logger logger;
    public final File cacheFile = new File("config/tinker_ore_dict_melting_cache.dat");
    public NBTTagCompound cacheNBT = new NBTTagCompound();

    @Mod.Instance
    public static TinkersOreDictCache INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        OreDictRecipesState.setCurrentState(cacheFile.exists() ? OreDictRecipesState.READ : OreDictRecipesState.SCAN);
        if (OreDictRecipesState.getCurrentState().isRead()) {
            try {
                cacheNBT = Objects.requireNonNull(CompressedStreamTools.read(cacheFile));
            } catch (IOException e) {
                logger.error("failed to read cache file! We will rescan all recipes...", e);
                OreDictRecipesState.setCurrentState(OreDictRecipesState.SCAN);
            }
            final NBTTagList modsNBT = cacheNBT.getTagList("Mods", Constants.NBT.TAG_STRING);
            if (!hasSameElements(getInstalledModIds(), StreamSupport.stream(modsNBT.spliterator(), false).map(nbt -> ((NBTTagString) nbt).getString()).collect(Collectors.toList()))) {
                logger.info("Detected mods installed are different from the last starting. We will rescan all recipes...");
                cacheNBT = new NBTTagCompound();
                OreDictRecipesState.setCurrentState(OreDictRecipesState.SCAN);
            }
        }
        if (OreDictRecipesState.getCurrentState().isScan()) {
            final NBTTagList modsNBT = new NBTTagList();
            getInstalledModIds().stream().map(NBTTagString::new).forEach(modsNBT::appendTag);
            cacheNBT.setTag("Mods", modsNBT);
        }
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        cacheNBT = null;
    }

    private Collection<String> getInstalledModIds() {
        return Loader.instance().getIndexedModList().keySet();
    }

    private <T> boolean hasSameElements(Collection<T> first, Collection<T> second) {
        if (first.size() != second.size()) {
            return false;
        }
        Set<T> collection = new HashSet<>(first);
        collection.addAll(second);
        return collection.size() == first.size();
    }

}
