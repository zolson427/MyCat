package dk.zolson427.mycat;

import java.util.UUID;

import dk.zolson427.mycat.objects.myCat;
import org.bukkit.entity.Cat;

public class MyCatAPI {
    private final MyCat plugin;

    public MyCatAPI(MyCat p) {
        this.plugin = p;
    }

    public boolean isCat(UUID catUUID) {
        return MyCat.getCatManager().isCat(catUUID);
    }

    public boolean isCat(Cat cat) {
        return MyCat.getCatManager().isCat(cat.getUniqueId());
    }

    public myCat getCat(UUID catUUID) {
        return MyCat.getCatManager().getCat(catUUID);
    }
}
