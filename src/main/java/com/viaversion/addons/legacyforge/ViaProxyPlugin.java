package com.viaversion.addons.legacyforge;

import net.lenni0451.lambdaevents.EventHandler;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.plugins.events.ViaLoadingEvent;

@SuppressWarnings("unused")
public class ViaProxyPlugin extends net.raphimc.viaproxy.plugins.ViaProxyPlugin {

    @Override
    public void onEnable() {
        ViaProxy.EVENT_MANAGER.register(this);
    }

    @EventHandler
    public void onViaLoading(final ViaLoadingEvent event) {
        LegacyForgeHandler.init();
    }

}
