package loqor.ait.tardis.desktops;

import loqor.ait.AITMod;
import loqor.ait.tardis.TardisDesktopSchema;
import loqor.ait.tardis.desktops.textures.DesktopPreviewTexture;
import net.minecraft.util.Identifier;

public class DefaultCaveDesktop extends TardisDesktopSchema {

	public DefaultCaveDesktop() {
		super(new Identifier(AITMod.MOD_ID, "default_cave"), new DesktopPreviewTexture(
				DesktopPreviewTexture.pathFromDesktopId(new Identifier(AITMod.MOD_ID, "cave"))));
	}

	@Override
	public boolean freebie() {
		return false;
	}
}
