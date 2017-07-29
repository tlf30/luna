package io.luna.game.plugin;

import io.luna.LunaContext;
import io.luna.game.event.Event;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A manager for plugins that loads and managers each plugin.
 * 
 * Used for jar based plugins
 * 
 * @author Trevor Flynn {@literal <trevorflynn@liquidcrystalstudios.com>}
 */
public final class PluginManager {

    private final LunaContext context;
    private List<Plugin> plugins = new ArrayList<>();
    //
    private final String PLUGIN_DIR = "plugins";
    private final String CONFIG_DIR = "config";

    public PluginManager(LunaContext context) {
        this.context = context;
    }
    
    public void load() throws MalformedURLException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        File pluginDir = new File(PLUGIN_DIR);
        File[] files = pluginDir.listFiles();
        ArrayList<URL> modFiles = new ArrayList<>();
        for (File s : files) {
            if (s.getName().endsWith(".jar")) {
                modFiles.add(s.toURI().toURL());
            }
        }
        URL[] importableMods = modFiles.toArray(new URL[modFiles.size()]);
        ClassLoader loader = new URLClassLoader(importableMods, ClassLoader.getSystemClassLoader()); //Build a classloader from the system classloader 
        for (URL jar : importableMods) { //Get each jar
            String[] classes = getClasses(jar); //Check class path
            for (String classpath : classes) {
                Class plugin = loader.loadClass(classpath);
                if (isPlugin(plugin)) { //If class is a plugin, add it
                    Plugin plugininstance = (Plugin) plugin.newInstance();
                    plugins.add(plugininstance);
                }
            }
        }
    }
    
    public void init() {
        for (Plugin plugin : plugins) {
            plugin.init(context, new File(CONFIG_DIR + File.separator + plugin.getName()));
        }
    }
    
    public void start() {
        for (Plugin plugin : plugins) {
            plugin.start();
        }
    }
    
    public void post(Event event) {
        for (Plugin plugin : plugins) {
            plugin.event(event);
        }
    }
    
    private boolean isPlugin(Class c) {
        if (c.isSynthetic() || c.isAnnotation() || c.isEnum() || c.isInterface()
                || c.isLocalClass() || c.isMemberClass() || c.isPrimitive()
                || c.isAnonymousClass() || c.isArray()) {
            //We do not want any of these
            return false;
        }
       //Get the interfaces
        for (Class inf : c.getInterfaces()) {
            if (inf.isAssignableFrom(Plugin.class)) {
                //If it is an instance of plugin, we want it
                return true;
            }
        }
        return false;
    }

    private String[] getClasses(URL url) throws FileNotFoundException, IOException {
        ArrayList<String> classNames = new ArrayList<>();
        ZipInputStream zip = new ZipInputStream(new FileInputStream(url.getFile())); //Load the jar as a zip for processing
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) { //Get each entry in the zip
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) { //If the entry is a class we need it
                String className = entry.getName().replace('/', '.'); // Fix format of path to make it a class path
                classNames.add(className.substring(0, className.length() - ".class".length())); //Take the '.class' off the end as it is not part of the class path
            }
        }
        return classNames.toArray(new String[classNames.size()]); //Return the class paths
    }
}
