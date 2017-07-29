
package io.luna.game.plugin;

import io.luna.LunaContext;
import io.luna.game.event.Event;
import java.io.File;

/**
 * A interface for implementation by a plugin to be loaded into the server
 * 
 * @author Trevor Flynn {@literal <trevorflynn@liquidcrystalstudios.com>}
 */
public interface Plugin {
    
    /**
     * The name of your plugin.
     * 
     * The name of the plugin should not contain any spaces of special chars.
     * It should only contain the characters _,a-z,A-Z,0-9
     * @return The name of the plugin
     */
    public String getName();
    
    /**
     * 
     * @return The unique version ID if the plugin 
     */
    public int getVersionID();
    
    /**
     * Init will be run prior to start.
     * This is a good time to load configuration and to perform
     * any pre-runtime setup.
     * 
     * @param context The LunaContext for the server
     * @param config The configuration file for this plugin
     */
    public void init(LunaContext context, File config);
    
    /**
     * Start will be run after the <code>init(LunaContext context)</code>.
     * 
     */
    public void start();
    
    /**
     * This function will get called any time an event is posted to the plugins.
     * 
     * @param event The event that was posted
     */
    public void event(Event event);
}
