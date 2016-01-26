package mcjty.deepresonance.proxy;

import elec332.core.client.IIconRegistrar;
import elec332.core.client.ITextureLoader;
import elec332.core.client.model.RenderingRegistry;
import mcjty.deepresonance.RadiationOverlayRenderer;
import mcjty.deepresonance.client.gui.NoRFFoundException;
import mcjty.deepresonance.client.render.ModRenderers;
import mcjty.deepresonance.fluid.DRFluidRegistry;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy implements ITextureLoader{

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        RenderingRegistry.instance().registerTextureLoader(this);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ModRenderers.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @Override
    public void throwException(Exception e, int i) {
        switch (i){
            case 0:
                throw new NoRFFoundException(e);
            default:
                throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void renderGameOverlayEvent(RenderGameOverlayEvent evt) {
        RadiationOverlayRenderer.onRender(evt);
    }

    @Override
    public void registerTextures(IIconRegistrar iIconRegistrar) {
        DRFluidRegistry.registerIcons(iIconRegistrar);
    }

}
