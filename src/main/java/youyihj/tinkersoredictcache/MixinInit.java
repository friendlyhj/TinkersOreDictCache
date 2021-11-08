package youyihj.tinkersoredictcache;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.MixinLoader;

/**
 * @author youyihj
 */
@MixinLoader
public class MixinInit {
    public MixinInit() {
        Mixins.addConfiguration("mixins.tic_oredict_cache.json");
    }
}
